package dominio;

public class DesarrolladorProducto extends Usuario {
    private String especialidad;

    public DesarrolladorProducto(String id, String nombre, String email, String password, String especialidad) {
        super(id, nombre, email, password, Rol.DESARROLLADOR_PRODUCTO);
        this.especialidad = especialidad;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
