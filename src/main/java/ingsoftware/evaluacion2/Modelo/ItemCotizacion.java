package ingsoftware.evaluacion2.Modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "items_cotizacion")
public class ItemCotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int cantidad;

    private double precioUnitarioFinal;

    @ManyToOne
    @JoinColumn(name = "mueble_id", nullable = false)
    private Mueble mueble;

    @ManyToOne
    @JoinColumn(name = "variante_id", nullable = true)
    private Variante variante;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private Cotizacion cotizacion;

    public ItemCotizacion(Long id, int cantidad, Mueble mueble, Variante variante, Cotizacion cotizacion) {
        this.id = id;
        this.cantidad = cantidad;
        this.mueble = mueble;
        this.variante = variante;
        this.cotizacion = cotizacion;
    }
    public double getPrecioUnitarioFinal(){
        return precioUnitarioFinal;
    }
    
    public void setPrecioUnitarioFinal(double precioUnitarioFinal){
        this.precioUnitarioFinal=precioUnitarioFinal;
    }

    public ItemCotizacion(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Mueble getMueble() {
        return mueble;
    }

    public void setMueble(Mueble mueble) {
        this.mueble = mueble;
    }

    public Variante getVariante() {
        return variante;
    }

    public void setVariante(Variante variante) {
        this.variante = variante;
    }

    public Cotizacion getCotizacion() {
        return cotizacion;
    }

    public void setCotizacion(Cotizacion cotizacion) {
        this.cotizacion = cotizacion;
    }
}
