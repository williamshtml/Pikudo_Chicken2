package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.mesa.MesaResponseDTO;
import com.pikudo.restaurant.entity.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaMapper {

    public MesaResponseDTO toDTO(Mesa m) {
        if (m == null) return null;

        return new MesaResponseDTO(
                m.getId(),
                m.getNumero(),
                m.getCapacidad(),
                Boolean.TRUE.equals(m.getEstado()) ? "DISPONIBLE" : "INACTIVA"
        );
    }
}