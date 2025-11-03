package servicios;

import operaciones.ConsejoSombrio;

import java.util.*;

public class ConsejoSombrioService {
    private final Map<String, ConsejoSombrio> consejos = new HashMap<>();

    public ConsejoSombrio crear(String id, String nombreClave) {
        ConsejoSombrio c = new ConsejoSombrio(id, nombreClave);
        consejos.put(id, c);
        return c;
    }

    public ConsejoSombrio buscar(String id) {
        ConsejoSombrio c = consejos.get(id);
        if (c == null) throw new RuntimeException("Consejo no encontrado");
        return c;
    }

    public List<ConsejoSombrio> listar() {
        return new ArrayList<>(consejos.values());
    }
}
