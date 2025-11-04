package servicios;

import comercio.Carrito;
import comercio.LineaCarrito;
import dominio.Cliente;
import produccion.Producto;

import java.util.List;

public class CartService {
    private final CarritoDAO carritoDAO = new CarritoDAO();
    private ProductService productService;
    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public Carrito obtenerCarrito(Cliente cliente) {
        return carritoDAO.obtenerOCrearCarritoPorClienteId(cliente.getId());
    }

    public void agregarProductoAlCarrito(Cliente cliente, String productoId, int cantidad) {
        Producto producto = productService.buscarPorId(productoId);
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre()
                    + " (Pedido: " + cantidad + ", Stock: " + producto.getStock() + ")");
        }

        Carrito carrito = obtenerCarrito(cliente);
        carrito.agregarProducto(producto, cantidad);

        int nuevaCantidadTotal = carrito.getLineas().stream()
                .filter(linea -> linea.getProducto().getId().equals(productoId))
                .findFirst()
                .map(LineaCarrito::getCantidad)
                .orElse(0);

        carritoDAO.guardarLineaCarrito(carrito.getId(), productoId, nuevaCantidadTotal);

        System.out.println("[CART SERVICE] Producto agregado al carrito persistente: " +
                producto.getNombre() + " x" + cantidad + " para cliente: " + cliente.getNombre());
    }

    public List<LineaCarrito> obtenerLineasCarrito(Cliente cliente) {
        Carrito carrito = obtenerCarrito(cliente);
        return carrito.getLineas();
    }

    public double obtenerTotalCarrito(Cliente cliente) {
        Carrito carrito = obtenerCarrito(cliente);
        return carrito.getTotal();
    }

    public void vaciarCarrito(Cliente cliente) {
        Carrito carrito = obtenerCarrito(cliente);
        carritoDAO.limpiarCarrito(carrito.getId());
        carrito.limpiar();

        System.out.println("[CART SERVICE] Carrito vaciado para cliente: " + cliente.getNombre());
    }
}