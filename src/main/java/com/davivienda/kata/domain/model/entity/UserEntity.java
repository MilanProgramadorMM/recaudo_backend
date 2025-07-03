package com.davivienda.kata.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usr", schema = "public")
public class UserEntity implements Serializable {

    private static final long serialVersionUID = -4768950189607025639L;

    @Id
    @SequenceGenerator(name = "usr_id_seq", sequenceName = "usr_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usr_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String user;

    @JsonIgnore
    @Column(name = "password", length = 330)
    private String password;

}