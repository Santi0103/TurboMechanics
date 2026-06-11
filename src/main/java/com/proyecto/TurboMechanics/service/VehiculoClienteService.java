package com.proyecto.TurboMechanics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.VehiculoClienteRequestDTO;
import com.proyecto.TurboMechanics.dto.VehiculoClienteResponseDTO;
import com.proyecto.TurboMechanics.entity.VehiculoCliente;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehiculoClienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehiculoClienteService {

    private final VehiculoClienteRepository vehiculoClienteRepository;
    private final UsersRepository usersRepository;

    /**
     * Registra un nuevo vehiculo para el cliente autenticado
     * @param usuarioId id del cliente extraido del JWT
     * @param request VehiculoClienteRequestDTO con los datos del vehiculo
     * @return retorna el vehiculo registrado
     */
    @Transactional
    public VehiculoClienteResponseDTO register(Long usuarioId, VehiculoClienteRequestDTO request) {
        if (vehiculoClienteRepository.existsByPlacaIgnoreCaseAndUsuarioId(request.getPlaca(), usuarioId)) {
            throw new RuntimeException("Ya tienes un vehículo registrado con la placa: " + request.getPlaca());
        }

        VehiculoCliente vehiculo = new VehiculoCliente();
        vehiculo.setUsuarioId(usuarioId);
        vehiculo.setPlaca(request.getPlaca().toUpperCase().trim());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setCilindraje(request.getCilindraje());

        return mapToDTO(vehiculoClienteRepository.save(vehiculo));
    }

    /**
     * Lista todos los vehiculos del cliente autenticado
     * @param usuarioId id del cliente extraido del JWT
     * @return retorna la lista de vehiculos del cliente
     */
    @Transactional(readOnly = true)
    public List<VehiculoClienteResponseDTO> listMyVehicles(Long usuarioId) {
        return vehiculoClienteRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un vehiculo del cliente por su id
     * @param id id del vehiculo
     * @param usuarioId id del cliente extraido del JWT
     * @return retorna el vehiculo encontrado
     */
    @Transactional(readOnly = true)
    public VehiculoClienteResponseDTO getVehicle(Long id, Long usuarioId) {
        VehiculoCliente vehiculo = findByIdAndUsuario(id, usuarioId);
        return mapToDTO(vehiculo);
    }

    /**
     * Actualiza los datos de un vehiculo del cliente
     * @param id id del vehiculo a actualizar
     * @param usuarioId id del cliente extraido del JWT
     * @param request VehiculoClienteRequestDTO con los nuevos datos
     * @return retorna el vehiculo actualizado
     */
    @Transactional
    public VehiculoClienteResponseDTO update(Long id, Long usuarioId, VehiculoClienteRequestDTO request) {
        VehiculoCliente vehiculo = findByIdAndUsuario(id, usuarioId);

        vehiculoClienteRepository.findByPlacaIgnoreCase(request.getPlaca()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Ya existe un vehículo con la placa: " + request.getPlaca());
            }
        });

        vehiculo.setPlaca(request.getPlaca().toUpperCase().trim());
        vehiculo.setMarca(request.getMarca());
        vehiculo.setModelo(request.getModelo());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setCilindraje(request.getCilindraje());

        return mapToDTO(vehiculoClienteRepository.save(vehiculo));
    }

    /**
     * Elimina un vehiculo del cliente
     * @param id id del vehiculo a eliminar
     * @param usuarioId id del cliente extraido del JWT
     */
    @Transactional
    public void delete(Long id, Long usuarioId) {
        VehiculoCliente vehiculo = findByIdAndUsuario(id, usuarioId);
        vehiculoClienteRepository.delete(vehiculo);
    }

    /**
     * Lista todos los vehiculos registrados por los clientes (solo admin)
     * @return retorna la lista completa de vehiculos
     */
    @Transactional(readOnly = true)
    public List<VehiculoClienteResponseDTO> listAll() {
        return vehiculoClienteRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lista los vehiculos registrados por un cliente especifico (solo admin)
     * @param usuarioId id del usuario cliente
     * @return retorna la lista de vehiculos del cliente
     */
    @Transactional(readOnly = true)
    public List<VehiculoClienteResponseDTO> listByUsuario(Long usuarioId) {
        return vehiculoClienteRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un vehiculo por id verificando que pertenezca al cliente
     * @param id id del vehiculo
     * @param usuarioId id del cliente
     * @return retorna el vehiculo encontrado
     */
    private VehiculoCliente findByIdAndUsuario(Long id, Long usuarioId) {
        VehiculoCliente vehiculo = vehiculoClienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con id: " + id));
        if (!vehiculo.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para acceder a este vehículo");
        }
        return vehiculo;
    }

    /**
     * Mapea la entidad VehiculoCliente al DTO de respuesta
     * @param vehiculo entidad del vehiculo
     * @return retorna el VehiculoClienteResponseDTO
     */
    private VehiculoClienteResponseDTO mapToDTO(VehiculoCliente vehiculo) {
        VehiculoClienteResponseDTO dto = new VehiculoClienteResponseDTO();
        dto.setId(vehiculo.getId());
        dto.setUsuarioId(vehiculo.getUsuarioId());
        dto.setPlaca(vehiculo.getPlaca());
        dto.setMarca(vehiculo.getMarca());
        dto.setModelo(vehiculo.getModelo());
        dto.setAnio(vehiculo.getAnio());
        dto.setColor(vehiculo.getColor());
        dto.setTipo(vehiculo.getTipo());
        dto.setCilindraje(vehiculo.getCilindraje());
        dto.setFechaRegistro(vehiculo.getFechaRegistro());
        usersRepository.findById(vehiculo.getUsuarioId()).ifPresent(user ->
            dto.setNombreUsuario(user.getUsername())
        );
        return dto;
    }
}