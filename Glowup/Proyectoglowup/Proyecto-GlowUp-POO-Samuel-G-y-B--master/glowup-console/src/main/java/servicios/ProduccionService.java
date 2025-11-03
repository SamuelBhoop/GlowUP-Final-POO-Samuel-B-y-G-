package servicios;

import produccion.Fabrica;
import java.util.*;

public class ProduccionService {
    private final FabricaDAO fabricaDAO = new FabricaDAO();
    private final Map<String, Fabrica> cacheFabricas = new HashMap<>();

    public Fabrica crearFabrica(String id, String pais, String ciudad, int capacidad, String nivelAutomatizacion) {

        Optional<Fabrica> existente = fabricaDAO.buscarPorId(id);
        if (existente.isPresent()) {
            throw new RuntimeException("ID de fábrica en uso");
        }

        Fabrica f = new Fabrica(id, pais, ciudad, capacidad, nivelAutomatizacion);
        fabricaDAO.insertar(f);

        cacheFabricas.put(id, f);

        System.out.println("[PRODUCCION] Fábrica creada en BD: " + id);
        return f;
    }

    public Fabrica get(String id) {

        Fabrica f = cacheFabricas.get(id);

        if (f == null) {
            Optional<Fabrica> resultado = fabricaDAO.buscarPorId(id);
            if (resultado.isPresent()) {
                f = resultado.get();
                cacheFabricas.put(id, f);
            }
        }

        if (f == null) {
            throw new RuntimeException("Fábrica no encontrada: " + id);
        }

        return f;
    }

    public List<Fabrica> listar() {
        List<Fabrica> fabricasBD = fabricaDAO.listarTodos();

        cacheFabricas.clear();
        fabricasBD.forEach(f -> cacheFabricas.put(f.getId(), f));

        return fabricasBD;
    }

    public void cargarFabricasIniciales() {
        List<Fabrica> fabricasBD = fabricaDAO.listarTodos();
        cacheFabricas.clear();
        fabricasBD.forEach(f -> cacheFabricas.put(f.getId(), f));
        System.out.println("[PRODUCCION] " + fabricasBD.size() + " fábricas cargadas desde BD");
    }
}