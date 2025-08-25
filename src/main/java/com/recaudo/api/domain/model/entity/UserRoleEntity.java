package com.recaudo.api.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_role")
public class UserRoleEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Builder.Default
    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
