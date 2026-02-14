package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.entity.GlotypesEntity;
import com.recaudo.api.infrastructure.adapter.GlotypesAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/glotypes")
public class GlotypesController {

    @Autowired
    private GlotypesAdapter glotypesAdapter;


    @GetMapping("/{key}")
    public List<GlotypesEntity> getByKey(@PathVariable String key) {
        return glotypesAdapter.getByKey(key);
    }

}
