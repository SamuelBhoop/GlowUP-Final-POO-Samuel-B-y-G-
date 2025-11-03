package dominio;

import produccion.Producto;
import comercio.Categoria;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AdministradorContenido extends Usuario {
    private final Set<String> permisosEdicion = new HashSet<>();
    private final LocalDateTime fechaContratacion;
    private String departamento;
    private int nivelAcceso;
    private boolean activo;

    public AdministradorContenido(String id, String nombre, String email, String password, Set<String> permisos) {
        super(id, nombre, email, password, Rol.ADMIN_CONTENIDO);
        if (permisos != null) permisosEdicion.addAll(permisos);
        this.fechaContratacion = LocalDateTime.now();
        this.departamento = "Contenido";
        this.nivelAcceso = 1;
        this.activo = true;
    }

    public AdministradorContenido(String id, String nombre, String email, String password,
                                  Set<String> permisos, String departamento, int nivelAcceso) {
        super(id, nombre, email, password, Rol.ADMIN_CONTENIDO);
        if (permisos != null) permisosEdicion.addAll(permisos);
        this.fechaContratacion = LocalDateTime.now();
        this.departamento = departamento;
        this.nivelAcceso = nivelAcceso;
        this.activo = true;
    }

    // === MÉTODOS DE VERIFICACIÓN DE PERMISOS ===
    public boolean puedeCrearProductos() {
        return permisosEdicion.contains("CREATE") && activo;
    }

    public boolean puedeModificarProductos() {
        return permisosEdicion.contains("UPDATE") && activo;
    }

    public boolean puedeEliminarProductos() {
        return permisosEdicion.contains("DELETE") && activo && nivelAcceso >= 2;
    }

    public boolean puedeGestionarCategorias() {
        return permisosEdicion.contains("CATEGORY_MGMT") && activo;
    }

    public boolean puedeModificarPrecios() {
        return permisosEdicion.contains("PRICE_EDIT") && activo && nivelAcceso >= 2;
    }

    public boolean puedeGestionarPromociones() {
        return permisosEdicion.contains("PROMOTION_MGMT") && activo;
    }

    public boolean puedeRevisarContenido() {
        return permisosEdicion.contains("CONTENT_REVIEW") && activo;
    }

    // === MÉTODOS DE GESTIÓN DE PERMISOS ===
    public void agregarPermiso(String permiso) {
        if (activo) {
            permisosEdicion.add(permiso.toUpperCase());
        }
    }

    public void removerPermiso(String permiso) {
        permisosEdicion.remove(permiso.toUpperCase());
    }

    public boolean tienePermiso(String permiso) {
        return permisosEdicion.contains(permiso.toUpperCase()) && activo;
    }

    // === MÉTODOS DE VALIDACIÓN PARA OPERACIONES ===
    public void validarPermisoCreacion() {
        if (!puedeCrearProductos()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para crear productos");
        }
    }

    public void validarPermisoEliminacion() {
        if (!puedeEliminarProductos()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para eliminar productos. Nivel requerido: 2");
        }
    }

    public void validarPermisoModificacionPrecios() {
        if (!puedeModificarPrecios()) {
            throw new SecurityException("Admin " + getNombre() + " no tiene permisos para modificar precios. Nivel requerido: 2");
        }
    }

    // === MÉTODOS DE GESTIÓN DE ESTADO ===
    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void promover(int nuevoNivel) {
        if (nuevoNivel > this.nivelAcceso) {
            this.nivelAcceso = nuevoNivel;
        }
    }

    public void cambiarDepartamento(String nuevoDepartamento) {
        this.departamento = nuevoDepartamento;
    }

    // === MÉTODOS DE AUDITORÍA ===
    public String getInfoAdministrador() {
        return String.format(
                "AdminContenido: %s | Depto: %s | Nivel: %d | Activo: %s | Permisos: %d",
                getNombre(), departamento, nivelAcceso, activo ? "Sí" : "No", permisosEdicion.size()
        );
    }

    public void generarReporteActividad() {
        System.out.println("=== REPORTE ADMIN CONTENIDO ===");
        System.out.println("Nombre: " + getNombre());
        System.out.println("Email: " + getEmail());
        System.out.println("Departamento: " + departamento);
        System.out.println("Nivel Acceso: " + nivelAcceso);
        System.out.println("Fecha Contratación: " + fechaContratacion);
        System.out.println("Estado: " + (activo ? "ACTIVO" : "INACTIVO"));
        System.out.println("Permisos: " + String.join(", ", permisosEdicion));
    }

    // === GETTERS ===
    public Set<String> getPermisosEdicion() {
        return new HashSet<>(permisosEdicion); // Copia defensiva
    }

    public LocalDateTime getFechaContratacion() {
        return fechaContratacion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public int getNivelAcceso() {
        return nivelAcceso;
    }

    public boolean isActivo() {
        return activo;
    }

    // === MÉTODO TO STRING MEJORADO ===
    @Override
    public String toString() {
        return String.format(
                "AdministradorContenido[id=%s, nombre=%s, depto=%s, nivel=%d, activo=%s]",
                getId(), getNombre(), departamento, nivelAcceso, activo
        );
    }
}