package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String user);

    Optional<UserEntity> findByPersonId(Long personId);

    List<UserEntity> findByStatusTrue();



}
