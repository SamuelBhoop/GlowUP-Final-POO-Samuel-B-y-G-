package dominio;

public class Cliente extends Usuario {
    private String direccionEnvio;
    private String telefono;

    public Cliente(String id, String nombre, String email, String password,
                   String direccionEnvio, String telefono) {
        super(id, nombre, email, password, Rol.CLIENTE);
        this.direccionEnvio = direccionEnvio;
        this.telefono = telefono;
    }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
