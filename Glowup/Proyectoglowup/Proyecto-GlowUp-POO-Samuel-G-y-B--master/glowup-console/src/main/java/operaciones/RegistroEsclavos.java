package operaciones;

import dominio.Duena;

import java.time.LocalDateTime;
import java.util.*;

public class RegistroEsclavos {
    private final Duena duena; // control exclusivo
    private LocalDateTime ultimoAcceso;
    private int nivelCifrado; // 1..5
    private final Map<String, TrabajadorEsclavizado> registros = new HashMap<>(); // id -> trabajador

    public RegistroEsclavos(Duena duena, int nivelCifrado) {
        this.duena = Objects.requireNonNull(duena);
        this.nivelCifrado = nivelCifrado;
        this.ultimoAcceso = LocalDateTime.now();
    }

    private void check(Duena operador) {
        if (operador == null || operador != duena) {
            throw new SecurityException("Acceso denegado: control exclusivo de la Dueña");
        }
        ultimoAcceso = LocalDateTime.now();
    }

    public void registrar(Duena operador, TrabajadorEsclavizado t) {
        check(operador);
        registros.put(t.getId(), t);
    }

    public Optional<TrabajadorEsclavizado> buscar(Duena operador, String id) {
        check(operador);
        return Optional.ofNullable(registros.get(id));
    }

    public List<TrabajadorEsclavizado> listar(Duena operador) {
        check(operador);
        return List.copyOf(registros.values());
    }

    public void eliminar(Duena operador, String id) {
        check(operador);
        registros.remove(id);
    }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public int getNivelCifrado() { return nivelCifrado; }
    public void setNivelCifrado(Duena operador, int nivelCifrado) {
        check(operador);
        this.nivelCifrado = nivelCifrado;
    }
}
