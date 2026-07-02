package com.pikudo.service.caja.impl;

import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.dto.caja.MetodoPagoDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.caja.Caja;
import com.pikudo.entity.caja.Gasto;
import com.pikudo.repository.CajaRepository; // Asegúrate de tener este repositorio básico creado
import com.pikudo.repository.GastoRepository;
import com.pikudo.repository.MetodoPagoRepository; // Asegúrate de tener este repositorio básico creado
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.caja.CajaService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final GastoRepository gastoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaDTO abrirCaja(CajaDTO dto) {
        // Verificar si ya existe una caja abierta para evitar duplicidad de turnos
        cajaRepository.findByEstado("ABIERTA").ifPresent(c -> {
            throw new RuntimeException("Ya existe un turno de caja abierto en el sistema");
        });

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Caja caja = Caja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(dto.getMontoInicial())
                .estado("ABIERTA")
                .build();

        return mapearCajaADto(cajaRepository.save(caja));
    }

    @Override
    public CajaDTO obtenerTurnoActual() {
        Caja caja = cajaRepository.findByEstado("ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta en este momento"));
        return mapearCajaADto(caja);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaDTO cerrarCaja(Long cajaId, CajaDTO dto) {
        Caja caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new RuntimeException("Turno de caja no encontrado"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("Esta caja ya se encuentra cerrada");
        }

        LocalDateTime finTurno = LocalDateTime.now();

        // 1. Calcular totales del período de caja usando consultas personalizadas
        BigDecimal efectivo = pedidoRepository.calcularTotalVentasPorMetodoTipo(caja.getFechaApertura(), finTurno, "EFECTIVO");
        BigDecimal tarjeta = pedidoRepository.calcularTotalVentasPorMetodoTipo(caja.getFechaApertura(), finTurno, "TARJETA");
        BigDecimal digital = pedidoRepository.calcularTotalVentasPorMetodoTipo(caja.getFechaApertura(), finTurno, "DIGITAL");
        BigDecimal gastos = gastoRepository.calcularTotalGastosPorRango(caja.getFechaApertura(), finTurno);

        // Validar nulos de base de datos
        efectivo = (efectivo != null) ? efectivo : BigDecimal.ZERO;
        tarjeta = (tarjeta != null) ? tarjeta : BigDecimal.ZERO;
        digital = (digital != null) ? digital : BigDecimal.ZERO;
        gastos = (gastos != null) ? gastos : BigDecimal.ZERO;

        // 2. Sistema = Monto Inicial + Ventas Efectivo - Gastos
        BigDecimal montoFinalSistema = caja.getMontoInicial().add(efectivo).subtract(gastos);

        // 3. Actualizar la entidad
        caja.setFechaCierre(finTurno);
        caja.setMontoVentasEfectivo(efectivo);
        caja.setMontoVentasTarjeta(tarjeta);
        caja.setMontoVentasDigital(digital);
        caja.setMontoGastos(gastos);
        caja.setMontoFinalSistema(montoFinalSistema);
        caja.setMontoFinalReal(dto.getMontoFinalReal()); // Lo declarado físicamente por el cajero
        caja.setObservaciones(dto.getObservaciones());
        caja.setEstado("CERRADA");

        return mapearCajaADto(cajaRepository.save(caja));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GastoDTO registrarGasto(GastoDTO dto) {
        Caja caja = cajaRepository.findById(dto.getCajaTurnoId())
                .orElseThrow(() -> new RuntimeException("El turno de caja especificado no existe"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("No se pueden registrar gastos en una caja cerrada");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        Gasto gasto = Gasto.builder()
                .caja(caja)
                .monto(dto.getMonto())
                .descripcion(dto.getDescripcion())
                .usuario(usuario)
                .build();

        return mapearGastoADto(gastoRepository.save(gasto));
    }

    @Override
    public List<GastoDTO> listarGastosPorTurno(Long cajaId) {
        return gastoRepository.findByCajaId(cajaId)
                .stream()
                .map(this::mapearGastoADto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDTO> listarMetodosPagoActivos() {
        return metodoPagoRepository.findByActivoTrue()
                .stream()
                .map(mp -> MetodoPagoDTO.builder()
                        .id(mp.getId()).nombre(mp.getNombre()).tipo(mp.getTipo()).activo(mp.getActivo())
                        .build())
                .collect(Collectors.toList());
    }

    // --- MÉTODOS PRIVADOS DE MAPEO ---
    private CajaDTO mapearCajaADto(Caja c) {
        return CajaDTO.builder()
                .id(c.getId())
                .usuarioUsername(c.getUsuario() != null ? c.getUsuario().getUsername() : null)
                .fechaApertura(c.getFechaApertura()).fechaCierre(c.getFechaCierre())
                .montoInicial(c.getMontoInicial()).montoVentasEfectivo(c.getMontoVentasEfectivo())
                .montoVentasTarjeta(c.getMontoVentasTarjeta()).montoVentasDigital(c.getMontoVentasDigital())
                .montoGastos(c.getMontoGastos()).montoFinalSistema(c.getMontoFinalSistema())
                .montoFinalReal(c.getMontoFinalReal()).observaciones(c.getObservaciones()).estado(c.getEstado())
                .build();
    }

    private GastoDTO mapearGastoADto(Gasto g) {
        return GastoDTO.builder()
                .id(g.getId()).cajaTurnoId(g.getCaja().getId()).monto(g.getMonto())
                .descripcion(g.getDescripcion())
                .usuarioUsername(g.getUsuario() != null ? g.getUsuario().getUsername() : "SISTEMA")
                .fechaCreacion(g.getFechaCreacion())
                .build();
    }
}