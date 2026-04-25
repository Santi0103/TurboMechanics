package com.proyecto.TurboMechanics.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.LoginRequestDTO;
import com.proyecto.TurboMechanics.dto.LoginResponseDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.RefreshTokenResponseDTO;
import com.proyecto.TurboMechanics.dto.RegisterRequestDTO;
import com.proyecto.TurboMechanics.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
    /**
     * Registrar el cliente  
     * @param request RegisterRequestDTO datos que le pide al cliente para registrarse
     * @return retorna MessageResponseDTO indicando que el cliente fue creado
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            MessageResponseDTO response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Iniciar sesion usuario
     * @param request LoginRequestDTO datos para iniciar sesion
     * @return los datos del incio de sesion
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = authService.login(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            LoginResponseDTO error = new LoginResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Refresca el token
     * @param request requiere el token viejo
     * @return retorna un token nuevo
     */
    @GetMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(HttpServletRequest request) {
        String autheader = request.getHeader("Authorization");
        if (autheader == null || !autheader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String token = autheader.substring(7);

        try {
            RefreshTokenResponseDTO response = authService.refreshToken(token);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}