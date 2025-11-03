package dominio;

import java.time.LocalDate;

public class Duena extends Usuario {
    private String claveMaestra;
    private LocalDate fechaCoronacion;

    public Duena(String id, String nombre, String email, String password,
                 String claveMaestra, LocalDate fechaCoronacion) {
        super(id, nombre, email, password, Rol.DUENA);
        this.claveMaestra = claveMaestra;
        this.fechaCoronacion = fechaCoronacion;
    }

    public String getClaveMaestra() { return claveMaestra; }
    public LocalDate getFechaCoronacion() { return fechaCoronacion; }
}
