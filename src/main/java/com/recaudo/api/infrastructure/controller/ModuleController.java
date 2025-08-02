package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.MenuItemDto;
import com.recaudo.api.infrastructure.adapter.ModuleAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/modules")
public class ModuleController {

    @Autowired
    private ModuleAdapter moduleAdapter;

    @GetMapping("/tree")
    public ResponseEntity<List<MenuItemDto>> getMenuTree(@RequestHeader("Authorization") String token) {
        List<MenuItemDto> menu = moduleAdapter.getMenuItems(token);
        return ResponseEntity.ok(menu);
    }

}
