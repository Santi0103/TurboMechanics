package com.proyecto.TurboMechanics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.UserRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
 
    private final UsersRepository usersRepository;
 
    /**
     * Obtiene toda la lista de clientes
     * @return todos los clientes registrados
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllClients() {
        List<Users> clients = usersRepository.findByRolId(RolEnum.CLIENTE.getId());
        return clients.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca el cliente por la identificacion
     * @param identification identificacion del cliente
     * @return retorna al cliente segun la identificacion
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getClientByIdentification(Integer identification) {
        Users user = usersRepository.findByIdentification(identification)
                .orElseThrow(() -> new RuntimeException("Cliente no fue encontrado con la identificacion: " + identification));
 
        if (!user.getRolId().equals(RolEnum.CLIENTE.getId())) {
            throw new RuntimeException("el usuario con identificacion " + identification + " no es un client");
        }
 
        return mapToDTO(user);
    }

    /**
     * Actualiza al cliente por medio de su identificacion
     * @param identification identificacion del cliente
     * @param request UserRequestDTO los nuevos datos
     * @return retorna el cliente con los datos actualizados
     */
    @Transactional
    public UserResponseDTO updateClient(Integer identification, UserRequestDTO request) {
        Users user = findClient(identification);

        usersRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getIdentification().equals(identification)) {
                throw new RuntimeException("el email " + request.getEmail() + " ya se encuentra en uso");
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
     * Eliminar el cliente en base a su identificacion
     * @param identification identificacion del cliente
     */
    @Transactional
    public void deleteClient(Integer identification) {
        Users user = findClient(identification);
        usersRepository.delete(user);
    }
 
 
    /**
     * Busca el cliente por identificacion
     * @param identification identificacion del cliente
     * @return retorna el cliente por identificacion
     */
    private Users findClient(Integer identification) {
        Users user = usersRepository.findByIdentification(identification)
                .orElseThrow(() -> new RuntimeException("Cliente no no fue cencntrado con identificacion: " + identification));
 
        if (!user.getRolId().equals(RolEnum.CLIENTE.getId())) {
            throw new RuntimeException("El usuario con identificacion " + identification + " no es un cliente");
        }
 
        return user;
    }

    /**
     * Mapea los datos del clienet
     * @param user entidad del cliente
     * @return retorna todos los datos exepto la contraseña
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
