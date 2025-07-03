package com.davivienda.kata.infrastructure.adapter;

import com.davivienda.kata.domain.model.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private UserEntity user;

    public UserDetailsImpl(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUser();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }

    public Long getId() {
        return user.getId();
    }

    public UserEntity getUserEntity() {
        return user;
    }
}
