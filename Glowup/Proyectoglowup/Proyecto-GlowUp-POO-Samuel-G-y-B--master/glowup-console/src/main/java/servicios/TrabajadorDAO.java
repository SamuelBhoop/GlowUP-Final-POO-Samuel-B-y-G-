package servicios;

import config.Database;
import operaciones.TrabajadorEsclavizado;
import produccion.Fabrica;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrabajadorDAO {

    public void insertar(TrabajadorEsclavizado trabajador) {
        String sql = """
            INSERT INTO trabajador (id, nombre, pais_origen, edad, fecha_captura, salud, fabrica_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trabajador.getId());
            ps.setString(2, trabajador.getNombre());
            ps.setString(3, trabajador.getPaisOrigen());
            ps.setInt(4, trabajador.getEdad());
            ps.setDate(5, Date.valueOf(trabajador.getFechaCaptura()));
            ps.setString(6, trabajador.getSalud());
            ps.setString(7, trabajador.getAsignadoA() != null ? trabajador.getAsignadoA().getId() : null);

            ps.executeUpdate();
            System.out.println("[DAO] Trabajador insertado: " + trabajador.getNombre());

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando trabajador: " + e.getMessage(), e);
        }
    }

    public Optional<TrabajadorEsclavizado> buscarPorId(String id) {
        String sql = "SELECT * FROM trabajador WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearTrabajador(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando trabajador: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public List<TrabajadorEsclavizado> listarTodos() {
        List<TrabajadorEsclavizado> trabajadores = new ArrayList<>();
        String sql = "SELECT * FROM trabajador ORDER BY nombre";

        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                trabajadores.add(mapearTrabajador(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando trabajadores: " + e.getMessage(), e);
        }

        return trabajadores;
    }

    public void actualizarAsignacion(String trabajadorId, String fabricaId) {
        String sql = "UPDATE trabajador SET fabrica_id = ? WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fabricaId);
            ps.setString(2, trabajadorId);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("[DAO] Trabajador asignado: " + trabajadorId + " -> fábrica " + fabricaId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando asignación: " + e.getMessage(), e);
        }
    }

    public void actualizarTrabajador(TrabajadorEsclavizado trabajador) {
        String sql = """
            UPDATE trabajador 
            SET nombre = ?, pais_origen = ?, edad = ?, fecha_captura = ?, salud = ?, fabrica_id = ?
            WHERE id = ?
        """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trabajador.getNombre());
            ps.setString(2, trabajador.getPaisOrigen());
            ps.setInt(3, trabajador.getEdad());
            ps.setDate(4, Date.valueOf(trabajador.getFechaCaptura()));
            ps.setString(5, trabajador.getSalud());
            ps.setString(6, trabajador.getAsignadoA() != null ? trabajador.getAsignadoA().getId() : null);
            ps.setString(7, trabajador.getId());

            ps.executeUpdate();
            System.out.println("[DAO] Trabajador actualizado: " + trabajador.getNombre());

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando trabajador: " + e.getMessage(), e);
        }
    }

    private TrabajadorEsclavizado mapearTrabajador(ResultSet rs) throws SQLException {
        TrabajadorEsclavizado trabajador = new TrabajadorEsclavizado(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("pais_origen"),
                rs.getInt("edad"),
                rs.getDate("fecha_captura").toLocalDate(),
                rs.getString("salud")
        );

        return trabajador;
    }
}