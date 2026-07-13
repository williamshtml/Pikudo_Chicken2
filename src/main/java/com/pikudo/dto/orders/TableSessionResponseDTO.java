package com.pikudo.dto.orders;

import com.pikudo.entity.orders.TableSessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TableSessionResponseDTO {

    private Long id;
    private Long mesaId;
    private Integer mesaNumero;
    private TableSessionStatus status;
    private Integer guestCount;
    private String notes;
    private String openedByUsername;
    private String closedByUsername;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
}
