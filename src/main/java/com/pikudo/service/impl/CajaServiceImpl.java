package com.pikudo.service.impl;

import com.pikudo.exception.BusinessException;
import com.pikudo.mapper.CajaMapper;
import com.pikudo.dto.caja.CajaDTO;
import com.pikudo.dto.caja.CajaResumenDTO;
import com.pikudo.dto.caja.GastoDTO;
import com.pikudo.dto.caja.MetodoPagoDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.caja.Caja;
import com.pikudo.entity.caja.Gasto;
import com.pikudo.repository.CajaRepository;
import com.pikudo.repository.GastoRepository;
import com.pikudo.repository.MetodoPagoRepository;
import com.pikudo.repository.TransaccionPagoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.CajaService;
import com.pikudo.service.TicketPrinterService;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final TransaccionPagoRepository transaccionPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaMapper cajaMapper;
    private final TicketPrinterService ticketPrinterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaDTO abrirCaja(CajaDTO dto) {
        // 1ra capa: validación rápida con lock pesimista (evita condición de carrera)
        cajaRepository.findByEstadoConLock().ifPresent(c -> {
            throw new BusinessException("Ya existe un turno de caja abierto en el sistema");
        });

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Caja caja = Caja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(dto.getMontoInicial())
                .estado("ABIERTA")
                .build();

        try {
            // 2da capa: red de seguridad si el constraint de BD existe (ver migración SQL)
            return cajaMapper.toCajaDTO(cajaRepository.save(caja));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Ya existe un turno de caja abierto en el sistema");
        }
    }

    @Override
    public CajaDTO obtenerTurnoActual() {
        Caja caja = cajaRepository.findByEstado("ABIERTA")
                .orElseThrow(() -> new BusinessException("No hay ninguna caja abierta en este momento"));
        return cajaMapper.toCajaDTO(caja);
    }

    @Override
    public CajaResumenDTO obtenerResumenParaCierre(Long cajaId) {
        Caja caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new BusinessException("Turno de caja no encontrado"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new BusinessException("Esta caja ya se encuentra cerrada");
        }

        LocalDateTime ahora = LocalDateTime.now();

        BigDecimal efectivo = transaccionPagoRepository.calcularTotalPorTipoMetodo("EFECTIVO", caja.getFechaApertura(), ahora);
        BigDecimal tarjeta = transaccionPagoRepository.calcularTotalPorTipoMetodo("TARJETA", caja.getFechaApertura(), ahora);
        BigDecimal digital = transaccionPagoRepository.calcularTotalPorTipoMetodo("DIGITAL", caja.getFechaApertura(), ahora);
        BigDecimal gastos = gastoRepository.calcularTotalGastosPorRango(caja.getFechaApertura(), ahora);

        efectivo = (efectivo != null) ? efectivo : BigDecimal.ZERO;
        tarjeta = (tarjeta != null) ? tarjeta : BigDecimal.ZERO;
        digital = (digital != null) ? digital : BigDecimal.ZERO;
        gastos = (gastos != null) ? gastos : BigDecimal.ZERO;

        BigDecimal montoEsperado = caja.getMontoInicial().add(efectivo).subtract(gastos);

        return CajaResumenDTO.builder()
                .cajaId(caja.getId())
                .montoInicial(caja.getMontoInicial())
                .montoVentasEfectivo(efectivo)
                .montoVentasTarjeta(tarjeta)
                .montoVentasDigital(digital)
                .montoGastos(gastos)
                .montoEsperadoEnCajon(montoEsperado)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaDTO cerrarCaja(Long cajaId, CajaDTO dto) {
        Caja caja = cajaRepository.findById(cajaId)
                .orElseThrow(() -> new BusinessException("Turno de caja no encontrado"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new BusinessException("Esta caja ya se encuentra cerrada");
        }

        if (dto.getMontoFinalReal() == null || dto.getMontoFinalReal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El monto final contado es obligatorio y no puede ser negativo");
        }

        LocalDateTime finTurno = LocalDateTime.now();

        BigDecimal efectivo = transaccionPagoRepository.calcularTotalPorTipoMetodo("EFECTIVO", caja.getFechaApertura(), finTurno);
        BigDecimal tarjeta = transaccionPagoRepository.calcularTotalPorTipoMetodo("TARJETA", caja.getFechaApertura(), finTurno);
        BigDecimal digital = transaccionPagoRepository.calcularTotalPorTipoMetodo("DIGITAL", caja.getFechaApertura(), finTurno);
        BigDecimal gastos = gastoRepository.calcularTotalGastosPorRango(caja.getFechaApertura(), finTurno);

        caja.setFechaCierre(finTurno);
        caja.setMontoVentasEfectivo((efectivo != null) ? efectivo : BigDecimal.ZERO);
        caja.setMontoVentasTarjeta((tarjeta != null) ? tarjeta : BigDecimal.ZERO);
        caja.setMontoVentasDigital((digital != null) ? digital : BigDecimal.ZERO);
        caja.setMontoGastos((gastos != null) ? gastos : BigDecimal.ZERO);
        caja.setMontoFinalSistema(caja.getMontoInicial().add(caja.getMontoVentasEfectivo()).subtract(caja.getMontoGastos()));
        caja.setMontoFinalReal(dto.getMontoFinalReal());
        caja.setObservaciones(dto.getObservaciones());
        caja.setEstado("CERRADA");

        Caja cerrada = cajaRepository.save(caja);
        ticketPrinterService.imprimirReporteCierreCaja(cerrada);

        return cajaMapper.toCajaDTO(cerrada);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GastoDTO registrarGasto(GastoDTO dto) {
        Caja caja = cajaRepository.findById(dto.getCajaTurnoId())
                .orElseThrow(() -> new BusinessException("El turno de caja especificado no existe"));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new BusinessException("No se pueden registrar gastos en una caja cerrada");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Gasto gasto = Gasto.builder()
                .caja(caja)
                .monto(dto.getMonto())
                .descripcion(dto.getDescripcion())
                .usuario(usuario)
                .build();

        return cajaMapper.toGastoDTO(gastoRepository.save(gasto));
    }

    @Override
    public List<GastoDTO> listarGastosPorTurno(Long cajaId) {
        return gastoRepository.findByCajaId(cajaId).stream()
                .map(cajaMapper::toGastoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetodoPagoDTO> listarMetodosPagoActivos() {
        return metodoPagoRepository.findByActivoTrue().stream()
                .map(cajaMapper::toMetodoPagoDTO)
                .collect(Collectors.toList());
    }
}