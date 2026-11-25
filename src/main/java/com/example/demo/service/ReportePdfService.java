package com.example.demo.service;

import com.example.demo.model.Ambiente;
import com.example.demo.model.Edificio;
import com.example.demo.model.HistorialReporte;
import com.example.demo.model.Piso;
import com.example.demo.model.Reporte;
import com.example.demo.model.ReporteFoto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.HistorialReporteRepository;
import com.example.demo.repository.ReporteFotoRepository;
import com.example.demo.repository.ReporteRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportePdfService {

    public record PdfGenerado(String filename, byte[] bytes) {}

    private final ReporteRepository reporteRepository;
    private final ReporteFotoRepository reporteFotoRepository;
    private final HistorialReporteRepository historialReporteRepository;

    private static final String RUTA_ENCABEZADO = "img/encabezado_mgp.png";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_CORTO = DateTimeFormatter.ofPattern("dd/MM/yy");

    private static final Color AZUL_OSCURO = new Color(30, 58, 138);
    private static final Color AZUL_ACENTO = new Color(29, 78, 216);
    private static final Color GRIS_FONDO = new Color(248, 250, 252);
    private static final Color GRIS_BORDE = new Color(203, 213, 225);
    private static final Color TEXTO_OSCURO = new Color(15, 23, 42);
    private static final Color TEXTO_GRIS = new Color(71, 85, 105);

    public ReportePdfService(ReporteRepository reporteRepository,
                             ReporteFotoRepository reporteFotoRepository,
                             HistorialReporteRepository historialReporteRepository) {
        this.reporteRepository = reporteRepository;
        this.reporteFotoRepository = reporteFotoRepository;
        this.historialReporteRepository = historialReporteRepository;
    }

    // =========================================================================
    // 1. FICHA INDIVIDUAL DE INCIDENCIA (A4 VERTICAL)
    // =========================================================================
    @Transactional(readOnly = true)
    public PdfGenerado generarFichaIndividual(Long reporteId, Usuario solicitante) {
        Reporte r = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reporte no encontrado con id: " + reporteId));

        String rol = solicitante.getRol() != null ? solicitante.getRol().getNombre() : "";
        if ("DOCENTE".equalsIgnoreCase(rol) && !r.getDocente().getId().equals(solicitante.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para descargar el PDF de este reporte");
        }

        List<ReporteFoto> fotos = reporteFotoRepository.findByReporteIdOrderByCreadoEnAsc(reporteId);
        List<HistorialReporte> historial = historialReporteRepository.findByReporte_IdOrderByCreadoEnAsc(reporteId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 34, 34, 24, 34);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Banner oficial MINEDU / DRELM / UGEL 06 / I.E. Manuel González Prada
            agregarBannerInstitucional(doc, 527f);

            Font fTitulo = new Font(Font.HELVETICA, 13, Font.BOLD, TEXTO_OSCURO);
            Font fSubtitulo = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO_GRIS);
            Font fTicket = new Font(Font.COURIER, 12, Font.BOLD, AZUL_OSCURO);
            Font fSeccion = new Font(Font.HELVETICA, 9, Font.BOLD, AZUL_OSCURO);
            Font fLabel = new Font(Font.HELVETICA, 8.5f, Font.BOLD, TEXTO_OSCURO);
            Font fValor = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO_OSCURO);
            Font fTh = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
            Font fTd = new Font(Font.HELVETICA, 8, Font.NORMAL, TEXTO_OSCURO);

            // Cabecera del documento
            PdfPTable header = new PdfPTable(new float[]{68f, 32f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(12f);

            PdfPCell hLeft = new PdfPCell();
            hLeft.setBorder(Rectangle.BOTTOM);
            hLeft.setBorderColorBottom(AZUL_ACENTO);
            hLeft.setBorderWidthBottom(2f);
            hLeft.setPaddingBottom(7f);
            hLeft.addElement(new Paragraph("FICHA DE INCIDENCIA DE INFRAESTRUCTURA", fTitulo));
            hLeft.addElement(new Paragraph("Sistema REPORTA — Gestión de Mantenimiento Escolar", fSubtitulo));

            PdfPCell hRight = new PdfPCell();
            hRight.setBorder(Rectangle.BOTTOM);
            hRight.setBorderColorBottom(AZUL_ACENTO);
            hRight.setBorderWidthBottom(2f);
            hRight.setPaddingBottom(7f);
            hRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pTicket = new Paragraph(nvl(r.getCodigoTicket()), fTicket);
            pTicket.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pBadges = new Paragraph(
                    nvl(r.getPrioridad()) + "  |  " + nvl(r.getEstado()).replace("_", " "), fLabel);
            pBadges.setAlignment(Element.ALIGN_RIGHT);
            hRight.addElement(pTicket);
            hRight.addElement(pBadges);

            header.addCell(hLeft);
            header.addCell(hRight);
            doc.add(header);

            // Ubicación y Responsables
            Ambiente amb = r.getAmbiente();
            Piso piso = amb != null ? amb.getPiso() : null;
            Edificio ed = piso != null ? piso.getEdificio() : null;

            PdfPTable grid = new PdfPTable(new float[]{49f, 2f, 49f});
            grid.setWidthPercentage(100);
            grid.setSpacingAfter(10f);

            PdfPCell boxUbic = crearCaja("UBICACIÓN DEL AMBIENTE", fSeccion);
            agregarCampo(boxUbic, "Edificio: ", ed != null ? ed.getCodigo() + " — " + ed.getNombre() : "—", fLabel, fValor);
            agregarCampo(boxUbic, "Piso: ", piso != null ? piso.getNombre() : "—", fLabel, fValor);
            agregarCampo(boxUbic, "Ambiente: ", amb != null ? amb.getNombre() : "—", fLabel, fValor);
            // Sin línea "Tipo": el tipo (AULA, SALA, BAÑO...) ya se infiere del
            // nombre del ambiente y la caja conserva su altura porque la fila
            // del PdfPTable la mantiene igual a la caja de RESPONSABLES.

            PdfPCell spacer = new PdfPCell();
            spacer.setBorder(Rectangle.NO_BORDER);

            PdfPCell boxResp = crearCaja("RESPONSABLES Y FECHAS", fSeccion);
            agregarCampo(boxResp, "Reportado por: ", r.getDocente() != null ? r.getDocente().getNombreCompleto() : "—", fLabel, fValor);
            agregarCampo(boxResp, "Atendido por: ", r.getEncargado() != null ? r.getEncargado().getNombreCompleto() : "Pendiente", fLabel, fValor);
            agregarCampo(boxResp, "Fecha reporte: ", fmtFecha(r.getFechaReporte()), fLabel, fValor);
            agregarCampo(boxResp, "Fecha atención: ", fmtFecha(r.getFechaAtencion()), fLabel, fValor);

            grid.addCell(boxUbic);
            grid.addCell(spacer);
            grid.addCell(boxResp);
            doc.add(grid);

            // Detalle
            doc.add(tituloSeccion("DETALLE DE LA INCIDENCIA", fSeccion));
            PdfPTable tblDetalle = new PdfPTable(1);
            tblDetalle.setWidthPercentage(100);
            tblDetalle.setSpacingAfter(9f);
            PdfPCell cDet = new PdfPCell();
            cDet.setBorderColor(GRIS_BORDE);
            cDet.setPadding(8f);
            agregarCampo(cDet, "Título: ", nvl(r.getTitulo()), fLabel, fValor);
            Paragraph pDesc = new Paragraph(nvl(r.getDescripcion()), fValor);
            pDesc.setSpacingBefore(4f);
            cDet.addElement(pDesc);
            tblDetalle.addCell(cDet);
            doc.add(tblDetalle);

            // Observación de atención
            doc.add(tituloSeccion("OBSERVACIÓN DE ATENCIÓN", fSeccion));
            PdfPTable tblObs = new PdfPTable(1);
            tblObs.setWidthPercentage(100);
            tblObs.setSpacingAfter(9f);
            PdfPCell cObs = new PdfPCell(new Phrase(
                    r.getObservacionAtencion() != null && !r.getObservacionAtencion().isBlank()
                            ? r.getObservacionAtencion()
                            : "Sin observación registrada aún.",
                    fValor));
            cObs.setBorderColor(GRIS_BORDE);
            cObs.setPadding(8f);
            tblObs.addCell(cObs);
            doc.add(tblObs);

            // Evidencias fotográficas (Cloudinary w_400, JPG)
            List<String> urls = new ArrayList<>();
            for (ReporteFoto f : fotos) {
                if (f.getUrl() != null && !f.getUrl().isBlank()) urls.add(f.getUrl());
            }
            if (urls.isEmpty() && r.getFotoUrl() != null && !r.getFotoUrl().isBlank()) {
                urls.add(r.getFotoUrl());
            }

            if (!urls.isEmpty()) {
                doc.add(tituloSeccion("EVIDENCIAS FOTOGRÁFICAS", fSeccion));
                PdfPTable gridFotos = new PdfPTable(2);
                gridFotos.setWidthPercentage(100);
                gridFotos.setSpacingAfter(9f);

                for (String rawUrl : urls) {
                    PdfPCell cFoto = new PdfPCell();
                    cFoto.setBorderColor(GRIS_BORDE);
                    cFoto.setPadding(6f);
                    cFoto.setHorizontalAlignment(Element.ALIGN_CENTER);
                    try {
                        Image img = Image.getInstance(URI.create(optimizarUrlCloudinary(rawUrl)).toURL());
                        img.scaleToFit(230f, 145f);
                        img.setAlignment(Element.ALIGN_CENTER);
                        cFoto.addElement(img);
                    } catch (Exception ex) {
                        cFoto.addElement(new Phrase("No se pudo cargar la imagen", fSubtitulo));
                    }
                    gridFotos.addCell(cFoto);
                }
                if (urls.size() % 2 != 0) {
                    PdfPCell vacia = new PdfPCell();
                    vacia.setBorder(Rectangle.NO_BORDER);
                    gridFotos.addCell(vacia);
                }
                doc.add(gridFotos);
            }

            // Historial
            doc.add(tituloSeccion("HISTORIAL DE ESTADOS", fSeccion));
            PdfPTable tblHist = new PdfPTable(new float[]{18f, 24f, 24f, 34f});
            tblHist.setWidthPercentage(100);
            tblHist.setSpacingAfter(24f);

            for (String col : new String[]{"Fecha / Hora", "Usuario", "Cambio de Estado", "Comentario"}) {
                PdfPCell th = new PdfPCell(new Phrase(col, fTh));
                th.setBackgroundColor(AZUL_OSCURO);
                th.setBorderColor(AZUL_OSCURO);
                th.setPadding(5f);
                tblHist.addCell(th);
            }

            for (HistorialReporte h : historial) {
                String cambio = (h.getEstadoAnterior() != null ? h.getEstadoAnterior() : "—")
                        + " -> " + nvl(h.getEstadoNuevo());
                agregarCelda(tblHist, fmtFecha(h.getCreadoEn()), fTd);
                agregarCelda(tblHist, h.getUsuario() != null ? h.getUsuario().getNombreCompleto() : "—", fTd);
                agregarCelda(tblHist, cambio, fTd);
                agregarCelda(tblHist, nvl(h.getComentario()), fTd);
            }
            doc.add(tblHist);

            // Firmas
            PdfPTable firmas = new PdfPTable(new float[]{45f, 10f, 45f});
            firmas.setWidthPercentage(100);
            firmas.setKeepTogether(true);

            String nombreEnc = r.getEncargado() != null
                    ? r.getEncargado().getNombreCompleto()
                    : "Responsable de Infraestructura";
            firmas.addCell(crearFirma("Firma del Encargado", nombreEnc, fLabel, fSubtitulo));
            PdfPCell fSp = new PdfPCell();
            fSp.setBorder(Rectangle.NO_BORDER);
            firmas.addCell(fSp);
            firmas.addCell(crearFirma("V°B° Dirección", "I.E. Manuel González Prada", fLabel, fSubtitulo));
            doc.add(firmas);

            doc.close();
            String filename = "ficha_" + r.getCodigoTicket() + ".pdf";
            return new PdfGenerado(filename, baos.toByteArray());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF del reporte " + reporteId, e);
        }
    }

    // =========================================================================
    // 2. REPORTE CONSOLIDADO DE INCIDENCIAS (A4 HORIZONTAL — ADMIN / ENCARGADO)
    // =========================================================================
    @Transactional(readOnly = true)
    public PdfGenerado generarConsolidado(String estado, Usuario solicitante) {
        List<Reporte> lista = (estado != null && !estado.isBlank() && !"TODOS".equalsIgnoreCase(estado))
                ? reporteRepository.findByEstado(estado.trim().toUpperCase())
                : reporteRepository.findAll();

        lista = lista.stream()
                .sorted(Comparator.comparing(Reporte::getId).reversed())
                .toList();

        long total = lista.size();
        long porAtender = lista.stream().filter(r -> "POR_ATENDER".equalsIgnoreCase(r.getEstado())).count();
        long enProceso = lista.stream().filter(r -> "EN_PROCESO".equalsIgnoreCase(r.getEstado())).count();
        long atendidos = lista.stream().filter(r -> "ATENDIDO".equalsIgnoreCase(r.getEstado())).count();
        long rechazados = lista.stream().filter(r -> "RECHAZADO".equalsIgnoreCase(r.getEstado())).count();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 20, 28);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Banner oficial centrado
            agregarBannerInstitucional(doc, 600f);

            Font fTitulo = new Font(Font.HELVETICA, 13, Font.BOLD, TEXTO_OSCURO);
            Font fSubtitulo = new Font(Font.HELVETICA, 8f, Font.NORMAL, TEXTO_GRIS);
            Font fLabel = new Font(Font.HELVETICA, 8f, Font.BOLD, TEXTO_OSCURO);
            Font fKpiNum = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL_OSCURO);
            Font fTh = new Font(Font.HELVETICA, 7.5f, Font.BOLD, Color.WHITE);
            Font fTd = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_OSCURO);
            Font fTicket = new Font(Font.COURIER, 7.5f, Font.BOLD, AZUL_OSCURO);

            // Cabecera
            PdfPTable header = new PdfPTable(new float[]{60f, 40f});
            header.setWidthPercentage(100);
            header.setSpacingAfter(10f);

            PdfPCell hLeft = new PdfPCell();
            hLeft.setBorder(Rectangle.BOTTOM);
            hLeft.setBorderColorBottom(AZUL_ACENTO);
            hLeft.setBorderWidthBottom(2f);
            hLeft.setPaddingBottom(6f);
            hLeft.addElement(new Paragraph("REPORTE CONSOLIDADO DE INCIDENCIAS", fTitulo));
            hLeft.addElement(new Paragraph("I.E. Manuel González Prada — Huaycán, Ate  |  Sistema REPORTA", fSubtitulo));

            String filtroTxt = (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado))
                    ? "Todos los estados"
                    : estado.toUpperCase().replace("_", " ");
            PdfPCell hRight = new PdfPCell();
            hRight.setBorder(Rectangle.BOTTOM);
            hRight.setBorderColorBottom(AZUL_ACENTO);
            hRight.setBorderWidthBottom(2f);
            hRight.setPaddingBottom(6f);
            Paragraph pF1 = new Paragraph("Filtro: " + filtroTxt + "  |  Emitido: " + fmtFecha(LocalDateTime.now()), fSubtitulo);
            pF1.setAlignment(Element.ALIGN_RIGHT);
            Paragraph pF2 = new Paragraph("Generado por: " + solicitante.getNombreCompleto(), fSubtitulo);
            pF2.setAlignment(Element.ALIGN_RIGHT);
            hRight.addElement(pF1);
            hRight.addElement(pF2);

            header.addCell(hLeft);
            header.addCell(hRight);
            doc.add(header);

            // KPIs
            PdfPTable kpis = new PdfPTable(5);
            kpis.setWidthPercentage(100);
            kpis.setSpacingAfter(10f);
            kpis.addCell(crearKpi("TOTAL", total, fSubtitulo, fKpiNum));
            kpis.addCell(crearKpi("POR ATENDER", porAtender, fSubtitulo, fKpiNum));
            kpis.addCell(crearKpi("EN PROCESO", enProceso, fSubtitulo, fKpiNum));
            kpis.addCell(crearKpi("ATENDIDOS", atendidos, fSubtitulo, fKpiNum));
            kpis.addCell(crearKpi("RECHAZADOS", rechazados, fSubtitulo, fKpiNum));
            doc.add(kpis);

            // Tabla principal
            PdfPTable tbl = new PdfPTable(new float[]{11f, 20f, 23f, 8f, 10f, 12f, 9f, 7f});
            tbl.setWidthPercentage(100);
            tbl.setHeaderRows(1);
            tbl.setSpacingAfter(20f);

            for (String col : new String[]{"Ticket", "Ubicación", "Incidencia", "Prioridad", "Estado", "Docente", "Encargado", "Fecha"}) {
                PdfPCell th = new PdfPCell(new Phrase(col, fTh));
                th.setBackgroundColor(AZUL_OSCURO);
                th.setBorderColor(AZUL_OSCURO);
                th.setPadding(5f);
                tbl.addCell(th);
            }

            if (lista.isEmpty()) {
                PdfPCell empty = new PdfPCell(new Phrase("No existen reportes registrados para el filtro seleccionado.", fSubtitulo));
                empty.setColspan(8);
                empty.setHorizontalAlignment(Element.ALIGN_CENTER);
                empty.setPadding(14f);
                empty.setBorderColor(GRIS_BORDE);
                tbl.addCell(empty);
            } else {
                for (Reporte r : lista) {
                    Ambiente amb = r.getAmbiente();
                    Piso piso = amb != null ? amb.getPiso() : null;
                    Edificio ed = piso != null ? piso.getEdificio() : null;
                    String ubic = (ed != null ? ed.getCodigo() : "")
                            + (piso != null ? " / " + piso.getNombre() : "")
                            + (amb != null ? "\n" + amb.getNombre() : "");

                    agregarCelda(tbl, nvl(r.getCodigoTicket()), fTicket);
                    agregarCelda(tbl, ubic.isBlank() ? "—" : ubic, fTd);
                    agregarCelda(tbl, nvl(r.getTitulo()), fTd);
                    agregarCelda(tbl, nvl(r.getPrioridad()), fTd);
                    agregarCelda(tbl, nvl(r.getEstado()).replace("_", " "), fTd);
                    agregarCelda(tbl, r.getDocente() != null ? r.getDocente().getNombreCompleto() : "—", fTd);
                    agregarCelda(tbl, r.getEncargado() != null ? r.getEncargado().getNombreCompleto() : "—", fTd);
                    agregarCelda(tbl, r.getFechaReporte() != null ? r.getFechaReporte().format(FMT_CORTO) : "—", fTd);
                }
            }
            doc.add(tbl);

            // Firmas
            PdfPTable firmas = new PdfPTable(new float[]{40f, 20f, 40f});
            firmas.setWidthPercentage(100);
            firmas.setKeepTogether(true);
            firmas.addCell(crearFirma("V°B° Dirección", "I.E. Manuel González Prada", fLabel, fSubtitulo));
            PdfPCell sp = new PdfPCell();
            sp.setBorder(Rectangle.NO_BORDER);
            firmas.addCell(sp);
            firmas.addCell(crearFirma("Responsable de Infraestructura", solicitante.getNombreCompleto(), fLabel, fSubtitulo));
            doc.add(firmas);

            doc.close();
            String sufijo = (estado == null || estado.isBlank()) ? "todos" : estado.toLowerCase();
            return new PdfGenerado("reportes_consolidado_" + sufijo + ".pdf", baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF consolidado", e);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private void agregarBannerInstitucional(Document doc, float maxWidth) {
        try {
            ClassPathResource res = new ClassPathResource(RUTA_ENCABEZADO);
            if (!res.exists()) return;
            Image banner = Image.getInstance(res.getContentAsByteArray());
            banner.scaleToFit(maxWidth, 80f);
            banner.setAlignment(Element.ALIGN_CENTER);
            banner.setSpacingAfter(8f);
            doc.add(banner);
        } catch (Exception ignored) {
            // Si el recurso falla, el PDF continúa sin interrumpirse
        }
    }

    private PdfPCell crearKpi(String label, long valor, Font fLabel, Font fNum) {
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(GRIS_FONDO);
        c.setBorderColor(GRIS_BORDE);
        c.setPadding(6f);
        Paragraph p1 = new Paragraph(label, fLabel);
        p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph(String.valueOf(valor), fNum);
        p2.setAlignment(Element.ALIGN_CENTER);
        c.addElement(p1);
        c.addElement(p2);
        return c;
    }

    private String optimizarUrlCloudinary(String url) {
        if (url != null && url.contains("/upload/")) {
            return url.replace("/upload/", "/upload/w_400,q_auto,f_jpg/");
        }
        return url;
    }

    private PdfPCell crearCaja(String titulo, Font fSeccion) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(GRIS_FONDO);
        cell.setBorderColor(GRIS_BORDE);
        cell.setPadding(8f);
        Paragraph p = new Paragraph(titulo, fSeccion);
        p.setSpacingAfter(4f);
        cell.addElement(p);
        return cell;
    }

    private Paragraph tituloSeccion(String texto, Font fSeccion) {
        Paragraph p = new Paragraph(texto, fSeccion);
        p.setSpacingBefore(4f);
        p.setSpacingAfter(4f);
        return p;
    }

    private void agregarCampo(PdfPCell cell, String label, String valor, Font fLabel, Font fValor) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, fLabel));
        p.add(new Chunk(valor, fValor));
        p.setSpacingAfter(2f);
        cell.addElement(p);
    }

    private void agregarCelda(PdfPTable table, String texto, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(texto, font));
        c.setBorderColor(GRIS_BORDE);
        c.setPadding(4.5f);
        table.addCell(c);
    }

    private PdfPCell crearFirma(String cargo, String nombre, Font fLabel, Font fSub) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.TOP);
        c.setBorderColorTop(TEXTO_GRIS);
        c.setPaddingTop(5f);
        Paragraph p1 = new Paragraph(cargo, fLabel);
        p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph(nombre, fSub);
        p2.setAlignment(Element.ALIGN_CENTER);
        c.addElement(p1);
        c.addElement(p2);
        return c;
    }

    private String fmtFecha(LocalDateTime dt) {
        return dt != null ? dt.format(FMT) : "—";
    }

    private String nvl(String s) {
        return s != null && !s.isBlank() ? s : "—";
    }
}
