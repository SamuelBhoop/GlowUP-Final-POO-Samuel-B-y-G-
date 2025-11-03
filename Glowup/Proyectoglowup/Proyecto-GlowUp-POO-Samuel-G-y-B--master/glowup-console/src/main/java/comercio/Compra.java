package comercio;

import dominio.Cliente;
import pago.MetodoPago;

import java.time.LocalDateTime;
import java.util.*;

public class Compra {
    private final String id;
    private final Cliente cliente;
    private final LocalDateTime fecha;
    private EstadoCompra estado;
    private final List<LineaCompra> lineas;
    private final MetodoPago metodoPago;

    public Compra(String id, Cliente cliente, List<LineaCompra> lineas, MetodoPago metodoPago) {
        this.id = id;
        this.cliente = cliente;
        this.lineas = new ArrayList<>(lineas);
        this.metodoPago = metodoPago;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoCompra.PAGADA;
    }

    public String getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }
    public List<LineaCompra> getLineas() { return Collections.unmodifiableList(lineas); }
    public MetodoPago getMetodoPago() { return metodoPago; }

    public double getTotal() { return lineas.stream().mapToDouble(LineaCompra::getSubtotal).sum(); }
}
