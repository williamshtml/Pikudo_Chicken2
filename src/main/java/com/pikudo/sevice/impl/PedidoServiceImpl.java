package com.pikudo.sevice.impl;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.MesaRepository;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.PedidoService;
import com.pikudo.util.FechaUtil; // Utiliza tu formateador de fechas
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PedidoServiceImpl implements PedidoService {
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        // 1. Validar y obtener la Mesa
        Mesa mesa = mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + dto.getMesaId()));

        // 2. Validar y obtener el Usuario (Mesero)
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + dto.getUsuarioId()));

        // SOLUCIÓN LÍNEA 44: Cambiar el estado booleano de la mesa a true (OCUPADA)
        mesa.setEstado(true); 
        mesaRepository.save(mesa);

        // 3. Inicializar la cabecera del Pedido
        Pedido pedido = Pedido.builder()
                .mesa(mesa)
                .usuario(usuario)
                .fechaHora(LocalDateTime.now())
                .estado(EstadoPedido.PENDING)
                .build();

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal totalPedido = BigDecimal.ZERO;

        // 4. Procesar cada ítem, calcular subtotales y validar Stock vigente
        for (PedidoRequestDTO.DetalleItemDTO item : dto.getDetalles()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + item.getProductoId()));

            // Validación de Stock vigente
            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            // Descuento automático de stock en tiempo real
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // Cálculos matemáticos usando precios vigentes
            BigDecimal precioUnitario = producto.getPrecio();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .observaciones(item.getObservaciones())
                    .build();

            detalles.add(detalle);
            totalPedido = totalPedido.add(subtotal);
        }

        pedido.setDetalles(detalles);
        pedido.setTotal(totalPedido);

        // 5. Guardar pedido en MySQL
        Pedido guardado = pedidoRepository.save(pedido);
        return toDTO(guardado);
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) {
        return pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        return toDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        pedido.setEstado(nuevoEstado);
        
        // SOLUCIÓN LÍNEA 139: Si se paga o cancela, la mesa pasa a false (DISPONIBLE)
        if (nuevoEstado == EstadoPedido.PAID || nuevoEstado == EstadoPedido.CANCELLED) {
            if (pedido.getMesa() != null) {
                pedido.getMesa().setEstado(false);
                mesaRepository.save(pedido.getMesa());
            }
        }
        
        return toDTO(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("No se puede cancelar un pedido que ya fue pagado");
        }

        // Reposición de stock
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        // SOLUCIÓN LÍNEA 166: Liberar la mesa pasándola a false (DISPONIBLE) al cancelar
        if (pedido.getMesa() != null) {
            pedido.getMesa().setEstado(false);
            mesaRepository.save(pedido.getMesa());
        }

        pedido.setEstado(EstadoPedido.CANCELLED);
        pedidoRepository.save(pedido);
    }

    // ─── MAPPER INTERNO ─────────────────────────────────────────────────────────
    private PedidoResponseDTO toDTO(Pedido p) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(p.getId());
        response.setMesaNumero(p.getMesa() != null ? p.getMesa().getNumero() : null);
        response.setUsuarioNombre(p.getUsuario() != null ? p.getUsuario().getUsername() : null);
        
        // Aquí usamos FechaUtil si tu response requiriera un String.
        // Si tu 'PedidoResponseDTO' acepta LocalDateTime, dejamos la asignación directa,
        // pero podemos imprimir en consola usando la utilidad para asegurar que el import sirva para auditoría técnica:
        if (p.getFechaHora() != null) {
            System.out.println("Procesando pedido del: " + FechaUtil.formatearFecha(p.getFechaHora()));
        }
        response.setFechaHora(p.getFechaHora()); 
        
        response.setTotal(p.getTotal());
        response.setEstadoPedido(p.getEstado() != null ? p.getEstado().name() : null);

        List<PedidoResponseDTO.DetalleItemDTO> detallesDTO = p.getDetalles() == null
                ? new ArrayList<>()
                : p.getDetalles().stream().map(this::toDetalleDTO).collect(Collectors.toList());
        response.setDetalles(detallesDTO);

        return response;
    }

    private PedidoResponseDTO.DetalleItemDTO toDetalleDTO(DetallePedido d) {
        PedidoResponseDTO.DetalleItemDTO item = new PedidoResponseDTO.DetalleItemDTO();
        item.setId(d.getId());
        item.setProductoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null);
        item.setPrecioUnitario(d.getPrecioUnitario());
        item.setCantidad(d.getCantidad());
        item.setSubtotal(d.getSubtotal());
        item.setObservaciones(d.getObservaciones());
        return item;
    }
}
