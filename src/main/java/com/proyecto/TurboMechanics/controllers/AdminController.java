package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.UserRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.entity.RolType;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
 
    /**
     * Returns the list of all customers registered in the system.
     * Only accessible by users with the {@code ADMIN} role.
     * @return {@code 200 OK} with the list of {@link UserResponseDTO},
     * or {@code 500 INTERNAL SERVER ERROR} if an unexpected error occurs
     */
    @RequiresRole({RolType.ADMIN, RolType.MECANICO})
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllClients() {
        try {
            List<UserResponseDTO> clients = adminService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
 
    /**
    * Returns information for a specific client by their ID.
    * Only accessible to users with the {@code ADMIN} role.
    * @param id Unique identifier of the client to query
    * @return {@code 200 OK} with the {@link UserResponseDTO} of the found client,
    * or {@code 404 NOT FOUND} if the client does not exist or does not have the CLIENT role
    */
    @RequiresRole({RolType.ADMIN})
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getClientById(@PathVariable Long id) {
        try {
            UserResponseDTO client = adminService.getClientById(id);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
    * Updates the personal data of an existing customer.
    *
    * Only accessible by users with the {@code ADMIN} role. The fields
    * that can be updated are: {@code username}, {@code identification},
    * {@code phone}, and {@code email}.
    *
    * @param id: Unique identifier of the customer to be updated
    * @param request: Request body with the new customer data;
    * Automatically validated by {@code @Valid}
    * @return {@code 200 OK} with the updated {@link UserResponseDTO},
    * or {@code 400 BAD REQUEST} if the customer does not exist, is not a CUSTOMER,
    * or the email address is already in use
    */
    @RequiresRole({RolType.ADMIN, RolType.MECANICO})
    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> updateClient(@PathVariable Long id, @Valid @RequestBody UserRequestDTO request) {
        try {
            UserResponseDTO updated = adminService.updateClient(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
    * Permanently deletes a client from the database.
    * Only accessible by users with the {@code ADMIN} role. This action
    * is irreversible; if history needs to be preserved, consider
    * a soft delete instead.
    * @param id Unique identifier of the client to be deleted
    * @return {@code 200 OK} with confirmation message,
    * or {@code 400 BAD REQUEST} if the client does not exist or is not a CLIENT
    */
    @RequiresRole({RolType.ADMIN})
    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponseDTO> deleteClient(@PathVariable Long id) {
        try {
            adminService.deleteClient(id);
            MessageResponseDTO response = new MessageResponseDTO();
            response.setMessage("client delete correctly");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

}
