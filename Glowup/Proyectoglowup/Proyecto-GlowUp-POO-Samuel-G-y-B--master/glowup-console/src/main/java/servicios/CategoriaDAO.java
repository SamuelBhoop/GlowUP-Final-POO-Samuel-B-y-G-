package servicios;

import config.Database;
import comercio.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    public void insertar(Categoria categoria) {
        String sql = "INSERT INTO categoria (id, nombre, descripcion) VALUES (?, ?, ?)";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getId());
            ps.setString(2, categoria.getNombre());
            ps.setString(3, categoria.getDescripcion());

            ps.executeUpdate();
            System.out.println("[DAO] Categoría insertada: " + categoria.getNombre());

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando categoría", e);
        }
    }

    public Optional<Categoria> buscarPorId(String id) {
        String sql = "SELECT * FROM categoria WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new Categoria(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando categoría", e);
        }

        return Optional.empty();
    }

    public List<Categoria> listarTodas() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria ORDER BY nombre";

        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(new Categoria(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando categorías", e);
        }

        return categorias;
    }
}