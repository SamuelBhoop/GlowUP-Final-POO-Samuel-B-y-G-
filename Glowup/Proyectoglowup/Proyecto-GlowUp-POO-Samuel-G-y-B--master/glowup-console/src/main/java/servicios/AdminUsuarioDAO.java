package servicios;

import config.Database;
import dominio.AdministradorUsuario;
import java.sql.*;
import java.util.*;

public class AdminUsuarioDAO {

    public void insertar(AdministradorUsuario admin) {
        System.out.println(" [DAO DEBUG] ===== INICIANDO INSERCIÓN ADMIN USUARIO =====");
        System.out.println(" [DAO DEBUG] ID: " + admin.getId());
        System.out.println(" [DAO DEBUG] Nombre: " + admin.getNombre());
        System.out.println(" [DAO DEBUG] Email: " + admin.getEmail());
        System.out.println(" [DAO DEBUG] PasswordHash: " + (admin.getPasswordHash() != null ? "***" : "NULL"));
        System.out.println(" [DAO DEBUG] Nivel Privilegio: " + admin.getNivelPrivilegio());
        System.out.println(" [DAO DEBUG] Permisos: " + admin.getPermisosAdministracion());

        // Verificar si el email ya existe
        if (existeEmail(admin.getEmail())) {
            throw new RuntimeException("El email '" + admin.getEmail() + "' ya está registrado");
        }

        String sqlUsuario = "INSERT INTO usuario (id, nombre, email, password, rol) VALUES (?, ?, ?, ?, ?)";
        String sqlAdmin = "INSERT INTO administrador_usuario (usuario_id, nivel_privilegio, puede_crear_usuarios, activo) VALUES (?, ?, ?, ?)";
        String sqlPermisos = "INSERT INTO admin_usuario_permisos (id, admin_id, permiso) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection()) {
            System.out.println(" [DAO DEBUG] Conexión a BD establecida");
            conn.setAutoCommit(false);

            try {
                // 1. Insertar en usuario
                System.out.println(" [DAO DEBUG] Insertando en tabla usuario...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                    stmt.setString(1, admin.getId());
                    stmt.setString(2, admin.getNombre());
                    stmt.setString(3, admin.getEmail());
                    stmt.setString(4, admin.getPasswordHash());  // ✅ CAMBIO AQUÍ
                    stmt.setString(5, admin.getRol().name());
                    int filas = stmt.executeUpdate();
                    System.out.println("✅ [DAO DEBUG] Usuario insertado: " + filas + " filas");
                }

                // 2. Insertar en administrador_usuario
                System.out.println(" [DAO DEBUG] Insertando en tabla administrador_usuario...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlAdmin)) {
                    stmt.setString(1, admin.getId());
                    stmt.setInt(2, admin.getNivelPrivilegio());
                    stmt.setBoolean(3, admin.puedeCrearUsuarios());
                    stmt.setBoolean(4, admin.isActivo());
                    int filas = stmt.executeUpdate();
                    System.out.println("✅ [DAO DEBUG] AdminUsuario insertado: " + filas + " filas");
                }

                // 3. Insertar permisos
                System.out.println(" [DAO DEBUG] Insertando permisos...");
                try (PreparedStatement stmt = conn.prepareStatement(sqlPermisos)) {
                    int contadorPermisos = 0;
                    for (String permiso : admin.getPermisosAdministracion()) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, admin.getId());
                        stmt.setString(3, permiso);
                        stmt.executeUpdate();
                        contadorPermisos++;
                        System.out.println("✅ [DAO DEBUG] Permiso insertado: " + permiso);
                    }
                    System.out.println("✅ [DAO DEBUG] Total permisos insertados: " + contadorPermisos);
                }

                conn.commit();
                System.out.println("🎉 [DAO DEBUG] TRANSACCIÓN COMPLETADA EXITOSAMENTE");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("❌ [DAO ERROR] Error en transacción - Rollback: " + e.getMessage());
                throw e;
            }

        } catch (SQLException e) {
            System.out.println("❌ [DAO ERROR] Error de conexión: " + e.getMessage());
            throw new RuntimeException("Error guardando AdminUsuario: " + e.getMessage(), e);
        }
    }

    // Método para verificar email
    private boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM usuario WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando email: " + e.getMessage(), e);
        }
    }

    public Optional<AdministradorUsuario> buscarPorId(String id) {
        String sql = """
            SELECT u.*, au.nivel_privilegio, au.puede_crear_usuarios, au.activo, au.fecha_asignacion
            FROM usuario u 
            JOIN administrador_usuario au ON u.id = au.usuario_id 
            WHERE u.id = ?
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Set<String> permisos = cargarPermisos(id);
                AdministradorUsuario admin = new AdministradorUsuario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("password"),
                        permisos,
                        rs.getInt("nivel_privilegio"),
                        rs.getBoolean("puede_crear_usuarios")
                );

                if (!rs.getBoolean("activo")) {
                    admin.desactivar();
                }

                return Optional.of(admin);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando AdminUsuario: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public Optional<AdministradorUsuario> buscarPorEmail(String email) {
        String sql = """
        SELECT u.id 
        FROM usuario u 
        JOIN administrador_usuario au ON u.id = au.usuario_id 
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
            throw new RuntimeException("Error buscando AdminUsuario por email: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    private Set<String> cargarPermisos(String adminId) {
        Set<String> permisos = new HashSet<>();
        String sql = "SELECT permiso FROM admin_usuario_permisos WHERE admin_id = ?";

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

    public List<AdministradorUsuario> listarTodos() {
        List<AdministradorUsuario> admins = new ArrayList<>();
        String sql = "SELECT id FROM usuario u JOIN administrador_usuario au ON u.id = au.usuario_id";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                buscarPorId(rs.getString("id")).ifPresent(admins::add);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando AdminUsuario: " + e.getMessage(), e);
        }

        return admins;
    }

    public void actualizar(AdministradorUsuario admin) {
        String sqlUsuario = "UPDATE usuario SET nombre = ?, email = ?, password = ? WHERE id = ?";
        String sqlAdmin = "UPDATE administrador_usuario SET nivel_privilegio = ?, puede_crear_usuarios = ?, activo = ? WHERE usuario_id = ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                    stmt.setString(1, admin.getNombre());
                    stmt.setString(2, admin.getEmail());
                    stmt.setString(3, admin.getPasswordHash());  // ✅ CAMBIO AQUÍ
                    stmt.setString(4, admin.getId());
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlAdmin)) {
                    stmt.setInt(1, admin.getNivelPrivilegio());
                    stmt.setBoolean(2, admin.puedeCrearUsuarios());
                    stmt.setBoolean(3, admin.isActivo());
                    stmt.setString(4, admin.getId());
                    stmt.executeUpdate();
                }

                String sqlDeletePermisos = "DELETE FROM admin_usuario_permisos WHERE admin_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDeletePermisos)) {
                    stmt.setString(1, admin.getId());
                    stmt.executeUpdate();
                }

                String sqlInsertPermisos = "INSERT INTO admin_usuario_permisos (id, admin_id, permiso) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsertPermisos)) {
                    for (String permiso : admin.getPermisosAdministracion()) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, admin.getId());
                        stmt.setString(3, permiso);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("[DAO] AdminUsuario actualizado: " + admin.getNombre());

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando AdminUsuario: " + e.getMessage(), e);
        }
    }

    public void eliminar(String id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            System.out.println("[DAO] AdminUsuario eliminado: " + id);

        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando AdminUsuario: " + e.getMessage(), e);
        }
    }
}