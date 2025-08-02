package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.MenuItemDto;
import com.recaudo.api.domain.model.entity.ModuleEntity;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import com.recaudo.api.infrastructure.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModuleAdapter {

    private final ModuleRepository moduleRepository;
    private final JwtUtil jwtService;

    public ModuleAdapter(ModuleRepository moduleRepository, JwtUtil jwtService) {
        this.moduleRepository = moduleRepository;
        this.jwtService = jwtService;
    }

    public List<MenuItemDto> getMenuItems(String token) {
        Long userId = jwtService.getClaimFromToken(token, "userId", Long.class);
        List<ModuleEntity> allModules = moduleRepository.findByStatus("ACTIVO");

        List<ModuleEntity> parents = allModules.stream()
                .filter(m -> m.getType() == 1)
                .toList();

        List<MenuItemDto> menuItems = new ArrayList<>();

        for (ModuleEntity parent : parents) {
            String parentKey = slugify(parent.getName());
            MenuItemDto parentItem = new MenuItemDto();
            parentItem.setKey(parentKey);
            parentItem.setLabel(parent.getName());
            parentItem.setIcon("ti-folder"); // o lo que desees
            parentItem.setCollapsed(true);

            List<MenuItemDto> children = allModules.stream()
                    .filter(m -> m.getType() == 2 && m.getParent() != null && m.getParent().equals(parent.getId()))
                    .map(child -> new MenuItemDto(
                            slugify(parentKey + "-" + child.getName()),
                            child.getName(),
                            null,
                            null,
                            "/" + parentKey + "/" + slugify(child.getName()), // ← aquí se genera la url
                            parentKey,
                            null
                    ))
                    .toList();

            parentItem.setChildren(children);
            menuItems.add(parentItem);
        }

        return menuItems;
    }

    private String slugify(String text) {
        return text.toLowerCase().replaceAll("\\s+", "-");
    }

}
