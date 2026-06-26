
package com.pikudo.validation;

/**
 * Validador de reglas de negocio para Usuario.
 * Verifica que el username sea único y que el rol asignado exista
 * antes de registrar o actualizar el personal del local.
 * Es invocado desde la capa service antes de crear o actualizar un usuario.
 * Agregado por: [tu nombre] - Módulo de validaciones.
 */

import com.pikudo.dto.usuario.UsuarioRequestDTO;
import com.pikudo.exception.BusinessException;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.repository.RolRepository;
import com.pikudo.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class UsuarioValidator {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioValidator(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    // Valida los datos antes de crear un nuevo usuario
    public void validarParaCrear(UsuarioRequestDTO dto) {
        // Usa findByUsername().isPresent() en vez de existsByUsername, ya que ese método
        // no existe en el UsuarioRepository del proyecto y no debemos modificarlo.
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("Ya existe un usuario con el username: " + dto.getUsername());
        }
        validarRolExistente(dto.getRolId());
    }

    // Valida los datos antes de actualizar un usuario existente
    public void validarParaActualizar(Long usuarioId, UsuarioRequestDTO dto) {
        usuarioRepository.findByUsername(dto.getUsername())
                .filter(u -> !u.getId().equals(usuarioId))
                .ifPresent(u -> {
                    throw new BusinessException("Ya existe otro usuario con el username: " + dto.getUsername());
                });
        validarRolExistente(dto.getRolId());
    }

    // Verifica que el rol exista en base de datos
    private void validarRolExistente(Long rolId) {
        rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("El rol indicado no existe"));
    }
}
