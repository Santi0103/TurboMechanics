package com.proyecto.TurboMechanics.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.RegisterRequestDTO;
import com.proyecto.TurboMechanics.entity.RolType;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    /**
     * Registers a new user (CLIENT role by default)
     * @param request DTO with username, email and password
     * @return success message
     */
   @Transactional
    public MessageResponseDTO register(@Valid RegisterRequestDTO request) {
        MessageResponseDTO response = new MessageResponseDTO();

        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("The username is already in use");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setIdentification(request.getIdentification());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRolId(RolType.CLIENTE.getId());

        usersRepository.save(user);

        response.setMessage("User successfully registered");
        return response;
    }
}