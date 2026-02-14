package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.GlotypesProjection;
import com.recaudo.api.domain.model.entity.GlotypesEntity;
import com.recaudo.api.infrastructure.repository.GlotypesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GlotypesAdapter {

    @Autowired
    private GlotypesRepository glotypesRepository;


    public List<GlotypesEntity> getByKey(String key) {
        return glotypesRepository.findByKey(key);
    }

    public Optional<GlotypesEntity> getByCodeAndKey(String key, String code) {
        return glotypesRepository.findByKeyAndCode(key, code);
    }
}
