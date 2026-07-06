package com.pikudo.config;

import com.pikudo.entity.Rol;
import com.pikudo.entity.Rol.TipoRol;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.RolRepository;
import com.pikudo.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeedConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeedConfig(UsuarioRepository usuarioRepository, 
                          RolRepository rolRepository, 
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Inicialización de Roles (Si la tabla está vacía)
        if (rolRepository.count() == 0) {
            System.out.println("⏳ Inicializando roles en la base de datos...");
            
            rolRepository.save(Rol.builder().nombre(TipoRol.ADMINISTRADOR).build());
            rolRepository.save(Rol.builder().nombre(TipoRol.CAJERO).build());
            rolRepository.save(Rol.builder().nombre(TipoRol.MOZO).build());
            rolRepository.save(Rol.builder().nombre(TipoRol.MOTORIZADO).build());

            System.out.println("✅ Roles iniciales creados correctamente.");
        }

        // 2. Inicialización del Administrador por defecto
        if (usuarioRepository.count() == 0) {
            System.out.println("⏳ Creando usuario administrador por defecto...");
            
            // Buscamos el rol usando el Enum
            Rol rolAdmin = rolRepository.findByNombre(TipoRol.ADMINISTRADOR)
                    .orElseThrow(() -> new RuntimeException("Error: Rol ADMINISTRADOR no encontrado."));

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            // Usamos el encoder para que la contraseña sea válida para Spring Security
            admin.setPassword(passwordEncoder.encode("Piscochi250")); 
            admin.setEstado(true);
            admin.setRol(rolAdmin);

            usuarioRepository.save(admin);
            System.out.println("✅ Administrador Jaider creado con éxito (User: jaider).");
        }
    }
}