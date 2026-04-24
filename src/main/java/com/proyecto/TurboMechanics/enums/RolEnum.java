package com.proyecto.TurboMechanics.enums;

public enum RolEnum {
    CLIENTE(1L),
    MECANICO(2L),
    ADMIN(3L);

    /**
     * Identificador único del rol, utilizado para asignar roles a los usuarios y verificar permisos.
     */
    private final Long id;

    RolEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
