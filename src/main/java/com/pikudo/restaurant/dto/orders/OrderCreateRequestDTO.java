package com.pikudo.restaurant.dto.orders;

import com.pikudo.restaurant.entity.orders.OrderServiceType;
import com.pikudo.restaurant.entity.orders.OrderSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderCreateRequestDTO {

    private Long mesaId;
    private Long tableSessionId;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private OrderServiceType serviceType;

    private OrderSource source = OrderSource.DINE_IN;

    private String direccion;
    private String telefonoCliente;
    private String observacionesPedido;

    @Valid
    @NotEmpty(message = "El pedido debe tener al menos un item")
    private List<Item> items = new ArrayList<>();

    @Getter
    @Setter
    public static class Item {

        @NotNull(message = "La variante es obligatoria")
        private Long variantId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer quantity;

        @Size(max = 250, message = "Las notas no pueden superar los 250 caracteres")
        private String notes;

        @Valid
        private List<SelectedModifier> modifiers = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class SelectedModifier {

        @NotNull(message = "El modificador es obligatorio")
        private Long modifierId;

        @Min(value = 1, message = "La cantidad del modificador debe ser al menos 1")
        private Integer quantity = 1;
    }
}
