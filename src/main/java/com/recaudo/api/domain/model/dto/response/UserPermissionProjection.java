package com.recaudo.api.domain.model.dto.response;

public interface UserPermissionProjection {
    Long getId();
    Long getModuleId();
    String getModulo();
    Long getActionId();
    String getAccion();
    Integer getPermiso();
}
