package servicios;

import config.Database;
import dominio.AdministradorContenido;
import dominio.AdministradorUsuario;
import dominio.Rol;
import dominio.Usuario;
import operaciones.ConsejoSombrio;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dominio.Cliente;
import dominio.DesarrolladorProducto;
import dominio.Duena;
import dominio.UsuarioSimple;
public class ConsejoSombrioDAO {



    public void insertar(ConsejoSombrio c) {
        String sql = "INSERT INTO consejo_sombrio (id, nombre_clave) VALUES (?, ?)";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getNombreClave());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando consejo_sombrio", e);
        }
    }

    public ConsejoSombrio crear(String nombreClave) {
        ConsejoSombrio c = new ConsejoSombrio(UUID.randomUUID().toString(), nombreClave);
        insertar(c);
        return c;
    }

    public Optional<ConsejoSombrio> buscarPorId(String id) {
        String sql = "SELECT id, nombre_clave FROM consejo_sombrio WHERE id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ConsejoSombrio c = new ConsejoSombrio(
                            rs.getString("id"),
                            rs.getString("nombre_clave")
                    );
                    for (Usuario u : listarMiembros(id)) c.agregarMiembro(u);
                    return Optional.of(c);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando consejo_sombrio por id", e);
        }
    }

    public List<ConsejoSombrio> listar() {
        String sql = "SELECT id, nombre_clave FROM consejo_sombrio ORDER BY nombre_clave";
        List<ConsejoSombrio> lista = new ArrayList<>();
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                ConsejoSombrio c = new ConsejoSombrio(
                        id,
                        rs.getString("nombre_clave")
                );
                for (Usuario u : listarMiembros(id)) c.agregarMiembro(u);
                lista.add(c);
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando consejos: " + e.getMessage(), e);
        }
    }

    public boolean actualizarNombre(String id, String nuevoNombreClave) {
        String sql = "UPDATE consejo_sombrio SET nombre_clave = ? WHERE id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoNombreClave);
            ps.setString(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando consejo_sombrio", e);
        }
    }

    public boolean eliminar(String id) {
        String sql = "DELETE FROM consejo_sombrio WHERE id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando consejo_sombrio", e);
        }
    }


    public boolean agregarMiembro(String consejoId, String usuarioId) {
        Usuario u = buscarUsuarioPorId(usuarioId).orElseThrow(() -> new RuntimeException("Usuario no existe"));
        if (!(u instanceof AdministradorContenido) && !(u instanceof AdministradorUsuario)) {
            throw new IllegalArgumentException("Solo AdminContenido o AdminUsuario pueden ser miembros del Consejo");
        }
        String sql = "INSERT INTO consejo_sombrio_miembro (consejo_id, usuario_id) VALUES (?, ?)";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            ps.setString(2, usuarioId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando miembro al consejo", e);
        }
    }

    public boolean retirarMiembro(String consejoId, String usuarioId) {
        String sql = "DELETE FROM consejo_sombrio_miembro WHERE consejo_id = ? AND usuario_id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            ps.setString(2, usuarioId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error retirando miembro del consejo", e);
        }
    }

    public List<Usuario> listarMiembros(String consejoId) {
        String sql = """
  SELECT u.id, u.nombre, u.email, u.password, u.rol,
         NOW() AS fecha_registro, 1 AS activo
  FROM consejo_sombrio_miembro m
  JOIN usuario u ON u.id = m.usuario_id
  WHERE m.consejo_id = ?
  ORDER BY u.nombre
""";
        ;
        List<Usuario> miembros = new ArrayList<>();
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    miembros.add(mapearUsuario(rs));
                }
            }
            return miembros;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando miembros del consejo: " + e.getMessage(), e);
        }
    }


    public List<ConsejoSombrio> listarConsejosDeUsuario(String usuarioId) {
        String sql = """
                SELECT c.id, c.nombre_clave
                FROM consejo_sombrio c
                JOIN consejo_sombrio_miembro m ON m.consejo_id = c.id
                WHERE m.usuario_id = ?
                ORDER BY c.nombre_clave
                """;
        List<ConsejoSombrio> lista = new ArrayList<>();
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ConsejoSombrio(rs.getString("id"), rs.getString("nombre_clave")));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando consejos por usuario", e);
        }
    }


    private Optional<Usuario> buscarUsuarioPorId(String id) {
        String sql = """
    SELECT id, nombre, email, password, rol,
           NOW() AS fecha_registro, 1 AS activo
    FROM usuario
    WHERE id = ?
  """;
        try (var cn = Database.getConnection();
             var ps = cn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapearUsuario(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario: " + e.getMessage(), e);
        }
    }


    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String nombre = rs.getString("nombre");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password");
        Rol rol = Rol.valueOf(rs.getString("rol"));
        boolean activo = rs.getBoolean("activo");

         java.sql.Date f = rs.getDate("fecha_registro");

        Usuario u;
        switch (rol) {
            case ADMIN_CONTENIDO:
                u = new AdministradorContenido(id, nombre, email, passwordHash, (java.util.Set<String>) null);
                break;
            case ADMIN_USUARIO:
                u = new AdministradorUsuario(id, nombre, email, passwordHash, 1);
                break;
            case CLIENTE:
                u = new Cliente(id, nombre, email, passwordHash, "", "");
                break;
            case DESARROLLADOR_PRODUCTO:
                u = new DesarrolladorProducto(id, nombre, email, passwordHash, "GENERAL");
                break;
            case DUENA:
                u = new Duena(id, nombre, email, passwordHash, "CLAVE", java.time.LocalDate.now());
                break;
            default:
                u = new UsuarioSimple(id, nombre, email, passwordHash, rol);
        }

        if (!activo) u.suspender();
        return u;
    }

}
