package com.pikudo.service;

import com.pikudo.entity.Rol;
import com.pikudo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {
    
    private final RolRepository rolRepository;

    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }
}