package com.pikudo.restaurant.controller.orders;

import com.pikudo.restaurant.dto.orders.TableSessionOpenRequestDTO;
import com.pikudo.restaurant.dto.orders.TableSessionResponseDTO;
import com.pikudo.restaurant.service.orders.TableSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableSessionController {

    private final TableSessionService tableSessionService;

    @GetMapping("/sessions/current")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public List<TableSessionResponseDTO> currentSessions() {
        return tableSessionService.listCurrent();
    }

    @PostMapping("/{id}/sessions/open")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public TableSessionResponseDTO open(@PathVariable Long id,
                                        @Valid @RequestBody(required = false) TableSessionOpenRequestDTO request) {
        return tableSessionService.open(id, request != null ? request : new TableSessionOpenRequestDTO());
    }

    @PostMapping("/sessions/{id}/close")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MOZO')")
    public TableSessionResponseDTO close(@PathVariable Long id) {
        return tableSessionService.close(id);
    }
}
