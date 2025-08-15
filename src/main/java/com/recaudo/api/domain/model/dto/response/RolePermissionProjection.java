package com.recaudo.api.domain.model.dto.response;

public interface RolePermissionProjection {
    Long getModuleId();
    String getModuleName();
    Long getActionId();
    String getActionName();
    Boolean getAllow();
}
