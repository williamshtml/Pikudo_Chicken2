package com.pikudo.service.impl;

import com.pikudo.dto.comprobante.PagoDetalleDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.caja.MetodoPago;
import com.pikudo.entity.caja.TransaccionPago;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.MetodoPagoRepository;
import com.pikudo.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final MetodoPagoRepository metodoPagoRepository;

    @Override
    public MetodoPago resolverMetodoPago(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new BusinessException("El método de pago es obligatorio");
        }

        MetodoPago metodo = metodoPagoRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new BusinessException("Método de pago no reconocido: " + nombre));

        if (!Boolean.TRUE.equals(metodo.getActivo())) {
            throw new BusinessException("El método de pago '" + nombre + "' está deshabilitado");
        }

        return metodo;
    }

    @Override
    public List<TransaccionPago> procesarPagos(Comprobante comprobante, List<PagoDetalleDTO> pagos, BigDecimal montoEsperado) {
        if (pagos == null || pagos.isEmpty()) {
            throw new BusinessException("Debe especificar al menos un método de pago");
        }

        BigDecimal sumaPagos = pagos.stream()
                .map(PagoDetalleDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumaPagos.compareTo(montoEsperado) != 0) {
            throw new BusinessException(
                    "La suma de los pagos (" + sumaPagos + ") no coincide con el total a cobrar (" + montoEsperado + ")");
        }

        List<TransaccionPago> transacciones = new ArrayList<>();
        for (PagoDetalleDTO detalle : pagos) {
            MetodoPago metodo = resolverMetodoPago(detalle.getMetodoPago());

            transacciones.add(TransaccionPago.builder()
                    .comprobante(comprobante)
                    .metodoPago(metodo)
                    .monto(detalle.getMonto())
                    .build());
        }

        return transacciones;
    }
}