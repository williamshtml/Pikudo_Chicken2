package com.pikudo.security;

import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service

public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UsuarioRepository usuarioRepository;

    /*
     Conecta Spring Security con tu base de datos usando el método findByUsername de tu compañero.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos al usuario en la BD. Si no existe, lanzamos un error de seguridad controlado.
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el username: " + username));

        // Extraemos el rol de nuestra entidad y lo convertimos en una autoridad de Spring Security
        String nombreRol = usuario.getRol() != null ? usuario.getRol().getNombre().name() : "MOZO";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + nombreRol);

        // Retornamos el User estándar de Spring Security con los datos de nuestra base de datos
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getEstado(), // Si el estado es false, Spring bloqueará el inicio de sesión
                true, // Cuenta no expirada
                true, // Credenciales no expiradas
                true, // Cuenta no bloqueada
                Collections.singletonList(authority) // Lista con el rol del usuario
        );
    }
}
