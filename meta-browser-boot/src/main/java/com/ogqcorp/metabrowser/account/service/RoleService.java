package com.ogqcorp.metabrowser.account.service;

import com.ogqcorp.metabrowser.account.dto.RoleDTO;
import com.ogqcorp.metabrowser.account.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    public RoleDTO findRoleById(Integer id){

        return roleRepository.findById(id).map(p -> new RoleDTO(p)).orElse(new RoleDTO());
    }
}
