package com.pikudo.service.impl;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.*;
import com.pikudo.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private SimpMessagingTemplate template;

    // Nuevas dependencias para impresion de tickets
    @Autowired private TicketPrinterService ticketPrinterService;
    @Autowired private ImpresoraRepository impresoraRepository;

    // --- METODOS DE CREACION Y ESTADO ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario mesero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario de sesión no encontrado"));

        Mesa mesa = (dto.getMesaId() != null) ? mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada")) : null;

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMesero(mesero);

        // Si no hay mesa, asumimos por defecto que es para despacho externo
        String tipo = (dto.getTipoPedido() != null) ? dto.getTipoPedido().toUpperCase() : "MESA";
        pedido.setTipoPedido(tipo);

        if ("DELIVERY".equals(tipo)) {
            pedido.setEstado(EstadoPedido.PENDING); // Inicialmente ingresa a cola
            pedido.setDireccion(dto.getDireccion());

            // Generación dinámica de la URL de navegación en Maps
            if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
                String urlFormateada = "https://www.google.com/maps/search/?api=1&query="
                        + URLEncoder.encode(dto.getDireccion(), StandardCharsets.UTF_8);
                pedido.setUrlMaps(urlFormateada);
            }
        } else {
            pedido.setEstado(EstadoPedido.PENDING);
        }

        BigDecimal totalAcumulado = BigDecimal.ZERO;
        List<DetallePedido> detallesEntidad = new ArrayList<>();

        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                Producto producto = productoRepository.findById(itemDto.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                DetallePedido detalle = new DetallePedido();
                detalle.setPedido(pedido);
                detalle.setProducto(producto);
                detalle.setCantidad(itemDto.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                detalle.setObservaciones(itemDto.getObservaciones());
                detalle.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(itemDto.getCantidad())));

                totalAcumulado = totalAcumulado.add(detalle.getSubtotal());
                detallesEntidad.add(detalle);
            }
        }
        pedido.setTotal(totalAcumulado);
        pedido.setDetalles(detallesEntidad);

        Pedido guardado = pedidoRepository.save(pedido);

        // Imprime automaticamente los tickets de cocina/bar/horno segun la categoria de cada producto.
        // Si una impresora esta apagada/desconectada, no rompe la creacion del pedido (ver TicketPrinterService).
        ticketPrinterService.imprimirTicketsPorArea(guardado);

        PedidoResponseDTO response = mapearADto(guardado);

        // Si es Delivery, alertamos instantáneamente a todos los repartidores conectados
        if ("DELIVERY".equals(guardado.getTipoPedido())) {
            template.convertAndSend("/topic/repartidores", response);
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario user = usuarioRepository.findByUsername(username).orElseThrow();
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();

        if (nuevoEstado == EstadoPedido.PAID) pedido.setCajero(user);

        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        PedidoResponseDTO response = mapearADto(actualizado);

        template.convertAndSend("/topic/pedidos", response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PedidoResponseDTO tomarPedido(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario repartidor = usuarioRepository.findByUsername(username).orElseThrow();
        Pedido p = pedidoRepository.findById(id).orElseThrow();

        p.setRepartidor(repartidor);
        p.setEstado(EstadoPedido.ON_DELIVERY);

        Pedido guardado = pedidoRepository.save(p);

        // Imprime la precuenta en la impresora de caja apenas el repartidor toma el pedido
        impresoraRepository.findByAreaAndActivaTrue(AreaPreparacion.CAJA)
                .ifPresentOrElse(
                        impresoraCaja -> ticketPrinterService.imprimirPrecuentaDelivery(guardado, impresoraCaja),
                        () -> { /* no hay impresora de caja configurada; no se rompe el flujo */ }
                );

        PedidoResponseDTO response = mapearADto(guardado);

        // Notifica a la caja en tiempo real que el pedido cambió de estado y ya tiene repartidor
        template.convertAndSend("/topic/pedidos", response);
        return response;
    }

    // --- METODOS DE CONSULTA (Se mantienen idénticos) ---
    @Override
    public List<PedidoResponseDTO> listarTodos() { return pedidoRepository.findAll().stream().map(this::mapearADto).collect(Collectors.toList()); }

    @Override
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) { return pedidoRepository.findByEstado(estado).stream().map(this::mapearADto).collect(Collectors.toList()); }

    @Override
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) { return pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido).stream().map(this::mapearADto).collect(Collectors.toList()); }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) { return mapearADto(pedidoRepository.findById(id).orElseThrow()); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelar(Long id) {
        // Cambiado de deleteById() a cambio de estado: evita perder el historial
        // y previene errores de integridad referencial si el pedido ya tiene
        // un Comprobante u otras relaciones asociadas.
        Pedido p = pedidoRepository.findById(id).orElseThrow();
        p.setEstado(EstadoPedido.CANCELLED);
        Pedido actualizado = pedidoRepository.save(p);

        template.convertAndSend("/topic/pedidos", mapearADto(actualizado));
    }

    // --- MAPEO PRIVADO ---
    private PedidoResponseDTO mapearADto(Pedido p) {
        PedidoResponseDTO r = new PedidoResponseDTO();
        r.setId(p.getId());
        r.setMesaNumero(p.getMesa() != null ? p.getMesa().getNumero() : 0);
        r.setTotal(p.getTotal());
        r.setEstadoPedido(p.getEstado().name());

        // Mapeo de campos de Delivery adicionales
        r.setDireccion(p.getDireccion());
        r.setUrlMaps(p.getUrlMaps());

        r.setCajeroNombre(p.getCajero() != null ? p.getCajero().getUsername() : "Pendiente de Caja");

        if ("MESA".equals(p.getTipoPedido())) {
            r.setResponsableNombre(p.getMesero() != null ? p.getMesero().getUsername() : "N/A");
            r.setResponsableRol("Mesero");
        } else if ("DELIVERY".equals(p.getTipoPedido())) {
            r.setResponsableNombre(p.getRepartidor() != null ? p.getRepartidor().getUsername() : "Por asignar");
            r.setResponsableRol("Repartidor");
        } else {
            r.setResponsableNombre("Mostrador");
            r.setResponsableRol("Venta Directa");
        }

        BigDecimal neto = p.getTotal().divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        r.setSubtotalNeto(neto);
        r.setIgv(p.getTotal().subtract(neto));

        r.setDetalles(p.getDetalles().stream().map(d -> {
            PedidoResponseDTO.DetalleItemDTO item = new PedidoResponseDTO.DetalleItemDTO();
            item.setId(d.getId());
            item.setProductoNombre(d.getProducto().getNombre());
            item.setPrecioUnitario(d.getPrecioUnitario());
            item.setCantidad(d.getCantidad());
            item.setSubtotal(d.getSubtotal());
            item.setObservaciones(d.getObservaciones());
            return item;
        }).collect(Collectors.toList()));

        // Mapeo seguro de fecha heredada de Auditable
        if (p.getFechaCreacion() != null) {
            r.setFechaCreacion(p.getFechaCreacion());
        }

        return r;
    }
}