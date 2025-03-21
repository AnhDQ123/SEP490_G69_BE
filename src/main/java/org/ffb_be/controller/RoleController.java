package org.ffb_be.controller;

import org.ffb_be.dto.auth.role.RoleDTO;
import org.ffb_be.entity.Role;
import org.ffb_be.repository.RoleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Role> role = roleRepository.findAll();
        List<RoleDTO> dtos = new ArrayList<>();
        for (Role role1 : role) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(role1.getId());
            roleDTO.setName(role1.getName());
            dtos.add(roleDTO);
        }
        return ResponseEntity.ok(dtos);
    }
}
