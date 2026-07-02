package com.pikudo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresa {

    @Id
    private Long id; // Mantendremos este ID siempre en 1, ya que solo hay una empresa

    private String razonSocial; // Ej: "PIKUDO CHICKEN S.A.C."
    
    private String ruc;         // Ej: "20601695597"
    
    private String direccion;   // Ej: "AV. JUAN PABLO FERNANDINI 1092 BREÑA"
    
    private String telefono;    // Ej: "980082265"
}