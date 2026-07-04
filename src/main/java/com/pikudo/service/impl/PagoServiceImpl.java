package com.pikudo.service.impl;

import com.pikudo.entity.Pedido;
import com.pikudo.entity.caja.MetodoPago;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.MetodoPagoRepository;
import com.pikudo.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public void aplicarMetodoPago(Pedido pedido, String nombreMetodoPago) {
        MetodoPago metodo = resolverMetodoPago(nombreMetodoPago);
        pedido.setMetodoPago(metodo);
    }
}