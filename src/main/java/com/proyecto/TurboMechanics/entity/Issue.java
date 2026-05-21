package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.StatusIssue;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asunto")
public class Issue {

    /**id del asunto */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Orden de trabajo a la que pertenece el inconveniente */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    /** Descripción del inconveniente reportado por el mecánico */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Estado: Open o Closed */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusIssue status;

    /** Fecha y hora en que se registró */
    @Column(nullable = false)
    private LocalDateTime reportedAt;

    /** Fecha y hora en que se cerró */
    @Column
    private LocalDateTime closedAt;

    /** Mecánico que reportó el inconveniente */
    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) reportedAt = LocalDateTime.now();
        if (status == null)     status     = StatusIssue.Open;
    }
}