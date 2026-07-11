package com.pikudo.config;

import com.pikudo.entity.Rol;
import com.pikudo.entity.Rol.TipoRol;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.RolRepository;
import com.pikudo.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class DataSeedConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean adminSeedEnabled;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminNombre;
    private final String adminApellido;
    private final String adminDni;
    private final String adminTelefono;

    public DataSeedConfig(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.admin.enabled:false}") boolean adminSeedEnabled,
            @Value("${app.seed.admin.username:}") String adminUsername,
            @Value("${app.seed.admin.password:}") String adminPassword,
            @Value("${app.seed.admin.nombre:}") String adminNombre,
            @Value("${app.seed.admin.apellido:}") String adminApellido,
            @Value("${app.seed.admin.dni:}") String adminDni,
            @Value("${app.seed.admin.telefono:}") String adminTelefono
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminSeedEnabled = adminSeedEnabled;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminNombre = adminNombre;
        this.adminApellido = adminApellido;
        this.adminDni = adminDni;
        this.adminTelefono = adminTelefono;
    }

    @Override
    public void run(String... args) {
        for (TipoRol tipoRol : TipoRol.values()) {
            rolRepository.findByNombre(tipoRol)
                    .orElseGet(() -> rolRepository.save(Rol.builder().nombre(tipoRol).build()));
        }

        if (!adminSeedEnabled) {
            log.info("Seed de administrador deshabilitado para este entorno");
            return;
        }

        validateAdminSeedConfig();

        if (usuarioRepository.findByUsername(adminUsername).isPresent()) {
            log.info("El usuario administrador inicial '{}' ya existe", adminUsername);
            return;
        }

        Rol rolAdmin = rolRepository.findByNombre(TipoRol.ADMINISTRADOR)
                .orElseThrow(() -> new IllegalStateException("Rol ADMINISTRADOR no encontrado."));

        Usuario admin = new Usuario();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setNombre(adminNombre);
        admin.setApellido(adminApellido);
        admin.setDni(adminDni);
        admin.setTelefono(adminTelefono);
        admin.setEstado(true);
        admin.setRol(rolAdmin);

        usuarioRepository.save(admin);
        log.info("Usuario administrador inicial '{}' creado", adminUsername);
    }

    private void validateAdminSeedConfig() {
        if (!StringUtils.hasText(adminUsername)
                || !StringUtils.hasText(adminPassword)
                || !StringUtils.hasText(adminNombre)
                || !StringUtils.hasText(adminApellido)
                || !StringUtils.hasText(adminDni)
                || !StringUtils.hasText(adminTelefono)) {
            throw new IllegalStateException(
                    "ADMIN_SEED_ENABLED=true requiere ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_NOMBRE, ADMIN_APELLIDO, ADMIN_DNI y ADMIN_TELEFONO"
            );
        }
    }
}
