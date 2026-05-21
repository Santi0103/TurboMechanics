package com.proyecto.TurboMechanics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.NotificationConsent;

@Repository
public interface NotificationConsentRepository extends JpaRepository<NotificationConsent, Long> {

    Optional<NotificationConsent> findByUsersIdentification(Integer identification);
}