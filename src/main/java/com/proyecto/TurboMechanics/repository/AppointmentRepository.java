package com.proyecto.TurboMechanics.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDate(LocalDate date);

    List<Appointment> findByDateBetween(LocalDate start, LocalDate end);

    boolean existsByDateAndTime(LocalDate date, LocalTime time);

    List<Appointment> findByUsersIdentification(Integer identification);

    List<Appointment> findByDateAndStatusNot(LocalDate date,
        com.proyecto.TurboMechanics.enums.StatusAppointment status);
}
