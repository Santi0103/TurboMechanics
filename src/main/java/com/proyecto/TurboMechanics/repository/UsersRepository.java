package com.proyecto.TurboMechanics.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Users;



@Repository
public interface UsersRepository extends JpaRepository<Users, Long>{

    Optional<Users> findByEmail(String email);
    
} 
