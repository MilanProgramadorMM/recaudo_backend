package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.GlotypesProjection;
import com.recaudo.api.infrastructure.repository.GlotypesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlotypesAdapter {

    @Autowired
    private GlotypesRepository glotypesRepository;


    public List<GlotypesProjection> getTipDoc(String key) {
        return glotypesRepository.findByKey(key);
    }
}
