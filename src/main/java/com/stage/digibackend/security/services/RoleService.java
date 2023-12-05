package com.stage.digibackend.security.services;

import com.stage.digibackend.Collections.ERole;
import com.stage.digibackend.Collections.Role;
import com.stage.digibackend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;

    public void addRole(String role){
        Role r = new Role();
        r.setName(ERole.valueOf(role));
        roleRepository.save(r);
    }
}
