package com.pikudo.restaurant.service.orders.impl;

import com.pikudo.restaurant.dto.orders.OrderDiscountRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentRequestDTO;
import com.pikudo.restaurant.dto.orders.OrderPaymentVoidRequestDTO;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.caja.Caja;
import com.pikudo.restaurant.entity.caja.MetodoPago;
import com.pikudo.restaurant.entity.orders.OrderDiscount;
import com.pikudo.restaurant.entity.orders.OrderDiscountType;
import com.pikudo.restaurant.entity.orders.OrderPayment;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatusType;
import com.pikudo.restaurant.repository.CajaRepository;
import com.pikudo.restaurant.repository.MetodoPagoRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.orders.OrderDiscountRepository;
import com.pikudo.restaurant.repository.orders.OrderPaymentRepository;
import com.pikudo.restaurant.service.orders.TableSessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderPaymentServiceImplTest {

    private OrderPaymentRepository paymentRepository;
    private OrderDiscountRepository discountRepository;
    private PedidoRepository pedidoRepository;
    private CajaRepository cajaRepository;
    private MetodoPagoRepository metodoPagoRepository;
    private UsuarioRepository usuarioRepository;
    private OrderPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(OrderPaymentRepository.class);
        discountRepository = mock(OrderDiscountRepository.class);
        pedidoRepository = mock(PedidoRepository.class);
        cajaRepository = mock(CajaRepository.class);
        metodoPagoRepository = mock(MetodoPagoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        TableSessionService tableSessionService = mock(TableSessionService.class);
        service = new OrderPaymentServiceImpl(
                paymentRepository,
                discountRepository,
                pedidoRepository,
                cajaRepository,
                metodoPagoRepository,
                usuarioRepository,
                tableSessionService
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cajero", "N/A"));
        when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(user()));
        when(cajaRepository.findByEstado("ABIERTA")).thenReturn(Optional.of(Caja.builder().id(5L).estado("ABIERTA").build()));
        when(metodoPagoRepository.findByNombreIgnoreCase("EFECTIVO")).thenReturn(Optional.of(
                MetodoPago.builder().id(3L).nombre("EFECTIVO").tipo("EFECTIVO").activo(true).build()));
        when(paymentRepository.save(any(OrderPayment.class))).thenAnswer(invocation -> {
            OrderPayment payment = invocation.getArgument(0);
            payment.setId(20L);
            return payment;
        });
        when(discountRepository.save(any(OrderDiscount.class))).thenAnswer(invocation -> {
            OrderDiscount discount = invocation.getArgument(0);
            discount.setId(30L);
            return discount;
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void partialPaymentMarksOrderPartiallyPaid() {
        Pedido pedido = order(new BigDecimal("100.00"));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentRepository.sumByPedidoIdAndStatus(1L, OrderPaymentStatusType.CONFIRMED))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("25.00"));

        OrderPaymentRequestDTO request = new OrderPaymentRequestDTO();
        request.setMetodoPago("EFECTIVO");
        request.setMonto(new BigDecimal("25.00"));

        var response = service.addPayment(1L, request);

        assertThat(response.getMonto()).isEqualByComparingTo("25.00");
        assertThat(pedido.getEstadoPago()).isEqualTo(OrderPaymentStatus.PARTIALLY_PAID);
    }

    @Test
    void fullPaymentMarksOrderPaid() {
        Pedido pedido = order(new BigDecimal("100.00"));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentRepository.sumByPedidoIdAndStatus(1L, OrderPaymentStatusType.CONFIRMED))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("100.00"));

        OrderPaymentRequestDTO request = new OrderPaymentRequestDTO();
        request.setMetodoPago("EFECTIVO");
        request.setMonto(new BigDecimal("100.00"));

        service.addPayment(1L, request);

        assertThat(pedido.getEstadoPago()).isEqualTo(OrderPaymentStatus.PAID);
    }

    @Test
    void voidPaymentRecalculatesOrderStatus() {
        Pedido pedido = order(new BigDecimal("100.00"));
        pedido.setEstadoPago(OrderPaymentStatus.PARTIALLY_PAID);
        OrderPayment payment = OrderPayment.builder()
                .id(20L)
                .pedido(pedido)
                .caja(Caja.builder().id(5L).build())
                .metodoPago(MetodoPago.builder().id(3L).nombre("EFECTIVO").tipo("EFECTIVO").build())
                .monto(new BigDecimal("40.00"))
                .status(OrderPaymentStatusType.CONFIRMED)
                .build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentRepository.findById(20L)).thenReturn(Optional.of(payment));
        when(paymentRepository.sumByPedidoIdAndStatus(1L, OrderPaymentStatusType.CONFIRMED)).thenReturn(BigDecimal.ZERO);

        OrderPaymentVoidRequestDTO request = new OrderPaymentVoidRequestDTO();
        request.setReason("Error de digitacion");

        service.voidPayment(1L, 20L, request);

        assertThat(payment.getStatus()).isEqualTo(OrderPaymentStatusType.VOIDED);
        assertThat(pedido.getEstadoPago()).isEqualTo(OrderPaymentStatus.UNPAID);
    }

    @Test
    void manualAmountDiscountRecalculatesOrderTotal() {
        Pedido pedido = order(new BigDecimal("100.00"));
        DetallePedido detail = DetallePedido.builder()
                .id(10L)
                .pedido(pedido)
                .producto(Producto.builder().id(99L).nombre("Pollo").precio(new BigDecimal("100.00")).stock(1).build())
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .precioUnitarioSnapshot(new BigDecimal("100.00"))
                .productoNombreSnapshot("Pollo")
                .subtotal(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .lineTotal(new BigDecimal("100.00"))
                .build();
        pedido.setDetalles(List.of(detail));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(paymentRepository.sumByPedidoIdAndStatus(1L, OrderPaymentStatusType.CONFIRMED)).thenReturn(BigDecimal.ZERO);

        OrderDiscountRequestDTO request = new OrderDiscountRequestDTO();
        request.setDetailId(10L);
        request.setDiscountType(OrderDiscountType.MANUAL_AMOUNT);
        request.setValue(new BigDecimal("10.00"));
        request.setReason("Cortesia");

        service.applyDiscount(1L, request);

        assertThat(pedido.getDiscountTotal()).isEqualByComparingTo("10.00");
        assertThat(pedido.getTotal()).isEqualByComparingTo("90.00");
        assertThat(detail.getLineTotal()).isEqualByComparingTo("90.00");
    }

    private Pedido order(BigDecimal total) {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setTotal(total);
        pedido.setSubtotal(total);
        pedido.setDiscountTotal(BigDecimal.ZERO);
        pedido.setEstadoPago(OrderPaymentStatus.UNPAID);
        return pedido;
    }

    private Usuario user() {
        Usuario user = new Usuario();
        user.setId(7L);
        user.setUsername("cajero");
        return user;
    }
}
