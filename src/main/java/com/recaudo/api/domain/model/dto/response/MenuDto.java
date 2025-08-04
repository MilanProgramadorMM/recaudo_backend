package com.recaudo.api.domain.model.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuDto {
    private String key;
    private String label;
    private Boolean isTitle;
    private String icon;
    private Boolean collapsed;
    private String url;
    private String parentKey;
    List<MenuDto> children;
}
