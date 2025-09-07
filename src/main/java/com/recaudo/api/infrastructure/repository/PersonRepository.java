package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.PersonEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    List<PersonEntity> findByStatusTrue(Sort id);
    Optional<PersonEntity> findByDocument(String document);
    List<PersonEntity> findByTypePersonIdAndStatusTrue(Long typePersonId);

}
