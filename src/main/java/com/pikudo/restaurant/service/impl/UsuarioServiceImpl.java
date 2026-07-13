package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.usuario.UsuarioRequestDTO;
import com.pikudo.restaurant.dto.usuario.UsuarioResponseDTO;
import com.pikudo.restaurant.dto.usuario.UsuarioUpdateRequestDTO;
import com.pikudo.restaurant.entity.Rol;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.mapper.UsuarioMapper;
import com.pikudo.restaurant.repository.RolRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException("El username '" + dto.getUsername() + "' ya está en uso");
        }

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new BusinessException("Rol no encontrado con id: " + dto.getRolId()));

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .dni(dto.getDni())
                .telefono(dto.getTelefono())
                .rol(rol)
                .estado(true)
                .build();

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarActivos() {
        return usuarioRepository.findByEstadoTrue().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return usuarioMapper.toDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioUpdateRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new BusinessException("Rol no encontrado con id: " + dto.getRolId()));

        usuario.setUsername(dto.getUsername());
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(rol);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }
}