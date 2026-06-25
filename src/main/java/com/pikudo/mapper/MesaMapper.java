package com.pikudo.mapper;

import com.pikudo.dto.mesa.MesaRequestDTO;
import com.pikudo.dto.mesa.MesaResponseDTO;
import com.pikudo.entity.Mesa;
import org.springframework.stereotype.Component;

@Component

public class MesaMapper {
    /*
     Traduce el RequestDTO (Frontend) a la Entidad (Base de Datos).
     El estado inicial no viene en el DTO, por lo que la entidad usará su valor por defecto (true).
     */
    public Mesa toEntity(MesaRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return Mesa.builder()
                .numero(dto.getNumero())
                .capacidad(dto.getCapacidad())
                .build();
    }

    /*
     Traduce la Entidad (Base de Datos) al ResponseDTO (Frontend).
     Aquí traducimos el Boolean 'estado' a un String comprensible para la interfaz gráfica.
     */
    public MesaResponseDTO toResponseDTO(Mesa entity) {
        if (entity == null) {
            return null;
        }

        MesaResponseDTO response = new MesaResponseDTO();
        response.setId(entity.getId());
        response.setNumero(entity.getNumero());
        response.setCapacidad(entity.getCapacidad());
        
        // Traducción dinámica del estado booleano a texto descriptivo
        if (entity.getEstado() != null) {
            response.setEstado(entity.getEstado() ? "DISPONIBLE" : "OCUPADA");
        } else {
            response.setEstado("MANTENIMIENTO"); // Por si acaso el estado llega nulo
        }

        return response;
    }
}
