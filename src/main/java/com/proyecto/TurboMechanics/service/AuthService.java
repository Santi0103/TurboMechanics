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
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.enums.RolEnum;
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
     * Registro del cliente
     * @param request RegisterRequestDTO datos para el registro
     * @return retorna un mensaje de registro exitoso
     */
   @Transactional
    public MessageResponseDTO register(@Valid RegisterRequestDTO request) {
        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Registrado correctamente");

        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya se encuentra en uso");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setIdentification(request.getIdentification());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRolId(RolEnum.CLIENTE.getId());

        usersRepository.save(user);

        response.setMessage("User successfully registered");
        return response;
    }

    /**
     * Inicio de sesion 
     * @param request LoginRequestDTO datos para el incio de sesion
     * @return retorna mensaje de inicio de seion correctamente
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        LoginResponseDTO response = new LoginResponseDTO();

        Optional<Users> user = usersRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            response.setMessage("El usuario no fue encontrado");
            return response;
        }

        Users userFound = user.get();

        if (!passwordEncoder.matches(request.getPassword(), userFound.getPassword())) {
            response.setMessage("contraseña incorrecta");
            return response;
        }

        String jwt = jwtService.generateToken(
            userFound.getId(),
            userFound.getUsername(),
            userFound.getRolId()
        );

        response.setMessage("inicio de sesion correctamente");
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