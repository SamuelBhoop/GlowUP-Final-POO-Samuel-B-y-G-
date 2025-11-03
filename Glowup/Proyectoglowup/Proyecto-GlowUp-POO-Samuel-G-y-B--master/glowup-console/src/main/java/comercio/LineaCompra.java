package comercio;

import produccion.Producto;

import java.util.List;

public class LineaCompra {
    private final Producto producto;
    private final int cantidad;
    private final double precioUnit;

    public LineaCompra(Producto producto, int cantidad, double precioUnit) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnit = precioUnit;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnit() { return precioUnit; }
    public double getSubtotal() { return precioUnit * cantidad; }

    private double calcularTotal(List<LineaCompra> lineasCompra) {
        return lineasCompra.stream()
                .mapToDouble(LineaCompra::getSubtotal)  // Esta es la mejor opción
                .sum();
    }
}
