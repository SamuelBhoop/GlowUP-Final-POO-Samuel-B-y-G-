package produccion;

import comercio.Categoria;

import java.time.LocalDate;
import java.util.Objects;

public class Producto {
    private final String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private LocalDate fechaLanzamiento;
    private Categoria categoria;

    public Producto(String id, String nombre, String descripcion, double precio, int stock,
                    LocalDate fechaLanzamiento, Categoria categoria) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.fechaLanzamiento = fechaLanzamiento;
        this.categoria = categoria;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public LocalDate getFechaLanzamiento() { return fechaLanzamiento; }
    public void setFechaLanzamiento(LocalDate fechaLanzamiento) { this.fechaLanzamiento = fechaLanzamiento; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String resumen() {
        String catNombre = (categoria != null ? categoria.getNombre() : "-");
        return String.format("[%s] %s | $%,.2f | stock:%d | cat:%s",
                id, nombre, precio, stock, catNombre);
    }
}
