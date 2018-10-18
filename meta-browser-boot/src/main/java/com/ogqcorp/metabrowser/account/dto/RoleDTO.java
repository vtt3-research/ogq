package com.ogqcorp.metabrowser.account.dto;

import com.ogqcorp.metabrowser.domain.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {
    private Integer id;
    private String role;

    public RoleDTO(Role role){
        this.id = role.getId();
        this.role = role.getRole();
    }

    public RoleDTO(){

    }
}
