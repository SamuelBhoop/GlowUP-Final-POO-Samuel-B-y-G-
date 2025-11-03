package operaciones;

import dominio.AdministradorContenido;
import dominio.AdministradorUsuario;
import dominio.Usuario;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConsejoSombrio {
    private final String id;
    private String nombreClave;
    private final Set<Usuario> miembros = new HashSet<>();

    public ConsejoSombrio(String id, String nombreClave) {
        this.id = Objects.requireNonNull(id);
        this.nombreClave = Objects.requireNonNull(nombreClave);
    }

    public String getId() { return id; }
    public String getNombreClave() { return nombreClave; }
    public void setNombreClave(String nombreClave) { this.nombreClave = nombreClave; }

    public Set<Usuario> getMiembros() { return Set.copyOf(miembros); }

    public void agregarMiembro(Usuario u) {
        if (!(u instanceof AdministradorContenido || u instanceof AdministradorUsuario)) {
            throw new IllegalArgumentException("Solo AdminContenido o AdminUsuario pueden ser miembros");
        }
        miembros.add(u);
    }

    public void retirarMiembro(Usuario u) { miembros.remove(u); }
}
