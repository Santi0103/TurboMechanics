package com.proyecto.TurboMechanics.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.ChangepasswordRequestdto;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.UserProfileRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Retorna el perfil del usuario autenticado
     * @param request HttpServletRequest para extraer el userId del JWT
     * @return retorna los datos del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserResponseDTO profile = userProfileService.getMyProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Actualiza los datos del usuario autenticado
     * @param request HttpServletRequest para extraer el userId del JWT
     * @param body UserProfileRequestDTO con los nuevos datos del perfil
     * @return retorna los datos actualizados del usuario
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            HttpServletRequest request,
            @Valid @RequestBody UserProfileRequestDTO body) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserResponseDTO updated = userProfileService.updateMyProfile(userId, body);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Cambia la contrasena del usuario autenticado
     * @param request HttpServletRequest para extraer el userId del JWT
     * @param body ChangepasswordRequestdto con la contrasena actual y la nueva
     * @return retorna un mensaje de confirmacion
     */
    @PutMapping("/me/password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangepasswordRequestdto body) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            MessageResponseDTO response = userProfileService.changePassword(userId, body);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageResponseDTO err = new MessageResponseDTO();
            err.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}