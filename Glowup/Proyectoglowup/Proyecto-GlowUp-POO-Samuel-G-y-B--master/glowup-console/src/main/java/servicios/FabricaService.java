package servicios;

import produccion.Fabrica;
import operaciones.TrabajadorEsclavizado;

import java.util.*;

public class FabricaService {
    private final Map<String, Fabrica> fabricas = new HashMap<>(); // id -> Fabrica

    public Fabrica crear(String id, String pais, String ciudad, int capacidad, String nivelAutomatizacion) {
        Fabrica f = new Fabrica(id, pais, ciudad, capacidad, nivelAutomatizacion);
        fabricas.put(f.getId(), f);
        return f;
    }

    public List<Fabrica> listar() { return new ArrayList<>(fabricas.values()); }

    public Fabrica buscar(String id) {
        Fabrica f = fabricas.get(id);
        if (f == null) throw new RuntimeException("Fábrica no encontrada");
        return f;
    }

    public void asignar(String fabricaId, TrabajadorEsclavizado t) {
        Fabrica f = buscar(fabricaId);
        f.agregarTrabajador(t);
    }
}
