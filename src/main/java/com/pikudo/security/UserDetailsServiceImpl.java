package com.pikudo.security;

import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor; // <--- Usamos Lombok
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor // <--- Constructor limpio, sin @Autowired
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Buscamos el usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Extraemos el rol de forma segura
        String nombreRol = (usuario.getRol() != null) ? usuario.getRol().getNombre().name() : "MOZO";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + nombreRol);

        // 3. Retornamos el User de Spring con la validación de estado activa
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getEstado(), // Si es false, el usuario no puede loguearse
                true, 
                true, 
                true, 
                Collections.singletonList(authority)
        );
    }
}