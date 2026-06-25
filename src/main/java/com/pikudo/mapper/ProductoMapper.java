package com.pikudo.mapper;

import com.pikudo.dto.producto.ProductoRequestDTO;
import com.pikudo.dto.producto.ProductoResponseDTO;
import com.pikudo.entity.Producto;
import com.pikudo.entity.Categoria;
import org.springframework.stereotype.Component;

@Component

public class ProductoMapper {
    /*
     Traduce el RequestDTO (Frontend) a la Entidad (Base de Datos).
     Requiere el objeto Categoria previamente recuperado de la base de datos mediante su ID.
     */
    public Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        if (dto == null) {
            return null;
        }

        return Producto.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .categoria(categoria)
                .build();
    }

    /*
     Traduce la Entidad (Base de Datos) al ResponseDTO (Frontend).
     Mapea el nombre de la categoría de forma segura para evitar excepciones de puntero nulo.
     */
    public ProductoResponseDTO toResponseDTO(Producto entity) {
        if (entity == null) {
            return null;
        }

        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(entity.getId());
        response.setNombre(entity.getNombre());
        response.setPrecio(entity.getPrecio());
        response.setStock(entity.getStock());
        response.setEstado(entity.getEstado());

        // Controlamos de forma segura si el producto tiene asignada una categoría
        if (entity.getCategoria() != null) {
            response.setCategoriaNombre(entity.getCategoria().getNombre());
        }

        return response;
    }
}
