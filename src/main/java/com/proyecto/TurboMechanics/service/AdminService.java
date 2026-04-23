package com.proyecto.TurboMechanics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.UserRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.entity.RolType;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
 
    private final UsersRepository usersRepository;
 
    /**
     * extracts all customer information
     * the password never is expon in teh DTO.
     * @return all the users with rol CLIENT.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllClients() {
        List<Users> clients = usersRepository.findByRolId(RolType.CLIENTE.getId());
        return clients.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Extract all customer information by ID
     * lance RuntimeException yes not exist o yes not is CLIENT.
     * @param id parameter is id the client
     * @return return a client for your ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getClientById(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
 
        if (!user.getRolId().equals(RolType.CLIENTE.getId())) {
            throw new RuntimeException("El usuario con id " + id + " no es un cliente");
        }
 
        return mapToDTO(user);
    }

    /**
     * update customer data username, identification, phone and email. omits the password
     * @param id id the client to update
     * @param request DTO with the new values: username, identification, phone, email
     * @return {@link UserResponseDTO} with the data already update
     */
    @Transactional
    public UserResponseDTO updateClient(Long id, UserRequestDTO request) {
 
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
 
        if (!user.getRolId().equals(RolType.CLIENTE.getId())) {
            throw new RuntimeException("the user with id " + id + " not is a client");
        }
 
        // Validar que el nuevo email no esté en uso por otro usuario
        usersRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("the email " + request.getEmail() + " It is already in use");
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
     * Permanently removes a customer from the database.
     *
     * @param id ID of cliente to delete
     * @throws RuntimeException yes he cliente not exists or not is CLIENTE
     */
    @Transactional
    public void deleteClient(Long id) {
        Users user = findClient(id);
        usersRepository.delete(user);
    }
 
 
    /**
     * Search for a user by ID and validate that they have the CLIENT role.
     * Centralize the validation to avoid repeating it in each method.
     *
     * @param id ID of user the search
     * @return entity {@link Users} valid
     * @throws RuntimeException yes not exists or not is CLIENTE
     */
    private Users findClient(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
 
        if (!user.getRolId().equals(RolType.CLIENTE.getId())) {
            throw new RuntimeException("El usuario con id " + id + " no es un cliente");
        }
 
        return user;
    }

    /**
     * It obtains all the information from the userResponseDTO, omitting the password.
     * @param user entity {@link Users} get from the database, it should not be {@code null}
     * @return {@link UserResponseDTO} with the user's public fields: 
    * {@code id}, {@code username}, {@code identification}, 
    * {@code phone}, {@code email} and {@code roleId}
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
