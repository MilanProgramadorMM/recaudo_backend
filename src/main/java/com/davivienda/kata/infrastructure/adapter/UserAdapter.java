package com.davivienda.kata.infrastructure.adapter;

import com.davivienda.kata.domain.gateway.UserGateway;
import com.davivienda.kata.domain.model.entity.UserEntity;
import com.davivienda.kata.infrastructure.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAdapter implements UserGateway {

    UserRepository userRepository;

    @Override
    public UserEntity getByUser(String user) {
        return userRepository.findByUser(user).orElse(null);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
}
