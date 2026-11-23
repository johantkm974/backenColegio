package com.example.demo.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Foto adjunta de un reporte (hasta {@code ReporteService.MAX_FOTOS} por reporte).
 * Se guarda en la nube (Cloudinary); aquí queda la URL pública y el public_id
 * para poder borrarla si se elimina el reporte.
 */
@Entity
@Table(name = "reporte_fotos", indexes = {
        @Index(name = "idx_reporte_fotos_reporte", columnList = "reporte_id")
})
public class ReporteFoto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "public_id", length = 255, nullable = false)
    private String publicId;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public ReporteFoto() {
    }

    public ReporteFoto(Reporte reporte, String url, String publicId) {
        this.reporte = reporte;
        this.url = url;
        this.publicId = publicId;
        this.creadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public void setReporte(Reporte reporte) {
        this.reporte = reporte;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
