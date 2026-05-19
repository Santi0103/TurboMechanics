package com.proyecto.TurboMechanics.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.AvailabilityAppointmentResponseDTO;
import com.proyecto.TurboMechanics.dto.CreateAppointmentRequestDTO;
import com.proyecto.TurboMechanics.dto.RescheduleAppointmentRequestDTO;
import com.proyecto.TurboMechanics.dto.SendReminderApponitmentRequestDTO;
import com.proyecto.TurboMechanics.entity.Appointment;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.Vehicle;
import com.proyecto.TurboMechanics.enums.StatusAppointment;
import com.proyecto.TurboMechanics.repository.AppointmentRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final UsersRepository       usersRepository;

    private final VehicleRepository     vehicleRepository;

    private final NotificationService   notificationService;

    /** Horarios de trabajo del taller: 8 AM a 6 PM cada hora */
    private static final List<LocalTime> WORKING_SLOTS = Stream
        .iterate(LocalTime.of(8, 0), t -> t.plusHours(1))
        .limit(10)
        .collect(Collectors.toList());

    /**
     * crear la nueva cita
     * @param request dto con los datos de entrada
     * @return retorna la cita creada
     */
    @Transactional
    public Appointment createAppointment(CreateAppointmentRequestDTO request) {

        Users users = usersRepository.findByIdentification(request.getIdentification())
            .orElseThrow(() -> new EntityNotFoundException(
                "Cliente no encontrado con documento: " + request.getIdentification()));

        Vehicle vehicle = vehicleRepository.findByPlateIgnoreCase(request.getPlate())
            .orElseThrow(() -> new EntityNotFoundException(
                "Vehículo no encontrado con placa: " + request.getPlate()));

        // Verificar disponibilidad del horario
        Boolean occupied = appointmentRepository.existsByDateAndTime(
            request.getDate(), request.getTime());
        if (occupied) {
            throw new IllegalStateException(
                "El horario " + request.getTime() + " del " + request.getDate()
                + " no está disponible. Consulte los horarios disponibles.");
        }

        Appointment appointment = Appointment.builder()
            .users(users)
            .vehicle(vehicle)
            .date(request.getDate())
            .time(request.getTime())
            .reason(request.getReason())
            .status(StatusAppointment.Scheduled)
            .createdBy(request.getCreatedBy())
            .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Cita registrada id={} para cliente={} fecha={} hora={}",
            saved.getId(), request.getIdentification(), request.getDate(), request.getTime());
        return saved;
    }

    /**
     * Consutar agenda diaria
     * @param date fecha de la consulta
     * @return retorna todas las citas registradas de ese dia
     */
    public List<Appointment> getDailyAgenda(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }

    /**
     * consultar agenda semanal
     * @param start fecha de inicio
     * @param end fecha de fin
     * @return retorna todas as citas registradas de la semana
     */
    public List<Appointment> getWeeklyAgenda(LocalDate start, LocalDate end) {
        return appointmentRepository.findByDateBetween(start, end);
    }

    /**
     * Repogramar la cita
     * @param appointmentId id de la cita
     * @param request dto para reprogrmar la cita
     * @return retorna la cita reprogramda
     */
    @Transactional
    public Appointment reschedule(Long appointmentId, RescheduleAppointmentRequestDTO request) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Cita no encontrada: " + appointmentId));

        if (appointment.getStatus() == StatusAppointment.Cancelled) {
            throw new IllegalStateException("No se puede reprogramar una cita cancelada.");
        }

        Boolean ocupado = appointmentRepository.existsByDateAndTime(
            request.getNewDate(), request.getNewTime());
        if (ocupado) {
            throw new IllegalStateException(
                "El horario " + request.getNewTime() + " del " + request.getNewDate()
                + " no está disponible.");
        }

        appointment.setDate(request.getNewDate());
        appointment.setTime(request.getNewTime());
        appointment.setStatus(StatusAppointment.Reprogrammed);

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Cita id={} reprogramada a fecha={} hora={}", appointmentId,
            request.getNewDate(), request.getNewTime());
        return saved;
    }

    /**
     * Cancelar la cita y notificar de esta
     * @param appointmentId id de la cita
     * @param adminEmail correo del administrador
     * @param mecanicoEmail correo del mecanico
     * @return retorna la cita cancelada y la notificacion de esta
     */
    @Transactional
    public Appointment cancel(Long appointmentId, String adminEmail, String mecanicoEmail) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Cita no encontrada: " + appointmentId));

        if (appointment.getStatus() == StatusAppointment.Cancelled) {
            throw new IllegalStateException("La cita ya fue cancelada.");
        }

        appointment.setStatus(StatusAppointment.Cancelled);
        Appointment saved = appointmentRepository.save(appointment);

        // Notificar al administrador y al mecánico
        String asunto = "Cita cancelada – " + appointment.getDate() + " " + appointment.getTime();
        String cuerpo = String.format(
            "El cliente %s ha cancelado la cita programada para el %s a las %s.\n" +
            "Vehículo: %s\nMotivo original: %s",
            appointment.getUsers().getUsername(),
            appointment.getDate(),
            appointment.getTime(),
            appointment.getVehicle().getPlate(),
            appointment.getReason()
        );

        notificationService.SentEmailText(adminEmail, asunto, cuerpo);
        notificationService.SentEmailText(mecanicoEmail, asunto, cuerpo);

        log.info("Cita id={} cancelada. Notificaciones enviadas.", appointmentId);
        return saved;
    }

    /**
     * Consultar la disponibilidad de horarios en una fecha
     * @param date fecha de consulta de citas
     * @return retorna las citas registradas de esa fecha
     */
    public AvailabilityAppointmentResponseDTO getAvailability(LocalDate date) {

        // Horarios ocupados: citas que no están canceladas
        List<LocalTime> occupied = appointmentRepository
            .findByDateAndStatusNot(date, StatusAppointment.Cancelled)
            .stream()
            .map(Appointment::getTime)
            .collect(Collectors.toList());

        // Horarios disponibles: todos los del taller menos los ocupados
        List<LocalTime> available = WORKING_SLOTS.stream()
            .filter(slot -> !occupied.contains(slot))
            .collect(Collectors.toList());

        return new AvailabilityAppointmentResponseDTO(available, occupied);
    }

    /**
     * Enviar recordatorio de la cita al cliente
     * @param request dto para enviar el recordatorio
     */
    public void sendReminder(SendReminderApponitmentRequestDTO request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Cita no encontrada: " + request.getAppointmentId()));

        if (appointment.getStatus() == StatusAppointment.Cancelled) {
            throw new IllegalStateException("No se puede enviar recordatorio de una cita cancelada.");
        }

        String mensaje = String.format(
            "Estimado/a %s,%n%n" +
            "Le recordamos que tiene una cita programada en TurboMechanics:%n" +
            "  Fecha   : %s%n" +
            "  Hora    : %s%n" +
            "  Vehículo: %s%n" +
            "  Motivo  : %s%n%n" +
            "Si necesita reprogramar o cancelar, comuníquese con nosotros.%n%n" +
            "Gracias por preferirnos.",
            appointment.getUsers().getUsername(),
            appointment.getDate(),
            appointment.getTime(),
            appointment.getVehicle().getPlate(),
            appointment.getReason()
        );

        String asunto = "Recordatorio de cita – " + appointment.getDate()
            + " " + appointment.getTime();

        if ("EMAIL".equalsIgnoreCase(request.getCanal())) {
            notificationService.SentEmailText(
                appointment.getUsers().getEmail(), asunto, mensaje);
        } else if ("WHATSAPP".equalsIgnoreCase(request.getCanal())) {
            notificationService.SentWhatsappText(
                appointment.getUsers().getPhone(), mensaje);
        } else {
            throw new IllegalArgumentException(
                "Canal no válido: " + request.getCanal() + ". Use EMAIL o WHATSAPP");
        }

        log.info("Recordatorio enviado por {} para cita id={}", request.getCanal(),
            request.getAppointmentId());
    }

    /**
     * citas de un cliente
     * @param identification identificacion del cliente
     * @return retorna la citas del cliente
     */
    public List<Appointment> getByCustomer(Integer identification) {
        return appointmentRepository.findByUsersIdentification(identification);
    }
}
