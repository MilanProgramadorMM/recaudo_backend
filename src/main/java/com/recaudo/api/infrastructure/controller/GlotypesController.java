package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.GlotypesProjection;
import com.recaudo.api.infrastructure.adapter.GlotypesAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/glotypes")
public class GlotypesController {

    @Autowired
    private GlotypesAdapter glotypesAdapter;


    @GetMapping("/{key}")
    public List<GlotypesProjection> getByKey(@PathVariable String key) {
        return glotypesAdapter.getTipDoc(key);
    }

}
