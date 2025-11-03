package dominio;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AdministradorUsuario extends Usuario {
    private final Set<String> permisosAdministracion = new HashSet<>();
    private final LocalDateTime fechaAsignacion;
    private int nivelPrivilegio; // 1..5
    private boolean puedeCrearUsuarios;
    private boolean activo;

    public AdministradorUsuario(String id, String nombre, String email, String password,
                                Set<String> permisos, int nivelPrivilegio, boolean puedeCrearUsuarios) {
        super(id, nombre, email, password, Rol.ADMIN_USUARIO);
        if (permisos != null) permisosAdministracion.addAll(permisos);
        this.fechaAsignacion = LocalDateTime.now();
        this.nivelPrivilegio = nivelPrivilegio;
        this.puedeCrearUsuarios = puedeCrearUsuarios;
        this.activo = true;
    }

    public AdministradorUsuario(String id, String nombre, String email, String password, int nivelPrivilegio) {
        this(id, nombre, email, password, new HashSet<>(), nivelPrivilegio, false);
    }

    public boolean puedeCrearAdministradores() {
        return permisosAdministracion.contains("CREATE_ADMIN") && activo && nivelPrivilegio >= 3;
    }

    public boolean puedeModificarUsuarios() {
        return permisosAdministracion.contains("UPDATE_USERS") && activo;
    }

    public boolean puedeEliminarUsuarios() {
        return permisosAdministracion.contains("DELETE_USERS") && activo && nivelPrivilegio >= 4;
    }

    public boolean puedeResetearPasswords() {
        return permisosAdministracion.contains("RESET_PASSWORDS") && activo;
    }

    public boolean puedeAsignarRoles() {
        return permisosAdministracion.contains("ASSIGN_ROLES") && activo && nivelPrivilegio >= 2;
    }

    public boolean puedeGestionarPermisos() {
        return permisosAdministracion.contains("MANAGE_PERMISSIONS") && activo && nivelPrivilegio >= 3;
    }

    public boolean puedeAuditarActividad() {
        return permisosAdministracion.contains("AUDIT_ACTIVITY") && activo;
    }

    public boolean puedeSuspenderUsuarios() {
        return permisosAdministracion.contains("SUSPEND_USERS") && activo && nivelPrivilegio >= 2;
    }

    public void agregarPermiso(String permiso) {
        if (activo) {
            permisosAdministracion.add(permiso.toUpperCase());
        }
    }

    public void removerPermiso(String permiso) {
        permisosAdministracion.remove(permiso.toUpperCase());
    }

    public boolean tienePermiso(String permiso) {
        return permisosAdministracion.contains(permiso.toUpperCase()) && activo;
    }

    public void validarPermisoCreacionUsuarios() {
        if (!puedeCrearUsuarios()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para crear usuarios");
        }
    }

    public void validarPermisoEliminacionUsuarios() {
        if (!puedeEliminarUsuarios()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para eliminar usuarios. Nivel requerido: 4");
        }
    }

    public void validarPermisoAsignacionRoles() {
        if (!puedeAsignarRoles()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para asignar roles. Nivel requerido: 2");
        }
    }

    public void validarPermisoGestionPermisos() {
        if (!puedeGestionarPermisos()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para gestionar permisos. Nivel requerido: 3");
        }
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void promover(int nuevoNivel) {
        if (nuevoNivel > this.nivelPrivilegio && nuevoNivel <= 5) {
            this.nivelPrivilegio = nuevoNivel;
        }
    }

    public void habilitarCreacionUsuarios() {
        if (nivelPrivilegio >= 2) {
            this.puedeCrearUsuarios = true;
        }
    }

    public void deshabilitarCreacionUsuarios() {
        this.puedeCrearUsuarios = false;
    }

    public String getInfoAdministrador() {
        return String.format(
                "AdminUsuario: %s | Nivel: %d | Crear Usuarios: %s | Activo: %s | Permisos: %d",
                getNombre(), nivelPrivilegio, puedeCrearUsuarios ? "Sí" : "No",
                activo ? "Sí" : "No", permisosAdministracion.size()
        );
    }

    public void generarReporteActividad() {
        System.out.println("=== REPORTE ADMIN USUARIO ===");
        System.out.println("Nombre: " + getNombre());
        System.out.println("Email: " + getEmail());
        System.out.println("Nivel Privilegio: " + nivelPrivilegio);
        System.out.println("Puede Crear Usuarios: " + (puedeCrearUsuarios ? "SÍ" : "NO"));
        System.out.println("Fecha Asignación: " + fechaAsignacion);
        System.out.println("Estado: " + (activo ? "ACTIVO" : "INACTIVO"));
        System.out.println("Permisos: " + String.join(", ", permisosAdministracion));
    }

    public void auditarOperacion(String operacion, String objetivo) {
        System.out.printf("[AUDITORÍA] %s realizó '%s' sobre '%s' a las %s%n",
                getNombre(), operacion, objetivo, LocalDateTime.now());
    }

    public boolean puedeAdministrarUsuario(Usuario usuario) {
        if (!activo) return false;

        if (usuario instanceof AdministradorUsuario) {
            AdministradorUsuario otroAdmin = (AdministradorUsuario) usuario;
            return this.nivelPrivilegio > otroAdmin.nivelPrivilegio;
        }

        return true;
    }

    public Set<String> getPermisosAdministracion() {
        return new HashSet<>(permisosAdministracion);
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public int getNivelPrivilegio() {
        return nivelPrivilegio;
    }

    public boolean puedeCrearUsuarios() {
        return puedeCrearUsuarios && activo && nivelPrivilegio >= 2;
    }


    public boolean isActivo() {
        return activo;
    }

    public void setNivelPrivilegio(int nivelPrivilegio) {
        if (nivelPrivilegio >= 1 && nivelPrivilegio <= 5) {
            this.nivelPrivilegio = nivelPrivilegio;
        }
    }

    public void setPuedeCrearUsuarios(boolean puedeCrearUsuarios) {
        if (nivelPrivilegio >= 2) {
            this.puedeCrearUsuarios = puedeCrearUsuarios;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "AdministradorUsuario[id=%s, nombre=%s, nivel=%d, crearUsuarios=%s, activo=%s]",
                getId(), getNombre(), nivelPrivilegio, puedeCrearUsuarios, activo
        );
    }
}