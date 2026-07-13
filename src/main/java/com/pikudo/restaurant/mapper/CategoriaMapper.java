package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.categoria.CategoriaResponseDTO;
import com.pikudo.restaurant.entity.Categoria;
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