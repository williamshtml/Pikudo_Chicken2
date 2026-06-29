package com.pikudo.sevice.impl;

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.dto.pedido.PedidoResponseDTO;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.Producto;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.Mesa;
import com.pikudo.entity.EstadoPedido;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.repository.MesaRepository;
import com.pikudo.service.PedidoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private SimpMessagingTemplate template; // Canal WebSocket en tiempo real

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        
        // 1. RECOVERY DESDE LA BASE DE DATOS
        Mesa mesa = mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + dto.getMesaId()));
                
        Usuario mozo = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // 2. INICIALIZAR LA CABECERA DEL PEDIDO
        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setUsuario(mozo);
        pedido.setFechaHora(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDING);
        
        BigDecimal totalAcumulado = BigDecimal.ZERO;
        List<DetallePedido> detallesEntidad = new ArrayList<>();

        // 3. PROCESAR Y CALCULAR CADA PLATO DE LA COMANDA
        if (dto.getDetalles() != null) {
            for (PedidoRequestDTO.DetalleItemDTO itemDto : dto.getDetalles()) {
                
                Producto producto = productoRepository.findById(itemDto.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + itemDto.getProductoId()));
                
                DetallePedido detalle = new DetallePedido();
                detalle.setPedido(pedido);
                detalle.setProducto(producto);
                detalle.setCantidad(itemDto.getCantidad());
                
                BigDecimal precioProd = producto.getPrecio();
                detalle.setPrecioUnitario(precioProd);
                detalle.setObservaciones(itemDto.getObservaciones());
                
                // Cálculo: cantidad * precioUnitario
                BigDecimal subtotalItem = precioProd.multiply(BigDecimal.valueOf(itemDto.getCantidad()));
                detalle.setSubtotal(subtotalItem);
                
                totalAcumulado = totalAcumulado.add(subtotalItem);
                detallesEntidad.add(detalle);
            }
        }
        
        pedido.setTotal(totalAcumulado); 
        pedido.setDetalles(detallesEntidad);

        // 4. GUARDAR EN MYSQL
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 5. ENCAPSULAR RESPUESTA PARA EL FRONTEND USANDO EL MÉTODO AUXILIAR
        PedidoResponseDTO response = mapearADto(pedidoGuardado);

        // 7. MULTICAST POR WEBSOCKET HACIA LA CAJA AL INSTANTE
        template.convertAndSend("/topic/pedidos", response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return mapearAListas(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        // Asegúrate de tener este método declarado en tu PedidoRepository si lo usas
        List<Pedido> pedidos = pedidoRepository.findByEstado(estado);
        return mapearAListas(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido) {
        // Asegúrate de tener este método declarado en tu PedidoRepository si lo usas
        List<Pedido> pedidos = pedidoRepository.findByMesaIdAndEstadoNot(mesaId, estadoExcluido);
        return mapearAListas(pedidos);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        return mapearADto(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        
        pedido.setEstado(nuevoEstado);
        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        
        PedidoResponseDTO response = mapearADto(pedidoActualizado);
        
        // Broadcast en tiempo real a los clientes conectados por WebSocket tras el cambio de estado
        template.convertAndSend("/topic/pedidos", response);
        
        return response;
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        
        pedidoRepository.delete(pedido);
    }

    // ========== MÉTODOS REUTILIZABLES DE MAPEO DTO ==========

    private List<PedidoResponseDTO> mapearAListas(List<Pedido> pedidos) {
        List<PedidoResponseDTO> listaDto = new ArrayList<>();
        for (Pedido p : pedidos) {
            listaDto.add(mapearADto(p));
        }
        return listaDto;
    }

    private PedidoResponseDTO mapearADto(Pedido pedido) {
        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setId(pedido.getId());
        response.setMesaNumero(pedido.getMesa().getNumero()); 
        response.setUsuarioNombre(pedido.getUsuario().getUsername()); 
        response.setFechaHora(pedido.getFechaHora()); 
        response.setEstadoPedido(pedido.getEstado().name()); 
        response.setTotal(pedido.getTotal()); 
        
        // ---- OPERACIÓN MATEMÁTICA FINANCIERA PERUANA ----
        // Dividimos entre 1.18 para el Neto (Base Imponible)
        BigDecimal neto = pedido.getTotal().divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
        response.setSubtotalNeto(neto);
        
        // Restamos (Total - Neto) para calcular el Impuesto (IGV 18%)
        BigDecimal impuestoIgv = pedido.getTotal().subtract(neto);
        response.setIgv(impuestoIgv);
        // -------------------------------------------------

        List<PedidoResponseDTO.DetalleItemDTO> detallesResponse = new ArrayList<>();
        for (DetallePedido det : pedido.getDetalles()) {
            PedidoResponseDTO.DetalleItemDTO resItem = new PedidoResponseDTO.DetalleItemDTO();
            resItem.setId(det.getId());
            resItem.setProductoNombre(det.getProducto().getNombre()); 
            resItem.setPrecioUnitario(det.getPrecioUnitario());
            resItem.setCantidad(det.getCantidad());
            resItem.setSubtotal(det.getSubtotal()); 
            resItem.setObservaciones(det.getObservaciones());
            
            detallesResponse.add(resItem);
        }
        response.setDetalles(detallesResponse);

        return response;
    }
}