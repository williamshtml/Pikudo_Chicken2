package com.pikudo.restaurant.service.impl;
import com.pikudo.restaurant.dto.mesa.MesaEstadoResponseDTO;
import com.pikudo.restaurant.dto.mesa.MesaRequestDTO;
import com.pikudo.restaurant.dto.mesa.MesaResponseDTO;
import com.pikudo.restaurant.entity.Mesa;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.MesaMapper;
import com.pikudo.restaurant.repository.MesaRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.service.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesaServiceImpl implements MesaService {
    private static final List<OrderOperationalStatus> TERMINAL_STATUSES = List.of(
            OrderOperationalStatus.REJECTED,
            OrderOperationalStatus.DELIVERED,
            OrderOperationalStatus.CANCELLED
    );

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaMapper mesaMapper;

    @Override
    @Transactional
    public MesaResponseDTO crear(MesaRequestDTO dto) {
        validarNumeroDisponible(dto.getNumero(), null);

        Mesa mesa = Mesa.builder()
                .numero(dto.getNumero())
                .capacidad(dto.getCapacidad())
                .estado(true)
                .build();
        return mesaMapper.toDTO(mesaRepository.save(mesa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listarTodas() {
        return mesaRepository.findAll().stream()
                .map(mesaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listarDisponibles() {
        return mesaRepository.findByEstado(true).stream()
                .map(mesaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaEstadoResponseDTO> listarConOcupacion() {
        return mesaRepository.findByEstado(true).stream()
                .map(mesa -> {
                    // La ocupacion se calcula en el momento, no se guarda como campo fijo:
                    // evita que quede desincronizada con el estado real de los pedidos.
                    boolean tienePedidoAbierto = !pedidoRepository
                            .findByMesaIdAndEstadoOperativoNotIn(mesa.getId(), TERMINAL_STATUSES)
                            .isEmpty();

                    return new MesaEstadoResponseDTO(
                            mesa.getId(),
                            mesa.getNumero(),
                            mesa.getCapacidad(),
                            mesa.getEstado(),
                            tienePedidoAbierto
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDTO buscarPorId(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + id));
        return mesaMapper.toDTO(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizar(Long id, MesaRequestDTO dto) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + id));

        validarNumeroDisponible(dto.getNumero(), id);

        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad());
        return mesaMapper.toDTO(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada con id: " + id));

        boolean tienePedidoAbierto = !pedidoRepository
                .findByMesaIdAndEstadoOperativoNotIn(id, TERMINAL_STATUSES)
                .isEmpty();

        if (tienePedidoAbierto) {
            throw new BusinessException(
                    "No se puede dar de baja la mesa #" + mesa.getNumero() +
                            ": tiene un pedido abierto. Cierra o cancela el pedido primero.");
        }

        mesa.setEstado(false);
        mesaRepository.save(mesa);
    }

    private void validarNumeroDisponible(Integer numero, Long idExcluido) {
        mesaRepository.findByNumero(numero).ifPresent(existente -> {
            if (idExcluido == null || !existente.getId().equals(idExcluido)) {
                throw new BusinessException("Ya existe una mesa con el número " + numero);
            }
        });
    }
}
