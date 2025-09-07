package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ContactInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfoEntity, Long> {

    List<ContactInfoEntity> findByPerson(Long personId);


    boolean existsByPersonAndTypeAndIdNot(Long personId, Long typeId, Long excludeId);

    boolean existsByValueAndTypeAndIdNot(String value, Long typeId, Long excludeId);

    List<ContactInfoEntity> findByPersonOrderByIdDesc(Long personId);


}
