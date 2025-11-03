package dominio;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Usuario {
    private final String id;
    private String nombre;
    private String email;
    private String passwordHash;
    private String password;
    private final LocalDate fechaRegistro;
    private Rol rol;
    private boolean activo = true;

    protected Usuario(String id, String nombre, String email, String password, Rol rol) {
        this.id = Objects.requireNonNull(id);
        this.nombre = Objects.requireNonNull(nombre);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(password);
        this.fechaRegistro = LocalDate.now();
        this.rol = rol;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPassword() { return password; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public Rol getRol() { return rol; }
    protected void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void suspender() { this.activo = false; }
    public void reactivar() { this.activo = true; }
}
