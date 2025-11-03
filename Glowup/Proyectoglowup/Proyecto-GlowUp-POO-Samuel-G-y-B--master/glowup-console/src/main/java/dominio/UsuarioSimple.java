package dominio;

public class UsuarioSimple extends Usuario {
    public UsuarioSimple(String id, String nombre, String email, String passwordHash, Rol rol) {
        super(id, nombre, email, passwordHash, rol);
    }
}
