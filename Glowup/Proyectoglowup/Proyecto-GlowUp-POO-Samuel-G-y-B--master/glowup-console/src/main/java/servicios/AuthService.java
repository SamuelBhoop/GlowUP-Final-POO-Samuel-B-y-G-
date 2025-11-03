package servicios;

import dominio.Usuario;
import dominio.Cliente;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void registrar(Usuario usuario) {
        if (usuariosPorEmail.containsKey(usuario.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }

        // SOLO guardar Clientes en BD
        if (usuario instanceof Cliente cliente) {
            try {
                usuarioDAO.insertarCliente(cliente);
                System.out.println("[AUTH] Cliente guardado en BD: " + cliente.getEmail());
            } catch (Exception e) {
                throw new RuntimeException("Error guardando en base de datos: " + e.getMessage());
            }
        }

        // Todos los usuarios van a memoria
        usuariosPorEmail.put(usuario.getEmail(), usuario);
    }

    public Usuario login(String email, String password) {
        Usuario usuario = usuariosPorEmail.get(email);

        // Si no está en memoria, intentar cargar Cliente desde BD
        if (usuario == null) {
            usuario = cargarClienteDesdeBD(email, password);
        }

        if (usuario == null || !usuario.getPasswordHash().equals(password)) {
            throw new RuntimeException("Credenciales inválidas");
        }
        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario suspendido");
        }
        return usuario;
    }

    private Usuario cargarClienteDesdeBD(String email, String password) {
        try {
            Usuario cliente = usuarioDAO.buscarPorEmail(email);

            if (cliente != null && cliente.getPasswordHash().equals(password)) {
                usuariosPorEmail.put(email, cliente);
                System.out.println("Cliente cargado desde BD: " + email);
                return cliente;
            }
        } catch (Exception e) {
            System.out.println("Error cargando cliente desde BD: " + e.getMessage());
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) {
        return usuariosPorEmail.get(email);
    }
}