package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.producto.ProductoRequestDTO;
import com.pikudo.restaurant.dto.producto.ProductoResponseDTO;
import com.pikudo.restaurant.entity.Categoria;
import com.pikudo.restaurant.entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toDTO(Producto p) {
        if (p == null) return null;
        
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(p.getId());
        response.setNombre(p.getNombre());
        response.setPrecio(p.getPrecio());
        response.setStock(p.getStock());
        response.setEstado(p.getEstado());
        response.setCategoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null);
        return response;
    }

    public Producto toEntity(ProductoRequestDTO dto, Categoria categoria) {
        if (dto == null) return null;
        
        return Producto.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .categoria(categoria)
                .estado(true) // Por defecto nuevo producto activo
                .build();
    }
}