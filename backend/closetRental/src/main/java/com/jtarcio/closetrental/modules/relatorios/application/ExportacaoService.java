package com.jtarcio.closetrental.modules.relatorios.application;

import com.jtarcio.closetrental.modules.relatorios.api.dto.FaturamentoResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TopClienteResponse;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta dados de relatórios em CSV ou PDF.
 *
 * Formatos suportados: CSV (nativo Java) e PDF (OpenPDF).
 * Tipos suportados: faturamento, pecas-mais-alugadas, top-clientes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportacaoService {

    private final RelatorioService relatorioService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportarCsv(String tipo, LocalDate de, LocalDate ate) {
        return switch (tipo) {
            case "faturamento"        -> faturamentoCsv(de, ate);
            case "pecas-mais-alugadas" -> pecasCsv();
            case "top-clientes"       -> topClientesCsv();
            default -> throw new BusinessException("Tipo de exportação inválido: " + tipo);
        };
    }

    public byte[] exportarPdf(String tipo, LocalDate de, LocalDate ate) {
        return switch (tipo) {
            case "faturamento"        -> faturamentoPdf(de, ate);
            case "pecas-mais-alugadas" -> pecasPdf();
            case "top-clientes"       -> topClientesPdf();
            default -> throw new BusinessException("Tipo de exportação inválido: " + tipo);
        };
    }

    // ── CSV ───────────────────────────────────────────────────────────────────

    private byte[] faturamentoCsv(LocalDate de, LocalDate ate) {
        List<FaturamentoResponse> meses = relatorioService
                .faturamentoPorPeriodo(de, ate)
                .meses();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            w.println("Mes;Ano;Receita Total;Total Pagamentos");
            for (FaturamentoResponse m : meses) {
                w.printf("%d;%d;%s;%d%n", m.mes(), m.ano(), m.totalReceita().toPlainString(), m.totalPagamentos());
            }
        }
        return out.toByteArray();
    }

    private byte[] pecasCsv() {
        List<PecaMaisAlugadaResponse> lista = relatorioService.pecasMaisAlugadas(100);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            w.println("ID;Nome;Categoria;Total Locações");
            for (PecaMaisAlugadaResponse p : lista) {
                w.printf("%d;%s;%s;%d%n", p.produtoId(), p.nome(), p.categoria(), p.totalLocacoes());
            }
        }
        return out.toByteArray();
    }

    private byte[] topClientesCsv() {
        List<TopClienteResponse> lista = relatorioService.topClientes(100);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            w.println("ID;Nome;Total Locações;Receita Total");
            for (TopClienteResponse c : lista) {
                w.printf("%d;%s;%d;%s%n", c.clienteId(), c.nomeCliente(), c.totalLocacoes(), c.receitaTotal().toPlainString());
            }
        }
        return out.toByteArray();
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    private byte[] faturamentoPdf(LocalDate de, LocalDate ate) {
        List<FaturamentoResponse> meses = relatorioService
                .faturamentoPorPeriodo(de, ate)
                .meses();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitulo(doc, "Relatório de Faturamento");
        addSubtitulo(doc, "Período: " + de.format(FMT) + " a " + ate.format(FMT));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(12);
        addCabecalho(tabela, "Mês", "Ano", "Receita Total", "Pagamentos");
        for (FaturamentoResponse m : meses) {
            addLinha(tabela, String.valueOf(m.mes()), String.valueOf(m.ano()),
                    "R$ " + m.totalReceita().toPlainString(), String.valueOf(m.totalPagamentos()));
        }
        doc.add(tabela);
        doc.close();
        return out.toByteArray();
    }

    private byte[] pecasPdf() {
        List<PecaMaisAlugadaResponse> lista = relatorioService.pecasMaisAlugadas(100);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitulo(doc, "Peças Mais Alugadas");

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(12);
        addCabecalho(tabela, "#", "Nome", "Categoria", "Total Locações");
        int pos = 1;
        for (PecaMaisAlugadaResponse p : lista) {
            addLinha(tabela, String.valueOf(pos++), p.nome(), p.categoria(), String.valueOf(p.totalLocacoes()));
        }
        doc.add(tabela);
        doc.close();
        return out.toByteArray();
    }

    private byte[] topClientesPdf() {
        List<TopClienteResponse> lista = relatorioService.topClientes(100);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addTitulo(doc, "Top Clientes por Receita");

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(12);
        addCabecalho(tabela, "#", "Nome", "Locações", "Receita Total");
        int pos = 1;
        for (TopClienteResponse c : lista) {
            addLinha(tabela, String.valueOf(pos++), c.nomeCliente(),
                    String.valueOf(c.totalLocacoes()), "R$ " + c.receitaTotal().toPlainString());
        }
        doc.add(tabela);
        doc.close();
        return out.toByteArray();
    }

    // ── helpers PDF ───────────────────────────────────────────────────────────

    private void addTitulo(Document doc, String texto) {
        try {
            Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph p = new Paragraph(texto, f);
            p.setSpacingAfter(4);
            doc.add(p);
        } catch (Exception e) { log.warn("PDF titulo: {}", e.getMessage()); }
    }

    private void addSubtitulo(Document doc, String texto) {
        try {
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph p = new Paragraph(texto, f);
            p.setSpacingAfter(8);
            doc.add(p);
        } catch (Exception e) { log.warn("PDF subtitulo: {}", e.getMessage()); }
    }

    private void addCabecalho(PdfPTable tabela, String... cols) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String col : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(col, f));
            cell.setBackgroundColor(new Color(30, 30, 46));
            cell.setPadding(6);
            tabela.addCell(cell);
        }
    }

    private void addLinha(PdfPTable tabela, String... valores) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        for (String v : valores) {
            PdfPCell cell = new PdfPCell(new Phrase(v, f));
            cell.setPadding(5);
            tabela.addCell(cell);
        }
    }
}
