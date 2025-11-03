package servicios;

import config.Database;
import dominio.Cliente;
import dominio.Rol;
import dominio.Usuario;

import java.sql.*;

public class UsuarioDAO {

    public void insertarCliente(Cliente cliente) {
        String sql = "INSERT INTO usuario (id, nombre, email, password, rol,direccion_envio, telefono) VALUES (?, ?, ?, ?, ?,?,?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getId());
            stmt.setString(2, cliente.getNombre());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getPasswordHash());
            stmt.setString(5, Rol.CLIENTE.name());
            stmt.setString(6, cliente.getDireccionEnvio());
            stmt.setString(7, cliente.getTelefono());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando cliente: " + e.getMessage(), e);
        }
    }

    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("direccion_envio"), rs.getString("telefono")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando usuario por email", e);
        }
    }
}