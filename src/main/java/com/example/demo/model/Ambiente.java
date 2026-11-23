package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ambiente (aula, taller, baño, laboratorio, etc.) dentro de un piso.
 */
@Entity
@Table(name = "ambientes", indexes = {
        @Index(name = "idx_ambientes_piso_tipo", columnList = "piso_id, tipo"),
        @Index(name = "idx_ambientes_codigo", columnList = "codigo")
})
public class Ambiente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piso_id", nullable = false)
    private Piso piso;

    @Column(name = "tipo", length = 40, nullable = false)
    private String tipo; // AULA, TALLER, INVESTIGACION, LABORATORIO, AIP, BANO, SALA, ADMIN, DEPOSITO, BIBLIOTECA, TOPICO, MAESTRANZA, CONECTIVIDAD

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Column(name = "codigo", length = 20)
    private String codigo; // Número real de aula, sufijo de taller (T1, T2) o NULL

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre; // Etiqueta visible en la app

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Ambiente() {
    }

    public Ambiente(Piso piso, String tipo, String especialidad, String codigo, String nombre) {
        this.piso = piso;
        this.tipo = tipo;
        this.especialidad = especialidad;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Piso getPiso() {
        return piso;
    }

    public void setPiso(Piso piso) {
        this.piso = piso;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        if (this.creadoEn == null) {
            this.creadoEn = ahora;
        }
        this.actualizadoEn = ahora;
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
