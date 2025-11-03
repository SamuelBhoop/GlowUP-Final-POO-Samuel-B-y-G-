package operaciones;

import produccion.Fabrica;

import java.time.LocalDate;
import java.util.Objects;

public class TrabajadorEsclavizado {
    private final String id;
    private String nombre;
    private String paisOrigen;
    private int edad;
    private LocalDate fechaCaptura;
    private String salud;
    private Fabrica asignadoA; // many -> one

    public TrabajadorEsclavizado(String id, String nombre, String paisOrigen, int edad,
                                 LocalDate fechaCaptura, String salud) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.paisOrigen = Objects.requireNonNull(paisOrigen);
        this.edad = edad;
        this.fechaCaptura = Objects.requireNonNull(fechaCaptura);
        this.salud = Objects.requireNonNull(salud);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public LocalDate getFechaCaptura() { return fechaCaptura; }
    public void setFechaCaptura(LocalDate fechaCaptura) { this.fechaCaptura = fechaCaptura; }
    public String getSalud() { return salud; }
    public void setSalud(String salud) { this.salud = salud; }
    public Fabrica getAsignadoA() { return asignadoA; }

    public void asignarAFabrica(Fabrica f) { this.asignadoA = f; }

    @Override
    public String toString() {
        return "TrabajadorEsclavizado{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", paisOrigen='" + paisOrigen + '\'' +
                ", edad=" + edad +
                ", fechaCaptura=" + fechaCaptura +
                ", salud='" + salud + '\'' +
                ", asignadoA=" + (asignadoA != null ? asignadoA.getId() : "-") +
                '}';
    }
}
