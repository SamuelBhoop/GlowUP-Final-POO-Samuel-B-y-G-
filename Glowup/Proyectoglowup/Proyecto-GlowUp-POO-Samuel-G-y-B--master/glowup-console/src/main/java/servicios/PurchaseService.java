package servicios;

import comercio.Carrito;
import comercio.Compra;
import comercio.LineaCarrito;
import comercio.LineaCompra;
import dominio.Cliente;
import pago.MetodoPago;
import produccion.Producto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PurchaseService {
    private final ProductService producto;
    private final List<Compra> historial = new ArrayList<>();

    public PurchaseService(ProductService productos) {
        this.producto = productos;
    }

    // Debes tener una instancia de ProductService en tu clase
    private ProductService productService = new ProductService();

    public Compra checkout(Cliente cliente, Carrito carrito, MetodoPago mp) {
        List<LineaCompra> lineasCompra = new ArrayList<>();

        for (LineaCarrito lc : carrito.getLineas()) {
            // Usar la instancia en lugar de la clase
            Producto p = productService.buscarPorId(lc.getProducto().getId());

            if (p.getStock() < lc.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + p.getNombre());
            }

            p.setStock(p.getStock() - lc.getCantidad());
            lineasCompra.add(new LineaCompra(p, lc.getCantidad(), p.getPrecio()));
        }

        Compra compra = new Compra(
                UUID.randomUUID().toString(),
                cliente,
                lineasCompra,
                mp
        );

        return compra;
    }

    private double calcularTotal(List<LineaCompra> lineasCompra) {
        return lineasCompra.stream()
                .mapToDouble(LineaCompra::getSubtotal)
                .sum();
    }
}