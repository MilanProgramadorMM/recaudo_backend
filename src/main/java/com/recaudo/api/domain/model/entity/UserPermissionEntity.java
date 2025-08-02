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
@Table(name = "user_permission", schema = "public")
public class UserPermissionEntity implements Serializable {

    @Id
    @SequenceGenerator(name = "user_permission_seq", sequenceName = "user_permission_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_permission_seq")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "module_id")
    private Long moduleId;

    @Column(name = "allow")
    private Boolean allow;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
