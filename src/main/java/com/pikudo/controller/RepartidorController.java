package com.pikudo.controller;

import com.pikudo.dto.tracking.RepartidorEstadoDTO;
import com.pikudo.entity.Rol.TipoRol;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.impl.PresenciaRepartidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final UsuarioRepository usuarioRepository;
    private final PresenciaRepartidorService presenciaService;

    /**
     * Carga inicial del panel de caja: todos los usuarios con rol MOTORIZADO,
     * con su estado de conexion actual. El frontend usa esto al abrir la pantalla,
     * y despues se mantiene actualizado en tiempo real via /topic/repartidores/estado.
     */
    @GetMapping("/estado")
    public ResponseEntity<List<RepartidorEstadoDTO>> listarConEstado() {
        List<Usuario> repartidores = usuarioRepository.findByRol_Nombre(TipoRol.MOTORIZADO);

        List<RepartidorEstadoDTO> resultado = repartidores.stream()
                .map(u -> new RepartidorEstadoDTO(
                        u.getId(),
                        u.getUsername(),
                        presenciaService.estaConectado(u.getId())
                ))
                .toList();

        return ResponseEntity.ok(resultado);
    }
}