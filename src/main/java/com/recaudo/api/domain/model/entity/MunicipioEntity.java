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
@Table(name = "municipio")
public class MunicipioEntity {

    @Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value")
    private String value;

    @Column(name = "description")
    private String description;

    @Column(name = "id_departamento")
    private Long idDepartamento;

    @Column(name = "status")
    private Boolean status = true;
}
