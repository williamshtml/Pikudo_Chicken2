package com.pikudo.service.orders.impl;

import com.pikudo.dto.orders.OrderCreateRequestDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.entity.Mesa;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.Producto;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.catalog.Modifier;
import com.pikudo.entity.catalog.ModifierGroup;
import com.pikudo.entity.catalog.ProductoVariante;
import com.pikudo.entity.orders.OrderServiceType;
import com.pikudo.entity.orders.TableSession;
import com.pikudo.mapper.orders.OrderResponseMapper;
import com.pikudo.repository.MesaRepository;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.repository.catalog.ModifierRepository;
import com.pikudo.repository.catalog.ProductoVarianteRepository;
import com.pikudo.repository.orders.OrderDiscountRepository;
import com.pikudo.repository.orders.OrderPaymentRepository;
import com.pikudo.repository.orders.OrderStatusHistoryRepository;
import com.pikudo.service.orders.OrderTransitionService;
import com.pikudo.service.orders.TableSessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private PedidoRepository pedidoRepository;
    private MesaRepository mesaRepository;
    private UsuarioRepository usuarioRepository;
    private ProductoVarianteRepository varianteRepository;
    private ModifierRepository modifierRepository;
    private TableSessionService tableSessionService;
    private OrderTransitionService transitionService;
    private OrderPaymentRepository orderPaymentRepository;
    private OrderDiscountRepository orderDiscountRepository;
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        mesaRepository = mock(MesaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        varianteRepository = mock(ProductoVarianteRepository.class);
        modifierRepository = mock(ModifierRepository.class);
        OrderStatusHistoryRepository historyRepository = mock(OrderStatusHistoryRepository.class);
        orderPaymentRepository = mock(OrderPaymentRepository.class);
        orderDiscountRepository = mock(OrderDiscountRepository.class);
        tableSessionService = mock(TableSessionService.class);
        transitionService = mock(OrderTransitionService.class);

        service = new OrderServiceImpl(
                pedidoRepository,
                mesaRepository,
                usuarioRepository,
                varianteRepository,
                modifierRepository,
                historyRepository,
                tableSessionService,
                transitionService,
                new OrderResponseMapper(orderPaymentRepository, orderDiscountRepository)
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("mozo", "N/A"));
        when(usuarioRepository.findByUsername("mozo")).thenReturn(Optional.of(user()));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            pedido.setId(100L);
            return pedido;
        });
        when(orderPaymentRepository.sumByPedidoIdAndStatus(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderDiscountRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(any())).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsDineInOrderWithOpenTableSessionAndCalculatedModifierSnapshots() {
        Producto producto = product(10L);
        ProductoVariante variant = ProductoVariante.builder()
                .id(20L)
                .producto(producto)
                .nombre("Medio pollo")
                .precioActual(new BigDecimal("30.00"))
                .build();
        ModifierGroup group = ModifierGroup.builder().id(30L).nombre("Cremas").slug("cremas").build();
        Modifier modifier = Modifier.builder()
                .id(40L)
                .group(group)
                .nombre("Aji extra")
                .slug("aji-extra")
                .precioExtra(new BigDecimal("2.50"))
                .build();
        Mesa mesa = Mesa.builder().id(5L).numero(7).capacidad(4).estado(true).build();
        TableSession session = TableSession.builder().id(70L).mesa(mesa).openedBy(user()).build();

        when(tableSessionService.ensureOpenSession(eq(5L), isNull(), any(Usuario.class))).thenReturn(session);
        when(varianteRepository.findById(20L)).thenReturn(Optional.of(variant));
        when(modifierRepository.findById(40L)).thenReturn(Optional.of(modifier));

        OrderCreateRequestDTO request = new OrderCreateRequestDTO();
        request.setMesaId(5L);
        request.setServiceType(OrderServiceType.DINE_IN);
        OrderCreateRequestDTO.Item item = new OrderCreateRequestDTO.Item();
        item.setVariantId(20L);
        item.setQuantity(2);
        OrderCreateRequestDTO.SelectedModifier selected = new OrderCreateRequestDTO.SelectedModifier();
        selected.setModifierId(40L);
        selected.setQuantity(1);
        item.getModifiers().add(selected);
        request.getItems().add(item);

        var response = service.create(request);

        assertThat(response.getTableSessionId()).isEqualTo(70L);
        assertThat(response.getTotal()).isEqualByComparingTo("65.00");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("Producto 10");
        assertThat(response.getItems().get(0).getVariantName()).isEqualTo("Medio pollo");
        assertThat(response.getItems().get(0).getUnitPrice()).isEqualByComparingTo("32.50");
        assertThat(response.getItems().get(0).getModifiers().get(0).getTotalExtra()).isEqualByComparingTo("5.00");
        verify(transitionService).recordInitialStatus(any(Pedido.class), any(Usuario.class), any(String.class));
    }

    @Test
    void createsDeliveryOrderWithoutTableSession() {
        ProductoVariante variant = ProductoVariante.builder()
                .id(20L)
                .producto(product(10L))
                .nombre("Base")
                .precioActual(new BigDecimal("18.00"))
                .build();
        when(varianteRepository.findById(20L)).thenReturn(Optional.of(variant));

        OrderCreateRequestDTO request = new OrderCreateRequestDTO();
        request.setServiceType(OrderServiceType.DELIVERY);
        request.setDireccion("Av. Principal 123");
        OrderCreateRequestDTO.Item item = new OrderCreateRequestDTO.Item();
        item.setVariantId(20L);
        item.setQuantity(1);
        request.getItems().add(item);

        var response = service.create(request);

        assertThat(response.getMesaId()).isNull();
        assertThat(response.getTableSessionId()).isNull();
        assertThat(response.getTrackingCode()).startsWith("TRK-");
        assertThat(response.getTotal()).isEqualByComparingTo("18.00");
        verify(tableSessionService, never()).ensureOpenSession(any(), any(), any());
    }

    private Usuario user() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("mozo");
        return usuario;
    }

    private Producto product(Long id) {
        return Producto.builder()
                .id(id)
                .nombre("Producto " + id)
                .slug("producto-" + id)
                .categoria(Categoria.builder().id(1L).nombre("Categoria").slug("categoria").build())
                .precio(new BigDecimal("10.00"))
                .stock(10)
                .estado(true)
                .disponible(true)
                .visiblePublico(true)
                .build();
    }
}
