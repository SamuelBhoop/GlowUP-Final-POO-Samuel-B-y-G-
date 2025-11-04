package comercio;

import java.util.Objects;

public class Categoria {
    private final String id;
    private String nombre;
    private String descripcion;

    public Categoria(String id, String nombre, String descripcion) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
