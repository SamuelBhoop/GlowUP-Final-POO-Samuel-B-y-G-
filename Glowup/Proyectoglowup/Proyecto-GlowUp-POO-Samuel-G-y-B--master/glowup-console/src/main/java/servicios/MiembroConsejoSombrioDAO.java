package servicios;

import config.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MiembroConsejoSombrioDAO {

    public boolean agregar(String consejoId, String usuarioId) {
        String sql = "INSERT INTO consejo_sombrio_miembro (consejo_id, usuario_id) VALUES (?, ?)";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            ps.setString(2, usuarioId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error insertando miembro del consejo", e);
        }
    }

    public boolean eliminar(String consejoId, String usuarioId) {
        String sql = "DELETE FROM consejo_sombrio_miembro WHERE consejo_id = ? AND usuario_id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            ps.setString(2, usuarioId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando miembro del consejo", e);
        }
    }

    public boolean eliminarTodosPorConsejo(String consejoId) {
        String sql = "DELETE FROM consejo_sombrio_miembro WHERE consejo_id = ?";
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando miembros por consejo", e);
        }
    }

    public List<String> listarUsuariosPorConsejo(String consejoId) {
        String sql = "SELECT usuario_id FROM consejo_sombrio_miembro WHERE consejo_id = ?";
        List<String> ids = new ArrayList<>();
        try (Connection cn = Database.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, consejoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("usuario_id"));
            }
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException("Error listando ids de usuario del consejo", e);
        }
    }
}
