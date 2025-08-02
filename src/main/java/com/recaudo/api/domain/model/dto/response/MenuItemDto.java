package com.recaudo.api.domain.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuItemDto {
    private String key;
    private String label;
    private String icon;       // solo en padres
    private Boolean collapsed; // solo en padres
    private String url;        // solo en hijos
    private String parentKey;  // solo en hijos
    private List<MenuItemDto> children; // solo en padres
}
