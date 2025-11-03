package produccion;

import operaciones.TrabajadorEsclavizado;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Fabrica {
    private final String id;
    private String pais;
    private String ciudad;
    private int capacidad;
    private String nivelAutomatizacion;

    private final Set<TrabajadorEsclavizado> personal = new HashSet<>();

    public Fabrica(String id, String pais, String ciudad, int capacidad, String nivelAutomatizacion) {
        this.id = Objects.requireNonNull(id);
        this.pais = Objects.requireNonNull(pais);
        this.ciudad = Objects.requireNonNull(ciudad);
        this.capacidad = capacidad;
        this.nivelAutomatizacion = Objects.requireNonNull(nivelAutomatizacion);
    }

    public String getId() { return id; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getNivelAutomatizacion() { return nivelAutomatizacion; }
    public void setNivelAutomatizacion(String nivelAutomatizacion) { this.nivelAutomatizacion = nivelAutomatizacion; }

    public Set<TrabajadorEsclavizado> getPersonal() { return Set.copyOf(personal); }

    // Relación bidireccional
    public void agregarTrabajador(TrabajadorEsclavizado t) {
        if (t == null) return;
        if (personal.add(t)) {
            t.asignarAFabrica(this);
        }
    }

    public void retirarTrabajador(TrabajadorEsclavizado t) {
        if (t == null) return;
        if (personal.remove(t)) {
            t.asignarAFabrica(null);
        }
    }

    @Override
    public String toString() {
        return "Fabrica{" +
                "id='" + id + '\'' +
                ", pais='" + pais + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", capacidad=" + capacidad +
                ", nivelAutomatizacion='" + nivelAutomatizacion + '\'' +
                '}';
    }
}
