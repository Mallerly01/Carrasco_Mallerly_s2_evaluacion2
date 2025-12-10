package ingsoftware.evaluacion2.Modelo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cotizaciones")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado;
    @JsonManagedReference
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemCotizacion> items;

    public Cotizacion(Long id, LocalDate fecha, EstadoCotizacion estado, List<ItemCotizacion> items) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.items = items;
    }
    public Cotizacion(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoCotizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoCotizacion estado) {
        this.estado = estado;
    }

    public List<ItemCotizacion> getItems() {
        return items;
    }

    public void setItems(List<ItemCotizacion> items) {
        this.items = items;
    }
}