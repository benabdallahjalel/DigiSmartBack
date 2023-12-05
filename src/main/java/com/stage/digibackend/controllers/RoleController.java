package com.stage.digibackend.controllers;

import com.stage.digibackend.security.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    @Autowired
    RoleService roleService;
    @PostMapping("/addRole/{role}")
    public void addRole(@PathVariable("role") String role){
        roleService.addRole(role);
    }
    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
