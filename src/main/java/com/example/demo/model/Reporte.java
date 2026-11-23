package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Reporte de daño registrado por un docente en un ambiente.
 */
@Entity
@Table(name = "reportes", indexes = {
        @Index(name = "idx_reportes_estado", columnList = "estado"),
        @Index(name = "idx_reportes_docente", columnList = "docente_id"),
        @Index(name = "idx_reportes_encargado", columnList = "encargado_id"),
        @Index(name = "idx_reportes_ambiente", columnList = "ambiente_id")
}, uniqueConstraints = @UniqueConstraint(columnNames = "codigo_ticket"))
public class Reporte implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo_ticket", length = 25, nullable = false, unique = true)
    private String codigoTicket; // Ej: REP-2026-0001

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ambiente_id", nullable = false)
    private Ambiente ambiente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Usuario docente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encargado_id")
    private Usuario encargado;

    @Column(name = "titulo", length = 150, nullable = false)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "prioridad", length = 10, nullable = false)
    private String prioridad = "MEDIA"; // BAJA, MEDIA, ALTA, URGENTE

    @Column(name = "estado", length = 15, nullable = false)
    private String estado = "POR_ATENDER"; // POR_ATENDER, EN_PROCESO, ATENDIDO, RECHAZADO

    @Column(name = "observacion_atencion", columnDefinition = "TEXT")
    private String observacionAtencion;

    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    public Reporte() {
    }

    public Reporte(String codigoTicket, Ambiente ambiente, Usuario docente, Usuario encargado,
                   String titulo, String descripcion, String fotoUrl, String prioridad, String estado) {
        this.codigoTicket = codigoTicket;
        this.ambiente = ambiente;
        this.docente = docente;
        this.encargado = encargado;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
        this.prioridad = prioridad;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoTicket() {
        return codigoTicket;
    }

    public void setCodigoTicket(String codigoTicket) {
        this.codigoTicket = codigoTicket;
    }

    public Ambiente getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(Ambiente ambiente) {
        this.ambiente = ambiente;
    }

    public Usuario getDocente() {
        return docente;
    }

    public void setDocente(Usuario docente) {
        this.docente = docente;
    }

    public Usuario getEncargado() {
        return encargado;
    }

    public void setEncargado(Usuario encargado) {
        this.encargado = encargado;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacionAtencion() {
        return observacionAtencion;
    }

    public void setObservacionAtencion(String observacionAtencion) {
        this.observacionAtencion = observacionAtencion;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }
}
