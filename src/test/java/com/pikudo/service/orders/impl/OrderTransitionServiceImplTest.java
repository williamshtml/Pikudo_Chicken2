package com.pikudo.service.orders.impl;

import com.pikudo.entity.EstadoPedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.orders.OrderOperationalStatus;
import com.pikudo.entity.orders.OrderStatusHistory;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.orders.OrderStatusHistoryRepository;
import com.pikudo.service.orders.TableSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderTransitionServiceImplTest {

    private PedidoRepository pedidoRepository;
    private OrderStatusHistoryRepository historyRepository;
    private TableSessionService tableSessionService;
    private OrderTransitionServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        historyRepository = mock(OrderStatusHistoryRepository.class);
        tableSessionService = mock(TableSessionService.class);
        service = new OrderTransitionServiceImpl(pedidoRepository, historyRepository, tableSessionService);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void validTransitionUpdatesStatusAndRecordsHistory() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDING);
        pedido.setEstadoOperativo(OrderOperationalStatus.UNREAD);
        Usuario user = new Usuario();
        user.setId(9L);

        Pedido transitioned = service.transition(pedido, OrderOperationalStatus.READ, user, "Leido en cocina");

        assertThat(transitioned.getEstadoOperativo()).isEqualTo(OrderOperationalStatus.READ);
        assertThat(transitioned.getEstado()).isEqualTo(EstadoPedido.PENDING);
        verify(historyRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void invalidTransitionFails() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDING);
        pedido.setEstadoOperativo(OrderOperationalStatus.UNREAD);

        assertThatThrownBy(() -> service.transition(pedido, OrderOperationalStatus.READY, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transicion de pedido invalida");
    }
}
