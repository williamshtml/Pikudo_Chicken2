package com.pikudo.service.orders;

import com.pikudo.dto.orders.TableSessionOpenRequestDTO;
import com.pikudo.dto.orders.TableSessionResponseDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.orders.TableSession;

import java.util.List;

public interface TableSessionService {

    List<TableSessionResponseDTO> listCurrent();

    TableSessionResponseDTO open(Long mesaId, TableSessionOpenRequestDTO request);

    TableSessionResponseDTO close(Long sessionId);

    TableSession ensureOpenSession(Long mesaId, Long sessionId, Usuario openedBy);

    void closeIfNoOpenOrders(TableSession tableSession, Usuario closedBy);
}
