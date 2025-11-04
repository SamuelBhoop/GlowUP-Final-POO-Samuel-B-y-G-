package comercio;

import produccion.Producto;

import java.util.*;

public class Carrito {
    private final String id;
    private final List<LineaCarrito> lineas = new ArrayList<>();

    public Carrito(String id) { this.id = id; }

    public String getId() { return id; }
    public List<LineaCarrito> getLineas() { return Collections.unmodifiableList(lineas); }

    public void agregarProducto(Producto p, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        Optional<LineaCarrito> existente = lineas.stream()
                .filter(l -> l.getProducto().getId().equals(p.getId()))
                .findFirst();
        if (existente.isPresent()) {
            existente.get().incrementar(cantidad);
        } else {
            lineas.add(new LineaCarrito(p, cantidad));
        }
    }

    public double getTotal() {
        return lineas.stream().mapToDouble(LineaCarrito::getSubtotal).sum();
    }

    public void limpiar() { lineas.clear(); }
}
