package servicios;

import comercio.Categoria;
import produccion.Producto;

import java.util.*;

public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public void agregarProducto(Producto p) {
        Optional<Producto> existente = productDAO.buscarPorId(p.getId());
        if (existente.isPresent()) {
            throw new RuntimeException("ID de producto en uso");
        }

        productDAO.insertar(p);
        System.out.println("[SERVICE] Producto agregado a BD: " + p.getNombre());
    }



    public Producto buscarPorId(String productoId) {
        Optional<Producto> producto = productDAO.buscarPorId(productoId);
        return producto.orElse(null);
    }

    public Producto buscarPorId(int productoId) {
        return buscarPorId(String.valueOf(productoId));
    }

    public Categoria obtenerOCrearCategoria(String nombre, String descripcion) {
        List<Categoria> categorias = categoriaDAO.listarTodas();
        Optional<Categoria> existente = categorias.stream()
                .filter(cat -> cat.getNombre().equalsIgnoreCase(nombre))
                .findFirst();

        if (existente.isPresent()) {
            return existente.get();
        } else {
            // Crear nueva categoría
            return crearCategoria(nombre, descripcion);
        }
    }

    public List<Producto> listarProductosOrdenadosPorNombre() {
        List<Producto> productos = productDAO.listarTodos();
        productos.sort(Comparator.comparing(Producto::getNombre));
        return productos;
    }

    public List<Producto> listarTodos() {
        return productDAO.listarTodos();
    }

    public Categoria obtenerCategoriaPorId(String id) {
        Optional<Categoria> categoria = categoriaDAO.buscarPorId(id);
        if (categoria.isEmpty()) {
            throw new RuntimeException("Categoría no encontrada: " + id);
        }
        return categoria.get();
    }

    public List<Categoria> listarCategorias() {
        return categoriaDAO.listarTodas();
    }

    public Categoria crearCategoria(String nombre, String descripcion) {
        String id = UUID.randomUUID().toString();
        Categoria cat = new Categoria(id, nombre, descripcion);
        categoriaDAO.insertar(cat);
        return cat;
    }
}