package com.recaudo.api.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "departamento")
public class DepartamentoEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "value")
        private String value;

        @Column(name = "description")
        private String description;

        @Column(name = "id_pais", nullable = false)
        private Long idPais;

        @Column(name = "status")
        private Boolean status = true;

        @Column(name = "code")
        private Long code;
}
