package com.proyecto.TurboMechanics.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.proyecto.TurboMechanics.dto.LoginRequestDTO;
import com.proyecto.TurboMechanics.dto.LoginResponseDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.RefreshTokenResponseDTO;
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
        response.setMessage("registration successful");

        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("The email is already in use");
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

    public LoginResponseDTO login(LoginRequestDTO request) {
        LoginResponseDTO response = new LoginResponseDTO();
        Optional<Users> user = usersRepository.findByEmail(request.getEmail());

        if (user.isEmpty() && request.getEmail() != null) {
            response.setMessage("Este usuario no se encuentra registrado");
            return response;
        }

        Users userFound = user.get();

        if (!passwordEncoder.matches(request.getPassword(), userFound.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String jwt = jwtService.generateToken(userFound.getId(), userFound.getUsername(), userFound.getRolId());

        response.setMessage("Inicio de sesión exitoso");
        response.setJwt(jwt);
        return response;
    }

    /**
     * este metodo es para refrescar el token, se le pasa el token actual y se devuelve un nuevo token con una nueva fecha de expiracion
     * @param token el token actual que se quiere refrescar
     * @return un nuevo token con una nueva fecha de expiracion
     */
    public RefreshTokenResponseDTO refreshToken(String token) {
        String jwt = jwtService.refreshToken(token);
        RefreshTokenResponseDTO response = new RefreshTokenResponseDTO();
        response.setMessage("ok");
        response.setJwt(jwt);
        return response;
    }
}