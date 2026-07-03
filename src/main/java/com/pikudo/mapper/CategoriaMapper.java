package com.pikudo.mapper;

import com.pikudo.dto.categoria.CategoriaResponseDTO;
import com.pikudo.entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaResponseDTO toDTO(Categoria c) {
        if (c == null) return null;
        
        return new CategoriaResponseDTO(
                c.getId(),
                c.getNombre(),
                null // Manteniendo el null para la descripción como tenías originalmente
        );
    }
}