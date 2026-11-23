package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Piso de un edificio.
 */
@Entity
@Table(name = "pisos", uniqueConstraints = @UniqueConstraint(columnNames = { "edificio_id", "numero_piso" }))
public class Piso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "edificio_id", nullable = false)
    private Edificio edificio;

    @Column(name = "numero_piso", nullable = false)
    private Integer numeroPiso;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Piso() {
    }

    public Piso(Edificio edificio, Integer numeroPiso, String nombre) {
        this.edificio = edificio;
        this.numeroPiso = numeroPiso;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Edificio getEdificio() {
        return edificio;
    }

    public void setEdificio(Edificio edificio) {
        this.edificio = edificio;
    }

    public Integer getNumeroPiso() {
        return numeroPiso;
    }

    public void setNumeroPiso(Integer numeroPiso) {
        this.numeroPiso = numeroPiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
