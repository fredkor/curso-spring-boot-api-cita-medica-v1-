package mx.com.fredkor.security.bootstrap;

import mx.com.fredkor.security.persistence.entity.PermisoEntity;
import mx.com.fredkor.security.persistence.entity.RolEntity;
import mx.com.fredkor.security.persistence.repository.PermisoRepository;
import mx.com.fredkor.security.persistence.repository.RolRepository;
import mx.com.fredkor.security.properties.SecurityProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Synchronizes the roles declared under {@code fredkor.security.roles} with the
 * security tables when the application starts.
 */
public class RolesInitializer implements ApplicationRunner {
    private final SecurityProperties properties;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolesInitializer(SecurityProperties properties, RolRepository rolRepository,
            PermisoRepository permisoRepository) {
        this.properties = properties;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        properties.getRoles().forEach(this::synchronizeRole);
    }

    private void synchronizeRole(String roleKey, List<String> configuredPermissions) {
        RolEntity role = rolRepository.findByClaveIgnoreCase(roleKey)
                .orElseGet(() -> newRole(roleKey));

        var permissions = new LinkedHashSet<PermisoEntity>();
        if (configuredPermissions != null) {
            configuredPermissions.stream()
                    .distinct()
                    .map(this::findOrCreatePermission)
                    .forEach(permissions::add);
        }

        role.setActivo(true);
        role.setPermisos(permissions);
        rolRepository.save(role);
    }

    private PermisoEntity findOrCreatePermission(String permissionKey) {
        return permisoRepository.findByClaveIgnoreCase(permissionKey)
                .map(permission -> {
                    permission.setActivo(true);
                    return permission;
                })
                .orElseGet(() -> permisoRepository.save(newPermission(permissionKey)));
    }

    private RolEntity newRole(String key) {
        RolEntity role = new RolEntity();
        role.setClave(key);
        role.setDescripcion(key);
        role.setActivo(true);
        return role;
    }

    private PermisoEntity newPermission(String key) {
        PermisoEntity permission = new PermisoEntity();
        permission.setClave(key);
        permission.setDescripcion(key);
        permission.setActivo(true);
        return permission;
    }
}
