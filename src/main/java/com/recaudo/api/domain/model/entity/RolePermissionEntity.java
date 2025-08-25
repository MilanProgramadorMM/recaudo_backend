package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role_permission")
public class RolePermissionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "allow")
    private Boolean allow;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
