package servicios;

import operaciones.TrabajadorEsclavizado;
import produccion.Fabrica;

import java.time.LocalDate;
import java.util.*;

public class TrabajadorService {
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final Map<String, TrabajadorEsclavizado> cacheMemoria = new HashMap<>();

    public TrabajadorEsclavizado crear(String id, String nombre, String paisOrigen, int edad,
                                       LocalDate fechaCaptura, String salud) {

        TrabajadorEsclavizado t = new TrabajadorEsclavizado(id, nombre, paisOrigen, edad, fechaCaptura, salud);


        trabajadorDAO.insertar(t);


        cacheMemoria.put(t.getId(), t);

        System.out.println("[SERVICE] Trabajador creado en BD: " + nombre);
        return t;
    }

    public TrabajadorEsclavizado buscar(String id) {

        TrabajadorEsclavizado t = cacheMemoria.get(id);

        if (t == null) {

            Optional<TrabajadorEsclavizado> resultado = trabajadorDAO.buscarPorId(id);
            if (resultado.isPresent()) {
                t = resultado.get();
                cacheMemoria.put(id, t);
            }
        }

        if (t == null) {
            throw new RuntimeException("Trabajador no encontrado: " + id);
        }

        return t;
    }

    public List<TrabajadorEsclavizado> listar() {

        List<TrabajadorEsclavizado> trabajadores = trabajadorDAO.listarTodos();


        cacheMemoria.clear();
        trabajadores.forEach(t -> cacheMemoria.put(t.getId(), t));

        return trabajadores;
    }


    public void asignarAFabrica(String trabajadorId, Fabrica fabrica) {
        TrabajadorEsclavizado t = buscar(trabajadorId);
        t.asignarAFabrica(fabrica);

        trabajadorDAO.actualizarAsignacion(trabajadorId, fabrica.getId());

        System.out.println("[SERVICE] Trabajador " + t.getNombre() + " asignado a fábrica " + fabrica.getId());
    }


    public void cargarTrabajadoresIniciales() {
        List<TrabajadorEsclavizado> trabajadoresBD = trabajadorDAO.listarTodos();
        cacheMemoria.clear();
        trabajadoresBD.forEach(t -> cacheMemoria.put(t.getId(), t));
        System.out.println("[SERVICE] " + trabajadoresBD.size() + " trabajadores cargados desde BD");
    }
}