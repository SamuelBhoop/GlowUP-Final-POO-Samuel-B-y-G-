package servicios;
import config.Database;
import comercio.Categoria;
import produccion.Producto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class ProductDAO {
    public void insertar(Producto producto) {
        String sql = """
            INSERT INTO producto (id, nombre, descripcion, precio, stock, fecha_lanzamiento, categoria_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setDouble(4, producto.getPrecio());
            ps.setInt(5, producto.getStock());
            ps.setDate(6, Date.valueOf(producto.getFechaLanzamiento()));
            ps.setString(7, producto.getCategoria() != null ? producto.getCategoria().getId() : null);

            ps.executeUpdate();
            System.out.println("[DAO] Producto insertado: " + producto.getNombre());

        } catch (SQLException e) {
            throw new RuntimeException("Error insertando producto: " + e.getMessage(), e);
        }
    }

    public Optional<Producto> buscarPorId(String id) {
        String sql = "SELECT * FROM producto WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapearProducto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando producto: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public List<Producto> listarTodos() {

        List<Producto> productos = new ArrayList<>();
        String sql = """
  SELECT p.*, c.id AS cat_id, c.nombre AS cat_nombre, c.descripcion AS cat_desc
  FROM producto p
  LEFT JOIN categoria c ON c.id = p.categoria_id
  ORDER BY p.nombre
""";


        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando productos: " + e.getMessage(), e);
        }

        return productos;
    }

    public void actualizarStock(String id, int nuevoStock) {
        String sql = "UPDATE producto SET stock = ? WHERE id = ?";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevoStock);
            ps.setString(2, id);
            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("[DAO] Stock actualizado: " + id + " -> " + nuevoStock);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando stock: " + e.getMessage(), e);
        }
    }

    public void actualizarProducto(Producto producto) {
        String sql = """
            UPDATE producto 
            SET nombre = ?, descripcion = ?, precio = ?, stock = ?, 
                fecha_lanzamiento = ?, categoria_id = ? 
            WHERE id = ?
        """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setDouble(3, producto.getPrecio());
            ps.setInt(4, producto.getStock());
            ps.setDate(5, Date.valueOf(producto.getFechaLanzamiento()));
            ps.setString(6, producto.getCategoria() != null ? producto.getCategoria().getId() : null);
            ps.setString(7, producto.getId());

            ps.executeUpdate();
            System.out.println("[DAO] Producto actualizado: " + producto.getNombre());

        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando producto: " + e.getMessage(), e);
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                rs.getDate("fecha_lanzamiento").toLocalDate(),
                null
        );
    }
    public List<Producto> listarConCategoria() {
        List<Producto> productos = new ArrayList<>();
        String sql = """
        SELECT p.*,
               c.id          AS cat_id,
               c.nombre      AS cat_nombre,
               c.descripcion AS cat_desc
        FROM producto p
        LEFT JOIN categoria c ON c.id = p.categoria_id
        ORDER BY p.nombre
    """;

        try (Connection con = Database.getConnection();
             PreparedStatement st = con.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProductoConCategoria(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error listando productos: " + e.getMessage(), e);
        }
        return productos;
    }

    private Producto mapearProductoConCategoria(ResultSet rs) throws SQLException {
        java.sql.Date d = rs.getDate("fecha_lanzamiento");
        java.time.LocalDate fecha = (d != null) ? d.toLocalDate() : java.time.LocalDate.now();

        comercio.Categoria cat = null;
        String catId = rs.getString("cat_id");
        if (catId != null) {
            cat = new comercio.Categoria(
                    catId,
                    rs.getString("cat_nombre"),
                    rs.getString("cat_desc")
            );
        }

        return new produccion.Producto(
                rs.getString("id"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDouble("precio"),
                rs.getInt("stock"),
                fecha,
                cat
        );
    }

}
