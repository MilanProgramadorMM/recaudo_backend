
package com.recaudo.api.infrastructure.adapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recaudo.api.domain.model.dto.response.MenuDto;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import com.recaudo.api.infrastructure.repository.UserPermissionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModuleAdapter {

    private final ObjectMapper objectMapper;
    private final UserPermissionRepository userPermissionRepository;
    private final JwtUtil jwtService;

    public ModuleAdapter(ObjectMapper objectMapper, UserPermissionRepository userPermissionRepository, JwtUtil jwtService) {
        this.objectMapper = objectMapper;
        this.userPermissionRepository = userPermissionRepository;
        this.jwtService = jwtService;
    }

    public List<MenuDto> getMenuItems(String token) {
        token = token.substring(7);
        Long userId = jwtService.getClaimFromToken(token, "userId", Long.class);
        String menu = userPermissionRepository.findEnabledPermissionsByUserId(userId);
        List<MenuDto> menuItems = new ArrayList<>();
        try {
            JsonNode data = objectMapper.readTree(menu);
            data.valueStream().forEach(parent -> {
                menuItems.add(
                        MenuDto.builder()
                                .key(parent.get("key").asText())
                                .label(parent.get("label").asText())
                                .isTitle(parent.get("isTitle").asBoolean())
                                .build()
                );
                parent.get("children").valueStream().forEach(module -> {
                    List<MenuDto> moduleItemChildren = new ArrayList<>();
                    module.get("children").valueStream().forEach(child -> {
                        moduleItemChildren.add(
                                MenuDto.builder()
                                        .key(child.get("key").asText())
                                        .label(child.get("label").asText())
                                        .url(child.get("url").asText())
                                        .parentKey(child.get("parentKey").asText())
                                        .build()
                        );
                    });
                    menuItems.add(
                            MenuDto.builder()
                                    .key(module.get("key").asText())
                                    .label(module.get("label").asText())
                                    .icon(module.get("icon").asText())
                                    .collapsed(true)
                                    .url(module.get("url").asText())
                                    .children(!moduleItemChildren.isEmpty() ? moduleItemChildren : null)
                                    .build()
                    );
                });
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return menuItems;
    }

    private String slugify(String text) {
        return text.toLowerCase().replaceAll("\\s+", "-");
    }

}












