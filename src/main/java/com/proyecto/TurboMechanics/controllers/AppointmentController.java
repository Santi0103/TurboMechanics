package com.proyecto.TurboMechanics.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.AvailabilityAppointmentResponseDTO;
import com.proyecto.TurboMechanics.dto.CreateAppointmentRequestDTO;
import com.proyecto.TurboMechanics.dto.RescheduleAppointmentRequestDTO;
import com.proyecto.TurboMechanics.dto.SendReminderApponitmentRequestDTO;
import com.proyecto.TurboMechanics.entity.Appointment;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * crear una cita
     * @param request dto para crear la cita
     * @return retorna la cita creada
     */
    @PostMapping
    @RequiresRole({ RolEnum.CLIENTE })
    public ResponseEntity<Appointment> create(@Valid @RequestBody CreateAppointmentRequestDTO request) {
        try {
            Appointment appointment = appointmentService.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Agenda diaria
     * @param date fecha para la consulta de agenda diaria
     * @return retorna la agenda diaria
     */
    @GetMapping("/agenda/daily")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<List<Appointment>> daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Appointment> appointment = appointmentService.getDailyAgenda(date);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Agenda semanal
     * @param start fecha de inicio
     * @param end fecha de fin
     * @return retorna las citas agendadas de esa semana
     */
    @GetMapping("/agenda/weekly")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<List<Appointment>> weekly(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            List<Appointment> appointment = appointmentService.getWeeklyAgenda(start, end);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Reprogrmar cita
     * @param id id de la cita
     * @param request dto con los datos para ingresar la reprogrmacion de la cita
     * @return retorna la cita reprogrmada
     */
    @PatchMapping("/{id}/reschedule")
    @RequiresRole({ RolEnum.CLIENTE })
    public ResponseEntity<Appointment> reschedule(@PathVariable Long id,@Valid @RequestBody RescheduleAppointmentRequestDTO request) {
        try {
            Appointment appointment = appointmentService.reschedule(id, request);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Cancelar cita
     * @param id id de la cita
     * @param adminEmail correo del administrador
     * @param mecanicoEmail correo del mecanico
     * @return retorna la cita cancelada
     */
    @PatchMapping("/{id}/cancel")
    @RequiresRole({ RolEnum.CLIENTE })
    public ResponseEntity<Appointment> cancel(@PathVariable Long id, @RequestParam String adminEmail, @RequestParam String mecanicoEmail) {
        try {
            Appointment appointment = appointmentService.cancel(id, adminEmail, mecanicoEmail);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Disponibilidad de horarios
     * @param date fecha de los horarios
     * @return retorna la disponibilidad de horarios
     */
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityAppointmentResponseDTO> availability(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            AvailabilityAppointmentResponseDTO availabilityAppointmentResponseDTO = appointmentService.getAvailability(date);
            return ResponseEntity.status(HttpStatus.OK).body(availabilityAppointmentResponseDTO);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Enviar recordatorios a un cliente
     * @param request dto del recordatorio
     * @return
     */
    @PostMapping("/reminder")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<Void> sendReminder(@Valid @RequestBody SendReminderApponitmentRequestDTO request) {
        try {
            appointmentService.sendReminder(request);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Citas de un cliente
     * @param identification identificacion del cliente
     * @return retorna la citas del cliente
     */
    @GetMapping("/customer")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.CLIENTE})
    public ResponseEntity<List<Appointment>> byCustomer( @RequestParam Integer identification) {
        try {
            List<Appointment> appointment = appointmentService.getByCustomer(identification);
            return ResponseEntity.status(HttpStatus.OK).body(appointment);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }    }
}
