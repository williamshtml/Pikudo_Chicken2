package com.pikudo.restaurant.service.orders;

import com.pikudo.restaurant.dto.orders.TableSessionOpenRequestDTO;
import com.pikudo.restaurant.dto.orders.TableSessionResponseDTO;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.orders.TableSession;

import java.util.List;

public interface TableSessionService {

    List<TableSessionResponseDTO> listCurrent();

    TableSessionResponseDTO open(Long mesaId, TableSessionOpenRequestDTO request);

    TableSessionResponseDTO close(Long sessionId);

    TableSession ensureOpenSession(Long mesaId, Long sessionId, Usuario openedBy);

    void closeIfNoOpenOrders(TableSession tableSession, Usuario closedBy);
}
