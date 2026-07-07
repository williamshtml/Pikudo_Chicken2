package com.pikudo.service.impl;

import com.pikudo.mapper.CajaMapper;
import com.pikudo.dto.caja.CajaDTO;
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
    private final TransaccionPagoRepository transaccionPagoRepository; // Reemplaza a pedidoRepository para el desglose por metodo
    private final UsuarioRepository usuarioRepository;
    private final CajaMapper cajaMapper;
    private final TicketPrinterService ticketPrinterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CajaDTO abrirCaja(CajaDTO dto) {
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

        return cajaMapper.toCajaDTO(cajaRepository.save(caja));
    }

    @Override
    public CajaDTO obtenerTurnoActual() {
        Caja caja = cajaRepository.findByEstado("ABIERTA")
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta en este momento"));
        return cajaMapper.toCajaDTO(caja);
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

        // Ahora se calcula a partir de TransaccionPago, ya que un pedido puede
        // tener varios metodos de pago (pagos divididos)
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