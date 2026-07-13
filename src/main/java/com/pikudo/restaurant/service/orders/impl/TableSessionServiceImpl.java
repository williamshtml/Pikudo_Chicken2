package com.pikudo.restaurant.service.orders.impl;

import com.pikudo.restaurant.dto.orders.TableSessionOpenRequestDTO;
import com.pikudo.restaurant.dto.orders.TableSessionResponseDTO;
import com.pikudo.restaurant.entity.Mesa;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.TableSession;
import com.pikudo.restaurant.entity.orders.TableSessionStatus;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.orders.OrderResponseMapper;
import com.pikudo.restaurant.repository.MesaRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.orders.TableSessionRepository;
import com.pikudo.restaurant.service.orders.TableSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableSessionServiceImpl implements TableSessionService {

    private static final List<OrderOperationalStatus> TERMINAL_STATUSES = List.of(
            OrderOperationalStatus.REJECTED,
            OrderOperationalStatus.DELIVERED,
            OrderOperationalStatus.CANCELLED
    );

    private final TableSessionRepository tableSessionRepository;
    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrderResponseMapper mapper;

    @Override
    public List<TableSessionResponseDTO> listCurrent() {
        return tableSessionRepository.findByStatusOrderByOpenedAtDesc(TableSessionStatus.OPEN).stream()
                .map(mapper::toTableSessionResponse)
                .toList();
    }

    @Override
    @Transactional
    public TableSessionResponseDTO open(Long mesaId, TableSessionOpenRequestDTO request) {
        Usuario user = currentUser();
        TableSession session = createOpenSession(mesaId, request, user);
        return mapper.toTableSessionResponse(session);
    }

    @Override
    @Transactional
    public TableSessionResponseDTO close(Long sessionId) {
        Usuario user = currentUser();
        TableSession session = tableSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesion de mesa no encontrada: " + sessionId));
        if (session.getStatus() == TableSessionStatus.CLOSED) {
            return mapper.toTableSessionResponse(session);
        }
        if (pedidoRepository.existsByTableSessionIdAndEstadoOperativoNotIn(sessionId, TERMINAL_STATUSES)) {
            throw new BusinessException("No se puede cerrar la sesion: aun tiene pedidos abiertos.");
        }
        closeSession(session, user);
        return mapper.toTableSessionResponse(tableSessionRepository.save(session));
    }

    @Override
    @Transactional
    public TableSession ensureOpenSession(Long mesaId, Long sessionId, Usuario openedBy) {
        if (sessionId != null) {
            TableSession session = tableSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sesion de mesa no encontrada: " + sessionId));
            if (session.getStatus() != TableSessionStatus.OPEN) {
                throw new BusinessException("La sesion de mesa indicada ya esta cerrada.");
            }
            if (mesaId != null && !session.getMesa().getId().equals(mesaId)) {
                throw new BusinessException("La sesion indicada no pertenece a la mesa enviada.");
            }
            return session;
        }
        if (mesaId == null) {
            throw new BusinessException("Los pedidos de mesa requieren mesaId o tableSessionId.");
        }
        return tableSessionRepository.findFirstByMesaIdAndStatusOrderByOpenedAtDesc(mesaId, TableSessionStatus.OPEN)
                .orElseGet(() -> createOpenSession(mesaId, new TableSessionOpenRequestDTO(), openedBy));
    }

    @Override
    @Transactional
    public void closeIfNoOpenOrders(TableSession tableSession, Usuario closedBy) {
        if (tableSession == null || tableSession.getStatus() == TableSessionStatus.CLOSED) {
            return;
        }
        if (!pedidoRepository.existsByTableSessionIdAndEstadoOperativoNotIn(tableSession.getId(), TERMINAL_STATUSES)) {
            closeSession(tableSession, closedBy);
            tableSessionRepository.save(tableSession);
        }
    }

    private TableSession createOpenSession(Long mesaId, TableSessionOpenRequestDTO request, Usuario openedBy) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada: " + mesaId));
        if (Boolean.FALSE.equals(mesa.getEstado())) {
            throw new BusinessException("La mesa indicada esta desactivada.");
        }
        if (tableSessionRepository.existsByMesaIdAndStatus(mesaId, TableSessionStatus.OPEN)) {
            throw new BusinessException("La mesa ya tiene una sesion abierta.");
        }
        TableSession session = TableSession.builder()
                .mesa(mesa)
                .openedBy(openedBy)
                .guestCount(request.getGuestCount())
                .notes(request.getNotes())
                .status(TableSessionStatus.OPEN)
                .openedAt(LocalDateTime.now())
                .build();
        return tableSessionRepository.save(session);
    }

    private void closeSession(TableSession session, Usuario closedBy) {
        session.setStatus(TableSessionStatus.CLOSED);
        session.setClosedBy(closedBy);
        session.setClosedAt(LocalDateTime.now());
    }

    private Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("No hay usuario autenticado.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesion no encontrado"));
    }
}
