package servicios;

import config.Database;
import produccion.Fabrica;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FabricaDAO {

    public void insertar(Fabrica fabrica) {
        String sql = "INSERT INTO fabrica (id, pais, ciudad, capacidad, nivel_automatizacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fabrica.getId());
            ps.setString(2, fabrica.getPais());
            ps.setString(3, fabrica.getCiudad());
            ps.setInt(4, fabrica.getCapacidad());
            ps.setString(5, fabrica.getNivelAutomatizacion());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando fábrica", e);
        }
    }

    public Optional<Fabrica> buscarPorId(String id) {
        String sql = "SELECT * FROM fabrica WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(new Fabrica(
                        rs.getString("id"),
                        rs.getString("pais"),
                        rs.getString("ciudad"),
                        rs.getInt("capacidad"),
                        rs.getString("nivel_automatizacion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando fábrica", e);
        }

        return Optional.empty();
    }

    public List<Fabrica> listarTodos() {
        List<Fabrica> fabricas = new ArrayList<>();
        String sql = "SELECT * FROM fabrica ORDER BY id";

        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                fabricas.add(new Fabrica(
                        rs.getString("id"),
                        rs.getString("pais"),
                        rs.getString("ciudad"),
                        rs.getInt("capacidad"),
                        rs.getString("nivel_automatizacion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando fábricas", e);
        }

        return fabricas;
    }
}