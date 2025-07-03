package com.davivienda.kata.domain.gateway;

import com.davivienda.kata.domain.model.entity.UserEntity;

public interface UserGateway {
    public UserEntity getByUser(String user);
    public UserEntity save(UserEntity user);
}
