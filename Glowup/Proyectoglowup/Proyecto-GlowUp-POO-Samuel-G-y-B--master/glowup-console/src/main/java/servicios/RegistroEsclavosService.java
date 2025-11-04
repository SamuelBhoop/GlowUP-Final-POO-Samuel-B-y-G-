package servicios;

import dominio.Duena;
import operaciones.RegistroEsclavos;

import java.util.HashMap;
import java.util.Map;

public class RegistroEsclavosService {
    private final Map<String, RegistroEsclavos> registrosPorDuena = new HashMap<>();

    public RegistroEsclavos getOrCreate(Duena duena, int nivelCifrado) {
        return registrosPorDuena.computeIfAbsent(duena.getId(), k -> new RegistroEsclavos(duena, nivelCifrado));
    }
}
