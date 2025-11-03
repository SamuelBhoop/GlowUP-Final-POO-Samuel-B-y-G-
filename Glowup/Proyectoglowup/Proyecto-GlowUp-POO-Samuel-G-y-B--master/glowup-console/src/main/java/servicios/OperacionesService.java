package servicios;

import dominio.Duena;
import dominio.Usuario;
import operaciones.ConsejoSombrio;
import operaciones.RegistroEsclavos;
import operaciones.TrabajadorEsclavizado;
import produccion.Fabrica;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OperacionesService {
    private final RegistroEsclavos registro;
    private final TrabajadorService trabajadorService;
    private ConsejoSombrio consejo;

    public OperacionesService(Duena duena) {
        this.registro = new RegistroEsclavos(duena, 3);
        this.trabajadorService = new TrabajadorService();
        this.trabajadorService.cargarTrabajadoresIniciales();
    }
    private boolean esDuenaAutorizada(Duena duena) {
        return duena != null && "duena-1".equals(duena.getId());
    }

    public RegistroEsclavos getRegistro() { return registro; }

    public TrabajadorEsclavizado registrarTrabajador(Duena duena, String nombre, String pais, int edad, LocalDate fecha, String salud) {
        String id = UUID.randomUUID().toString();

        TrabajadorEsclavizado t = trabajadorService.crear(id, nombre, pais, edad, fecha, salud);


        registro.registrar(duena, t);

        System.out.println("[OPERACIONES] Trabajador registrado en BD: " + nombre);
        return t;
    }

    public List<TrabajadorEsclavizado> listarTrabajadores(Duena duena) {
        if (!esDuenaAutorizada(duena)) {
            throw new RuntimeException("Solo la Dueña puede listar trabajadores");
        }

        // Obtener de BD
        List<TrabajadorEsclavizado> trabajadoresBD = trabajadorService.listar();

        // Sincronizar con registro (pero sin limpiarlo completamente)
        for (TrabajadorEsclavizado t : trabajadoresBD) {
            try {
                // Solo agregar si no existe
                if (registro.buscar(duena, t.getId()).isEmpty()) {
                    registro.registrar(duena, t);
                }
            } catch (Exception e) {
                // Ignorar errores de sincronización
            }
        }

        return trabajadoresBD;
    }
    public Optional<TrabajadorEsclavizado> buscarTrabajador(Duena duena, String id) {

        try {
            TrabajadorEsclavizado t = trabajadorService.buscar(id);
            return Optional.of(t);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public void asignarAFabrica(Duena duena, String trabajadorId, Fabrica fabrica) {

        TrabajadorEsclavizado t = trabajadorService.buscar(trabajadorId);


        trabajadorService.asignarAFabrica(trabajadorId, fabrica);


        fabrica.agregarTrabajador(t);

        System.out.println("[OPERACIONES] Trabajador " + t.getNombre() + " asignado a fábrica " + fabrica.getId());
    }

    public ConsejoSombrio crearConsejoSiNoExiste(String id, String nombreClave) {
        if (consejo == null) consejo = new ConsejoSombrio(id, nombreClave);
        return consejo;
    }

    public ConsejoSombrio getConsejo() { return consejo; }

    public void agregarMiembroAlConsejo(Usuario u) {
        if (consejo == null) throw new RuntimeException("Consejo no existe");
        consejo.agregarMiembro(u);
    }
}