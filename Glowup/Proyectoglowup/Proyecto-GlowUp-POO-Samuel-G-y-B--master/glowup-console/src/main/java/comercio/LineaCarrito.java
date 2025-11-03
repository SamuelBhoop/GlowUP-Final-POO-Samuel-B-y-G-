package comercio;

import produccion.Producto;

public class LineaCarrito {
    private final Producto producto;
    private int cantidad;

    public LineaCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void incrementar(int extra) { this.cantidad += extra; }

    public double getSubtotal() { return producto.getPrecio() * cantidad; }

    @Override public String toString() {
        return producto.getNombre() + " x" + cantidad + " = $" + getSubtotal();
    }
}
