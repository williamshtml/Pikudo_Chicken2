package com.pikudo.dto.producto;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer el id, nombre, precio, etc.
@Setter              // Genera los métodos para armar el DTO desde la base de datos
@NoArgsConstructor   // Constructor vacío () estándar exigido por Jackson
@AllArgsConstructor  // Constructor completo optimizado en una sola línea por Lombok
public class ProductoResponseDTO {

    private Long id;              // ID único del producto en el sistema
    private String nombre;        // Nombre del plato o bebida (ej: "1/4 de Pollo")
    private BigDecimal precio;    // Precio actual mapeado con precisión matemática
    private Integer stock;        // Cantidad de porciones/unidades disponibles
    private Boolean estado;       // Estado activo/inactivo (true = disponible en carta, false = oculto)
    private String categoriaNombre; // Nombre limpio de la categoría (ej: "Pollos", "Bebidas")
}