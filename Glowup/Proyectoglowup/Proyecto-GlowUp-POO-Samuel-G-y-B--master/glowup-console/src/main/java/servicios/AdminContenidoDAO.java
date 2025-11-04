package servicios;

import config.Database;
import dominio.AdministradorContenido;
import java.sql.*;
import java.util.*;

public class AdminContenidoDAO {


    public void insertar(AdministradorContenido admin) {
        System.out.println(" [DAO DEBUG] ===== INICIANDO INSERCIÓN =====");
        System.out.println(" [DAO DEBUG] ID: " + admin.getId());
        System.out.println(" [DAO DEBUG] Nombre: " + admin.getNombre());
        System.out.println(" [DAO DEBUG] Email: " + admin.getEmail());
        System.out.println(" [DAO DEBUG] PasswordHash: " + (admin.getPasswordHash() != null ? "***" : "NULL"));
        System.out.println(" [DAO DEBUG] Departamento: " + admin.getDepartamento());
        System.out.println(" [DAO DEBUG] Nivel: " + admin.getNivelAcceso());
        System.out.println(" [DAO DEBUG] Permisos: " + admin.getPermisosEdicion());

        String sqlUsuario = "INSERT INTO usuario (id, nombre, email, password, rol) VALUES (?, ?, ?, ?, ?)";
        String sqlAdmin = "INSERT INTO administrador_contenido (usuario_id, departamento, nivel_acceso, activo) VALUES (?, ?, ?, ?)";
        String sqlPermisos = "INSERT INTO admin_contenido_permisos (id, admin_id, permiso) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            System.out.println(" [DAO DEBUG] Conexión a BD establecida");
            conn.setAutoCommit(false);

            try {
                System.out.println(" [DAO DEBUG] Insertando en tabla usuario...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                    stmt.setString(1, admin.getId());
                    stmt.setString(2, admin.getNombre());
                    stmt.setString(3, admin.getEmail());
                    stmt.setString(4, admin.getPasswordHash());
                    stmt.setString(5, admin.getRol().name());
                    int filas = stmt.executeUpdate();
                    System.out.println("✅ [DAO DEBUG] Usuario insertado: " + filas + " filas");
                }

                System.out.println("🔍 [DAO DEBUG] Insertando en tabla administrador_contenido...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlAdmin)) {
                    stmt.setString(1, admin.getId());
                    stmt.setString(2, admin.getDepartamento());
                    stmt.setInt(3, admin.getNivelAcceso());
                    stmt.setBoolean(4, admin.isActivo());
                    int filas = stmt.executeUpdate();
                    System.out.println(" [DAO DEBUG] AdminContenido insertado: " + filas + " filas");
                }

                System.out.println(" [DAO DEBUG] Insertando permisos...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlPermisos)) {
                    int contadorPermisos = 0;
                    for (String permiso : admin.getPermisosEdicion()) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, admin.getId());
                        stmt.setString(3, permiso);
                        stmt.executeUpdate();
                        contadorPermisos++;
                        System.out.println(" [DAO DEBUG] Permiso insertado: " + permiso);
                    }
                    System.out.println(" [DAO DEBUG] Total permisos insertados: " + contadorPermisos);
                }

                conn.commit();
                System.out.println(" [DAO DEBUG] TRANSACCIÓN COMPLETADA EXITOSAMENTE");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println(" [DAO ERROR] Error en transacción - Rollback: " + e.getMessage());
                throw e;
            }

        } catch (SQLException e) {
            System.out.println(" [DAO ERROR] Error de conexión: " + e.getMessage());
            throw new RuntimeException("Error guardando AdminContenido: " + e.getMessage(), e);
        }
    }

    public Optional<AdministradorContenido> buscarPorId(String id) {
        String sql = """
            SELECT u.*, ac.departamento, ac.nivel_acceso, ac.activo, ac.fecha_contratacion
            FROM usuario u 
            JOIN administrador_contenido ac ON u.id = ac.usuario_id 
            WHERE u.id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Set<String> permisos = cargarPermisos(id);
                AdministradorContenido admin = new AdministradorContenido(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("password"),
                        permisos,
                        rs.getString("departamento"),
                        rs.getInt("nivel_acceso")
                );

                if (!rs.getBoolean("activo")) {
                    admin.desactivar();
                }

                return Optional.of(admin);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando AdminContenido: " + e.getMessage(), e);
        }

        return Optional.empty();
    }
    public Optional<AdministradorContenido> buscarPorEmail(String email) {
        String sql = """
        SELECT u.id 
        FROM usuario u 
        JOIN administrador_contenido ac ON u.id = ac.usuario_id 
        WHERE u.email = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return buscarPorId(rs.getString("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando AdminContenido por email: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    private Set<String> cargarPermisos(String adminId) {
        Set<String> permisos = new HashSet<>();
        String sql = "SELECT permiso FROM admin_contenido_permisos WHERE admin_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adminId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                permisos.add(rs.getString("permiso"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error cargando permisos: " + e.getMessage(), e);
        }

        return permisos;
    }

    public List<AdministradorContenido> listarTodos() {
        List<AdministradorContenido> admins = new ArrayList<>();
        String sql = "SELECT id FROM usuario u JOIN administrador_contenido ac ON u.id = ac.usuario_id";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                buscarPorId(rs.getString("id")).ifPresent(admins::add);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando AdminContenido: " + e.getMessage(), e);
        }

        return admins;
    }
}
