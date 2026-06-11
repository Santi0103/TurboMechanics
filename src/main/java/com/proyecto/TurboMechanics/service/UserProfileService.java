package com.proyecto.TurboMechanics.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.ChangepasswordRequestdto;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.UserProfileRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retorna el perfil del usuario autenticado
     * @param userId id del usuario extraido del JWT
     * @return retorna los datos del usuario autenticado
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getMyProfile(Long userId) {
        Users user = findById(userId);
        return mapToDTO(user);
    }

    /**
     * Actualiza los datos del usuario autenticado
     * @param userId id del usuario extraido del JWT
     * @param request UserProfileRequestDTO con los nuevos datos
     * @return retorna los datos actualizados del usuario
     */
    @Transactional
    public UserResponseDTO updateMyProfile(Long userId, UserProfileRequestDTO request) {
        Users user = findById(userId);

        usersRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new RuntimeException("El correo " + request.getEmail() + " ya está en uso");
            }
        });

        user.setUsername(request.getUsername());
        user.setIdentification(request.getIdentification());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());

        usersRepository.save(user);
        return mapToDTO(user);
    }

    /**
     * Cambia la contrasena del usuario autenticado
     * @param userId id del usuario extraido del JWT
     * @param request ChangePasswordRequestDTO con la contrasena actual y la nueva
     * @return retorna un mensaje de confirmacion
     */
    @Transactional
    public MessageResponseDTO changePassword(Long userId, ChangepasswordRequestdto request) {
        Users user = findById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Contraseña actualizada correctamente");
        return response;
    }

    /**
     * Busca el usuario por su id
     * @param userId id del usuario
     * @return retorna el usuario encontrado
     */
    private Users findById(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));
    }

    /**
     * Mapea la entidad Users al DTO de respuesta
     * @param user entidad del usuario
     * @return retorna el UserResponseDTO sin la contrasena
     */
    private UserResponseDTO mapToDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setIdentification(user.getIdentification());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRolId(user.getRolId());
        return dto;
    }
}