package com.proyecto.TurboMechanics.entity;

public enum RolType {
    CLIENTE(1L),
    MECANICO(2L),
    ADMIN(3L);  // ajusta según tus roles

    private final Long id;

    RolType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
