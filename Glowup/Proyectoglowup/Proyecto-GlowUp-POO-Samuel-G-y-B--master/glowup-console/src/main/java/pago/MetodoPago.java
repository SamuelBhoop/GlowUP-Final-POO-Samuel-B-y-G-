package pago;

public class MetodoPago {
    private final String id;
    private final TipoMetodoPago tipo;
    private final String titular;
    private final String numeroEnmascarado;

    public MetodoPago(String id, TipoMetodoPago tipo, String titular, String numeroEnmascarado) {
        this.id = id; this.tipo = tipo; this.titular = titular; this.numeroEnmascarado = numeroEnmascarado;
    }

    public String getId() { return id; }
    public TipoMetodoPago getTipo() { return tipo; }
    public String getTitular() { return titular; }
    public String getNumeroEnmascarado() { return numeroEnmascarado; }
}
