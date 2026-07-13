package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.rol.RolResponseDTO;
import com.pikudo.restaurant.mapper.RolMapper;
import com.pikudo.restaurant.repository.RolRepository;
import com.pikudo.restaurant.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    public List<RolResponseDTO> listarTodos() {
        return rolRepository.findAll().stream()
                .map(rolMapper::toDTO)
                .collect(Collectors.toList());
    }
}