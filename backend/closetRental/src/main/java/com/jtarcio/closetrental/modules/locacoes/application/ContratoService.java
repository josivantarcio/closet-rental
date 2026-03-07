package com.jtarcio.closetrental.modules.locacoes.application;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Use Case: Gerar contrato de locação em PDF.
 *
 * O documento inclui:
 * - Cabeçalho com nome do atelier/tenant
 * - Dados do cliente (nome, documento, telefone, endereço)
 * - Dados da locação (período, itens, valores)
 * - Condições de cancelamento conforme política do tenant
 * - Campos de assinatura (cliente e responsável)
 *
 * Complexidade: O(n) onde n = número de itens (tipicamente ≤ 10).
 */
@Service
@RequiredArgsConstructor
public class ContratoService {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DT   = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private static final Font FONT_TITULO   = new Font(Font.HELVETICA, 18, Font.BOLD,   new Color(30, 30, 30));
    private static final Font FONT_SECAO    = new Font(Font.HELVETICA, 11, Font.BOLD,   new Color(60, 60, 60));
    private static final Font FONT_LABEL    = new Font(Font.HELVETICA,  9, Font.BOLD,   new Color(80, 80, 80));
    private static final Font FONT_VALOR    = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(40, 40, 40));
    private static final Font FONT_RODAPE   = new Font(Font.HELVETICA,  7, Font.ITALIC, new Color(120, 120, 120));

    private final LocacaoRepositoryPort locacaoPort;
    private final ClienteRepositoryPort clientePort;
    private final TenantRepositoryPort  tenantPort;

    @Transactional(readOnly = true)
    public byte[] gerarContrato(Long locacaoId) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        Cliente cliente = clientePort.buscarPorId(locacao.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado com id: " + locacao.getClienteId()));

        Tenant tenant = locacao.getTenantId() != null
                ? tenantPort.buscarPorId(locacao.getTenantId()).orElse(tenantPadrao())
                : tenantPadrao();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            adicionarCabecalho(doc, tenant, locacaoId);
            adicionarDadosCliente(doc, cliente);
            adicionarDadosLocacao(doc, locacao);
            adicionarItens(doc, locacao);
            adicionarResumoFinanceiro(doc, locacao);
            adicionarPoliticaCancelamento(doc, tenant);
            adicionarClausulas(doc);
            adicionarAssinaturas(doc, cliente, tenant);
            adicionarRodape(doc, locacao);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar contrato PDF para locacao " + locacaoId, e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void adicionarCabecalho(Document doc, Tenant tenant, Long locacaoId) throws DocumentException {
        Paragraph titulo = new Paragraph(tenant.getNome() != null ? tenant.getNome().toUpperCase() : "ATELIER", FONT_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Paragraph subtitulo = new Paragraph("CONTRATO DE LOCAÇÃO DE TRAJE", FONT_SECAO);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingBefore(4);
        doc.add(subtitulo);

        Paragraph numContrato = new Paragraph("Nº do Contrato: " + String.format("%06d", locacaoId), FONT_VALOR);
        numContrato.setAlignment(Element.ALIGN_CENTER);
        numContrato.setSpacingBefore(2);
        doc.add(numContrato);

        adicionarLinha(doc);
    }

    private void adicionarDadosCliente(Document doc, Cliente cliente) throws DocumentException {
        doc.add(secao("DADOS DO CONTRATANTE"));

        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{1, 2});

        adicionarCelula(tabela, "Nome:", FONT_LABEL);
        adicionarCelula(tabela, nvl(cliente.getNome()), FONT_VALOR);

        adicionarCelula(tabela, "CPF/CNPJ:", FONT_LABEL);
        adicionarCelula(tabela, nvl(cliente.getDocumento()), FONT_VALOR);

        adicionarCelula(tabela, "Telefone:", FONT_LABEL);
        adicionarCelula(tabela, nvl(cliente.getTelefone()), FONT_VALOR);

        adicionarCelula(tabela, "E-mail:", FONT_LABEL);
        adicionarCelula(tabela, nvl(cliente.getEmail()), FONT_VALOR);

        adicionarCelula(tabela, "Endereço:", FONT_LABEL);
        adicionarCelula(tabela, nvl(cliente.getEndereco()), FONT_VALOR);

        doc.add(tabela);
    }

    private void adicionarDadosLocacao(Document doc, Locacao locacao) throws DocumentException {
        doc.add(secao("DADOS DA LOCAÇÃO"));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);

        adicionarCelula(tabela, "Data de Retirada:", FONT_LABEL);
        adicionarCelula(tabela, locacao.getDataInicio().format(FMT_DATA), FONT_VALOR);
        adicionarCelula(tabela, "Data de Devolução:", FONT_LABEL);
        adicionarCelula(tabela, locacao.getDataFim().format(FMT_DATA), FONT_VALOR);

        adicionarCelula(tabela, "Prazo (dias):", FONT_LABEL);
        adicionarCelula(tabela, String.valueOf(locacao.calcularDias()), FONT_VALOR);
        adicionarCelula(tabela, "Status:", FONT_LABEL);
        adicionarCelula(tabela, locacao.getStatus().name(), FONT_VALOR);

        doc.add(tabela);
    }

    private void adicionarItens(Document doc, Locacao locacao) throws DocumentException {
        doc.add(secao("PEÇAS LOCADAS"));

        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setWidths(new float[]{3, 1, 1, 1});

        // Cabeçalho da tabela
        adicionarCelulaHeader(tabela, "Peça / Produto");
        adicionarCelulaHeader(tabela, "Diária (R$)");
        adicionarCelulaHeader(tabela, "Dias");
        adicionarCelulaHeader(tabela, "Subtotal (R$)");

        long dias = locacao.calcularDias();
        for (ItemLocacao item : locacao.getItens()) {
            adicionarCelula(tabela, nvl(item.getNomeProduto()), FONT_VALOR);
            adicionarCelulaDireita(tabela, formatarMoeda(item.getValorDiaria()), FONT_VALOR);
            adicionarCelulaDireita(tabela, String.valueOf(dias), FONT_VALOR);
            adicionarCelulaDireita(tabela, formatarMoeda(item.calcularSubtotal(dias)), FONT_VALOR);
        }

        doc.add(tabela);
    }

    private void adicionarResumoFinanceiro(Document doc, Locacao locacao) throws DocumentException {
        doc.add(secao("RESUMO FINANCEIRO"));

        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(60);
        tabela.setHorizontalAlignment(Element.ALIGN_RIGHT);

        if (locacao.getCaucao() != null) {
            adicionarCelula(tabela, "Caução:", FONT_LABEL);
            adicionarCelulaDireita(tabela, "R$ " + formatarMoeda(locacao.getCaucao()), FONT_VALOR);
        }

        if (locacao.getMulta() != null && locacao.getMulta().signum() > 0) {
            adicionarCelula(tabela, "Multa por atraso:", FONT_LABEL);
            adicionarCelulaDireita(tabela, "R$ " + formatarMoeda(locacao.getMulta()), FONT_VALOR);
        }

        Font fontTotal = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(30, 30, 30));
        adicionarCelula(tabela, "VALOR TOTAL:", fontTotal);
        adicionarCelulaDireita(tabela, "R$ " + formatarMoeda(locacao.getValorTotal()), fontTotal);

        doc.add(tabela);
    }

    private void adicionarPoliticaCancelamento(Document doc, Tenant tenant) throws DocumentException {
        doc.add(secao("POLÍTICA DE CANCELAMENTO"));

        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(80);

        adicionarCelula(tabela, "7 ou mais dias de antecedência:", FONT_LABEL);
        adicionarCelula(tabela, "Isento de multa", FONT_VALOR);

        adicionarCelula(tabela, "3 a 6 dias de antecedência:", FONT_LABEL);
        adicionarCelula(tabela, tenant.getPctCancelamento3d() + "% sobre o valor locado", FONT_VALOR);

        adicionarCelula(tabela, "1 a 2 dias de antecedência:", FONT_LABEL);
        adicionarCelula(tabela, tenant.getPctCancelamento1d() + "% sobre o valor locado", FONT_VALOR);

        adicionarCelula(tabela, "No dia da retirada:", FONT_LABEL);
        adicionarCelula(tabela, tenant.getPctCancelamento0d() + "% sobre o valor locado", FONT_VALOR);

        doc.add(tabela);
    }

    private void adicionarClausulas(Document doc) throws DocumentException {
        doc.add(secao("CONDIÇÕES GERAIS"));

        String[] clausulas = {
            "1. O(a) CONTRATANTE responsabiliza-se pela conservação das peças durante o período de locação.",
            "2. Danos, manchas ou avarias serão cobrados à parte, podendo resultar em retenção total ou parcial da caução.",
            "3. A devolução deve ocorrer até a data prevista. Atrasos acarretam multa diária conforme tabela do atelier.",
            "4. As peças são higienizadas pelo atelier antes e após cada locação.",
            "5. Em caso de extravio, o(a) CONTRATANTE arcará com o valor de reposição da peça.",
            "6. O presente contrato é regido pelas disposições do Código Civil Brasileiro."
        };

        for (String clausula : clausulas) {
            Paragraph p = new Paragraph(clausula, FONT_VALOR);
            p.setSpacingBefore(3);
            doc.add(p);
        }
    }

    private void adicionarAssinaturas(Document doc, Cliente cliente, Tenant tenant) throws DocumentException {
        adicionarLinha(doc);

        Paragraph espaco = new Paragraph(" ");
        espaco.setSpacingBefore(30);
        doc.add(espaco);

        PdfPTable tabela = new PdfPTable(2);
        tabela.setWidthPercentage(100);

        PdfPCell celulaCliente = new PdfPCell();
        celulaCliente.setBorder(Rectangle.NO_BORDER);
        Paragraph linhaCliente = new Paragraph("_______________________________", FONT_VALOR);
        linhaCliente.setAlignment(Element.ALIGN_CENTER);
        celulaCliente.addElement(linhaCliente);
        Paragraph nomeCliente = new Paragraph(nvl(cliente.getNome()), FONT_LABEL);
        nomeCliente.setAlignment(Element.ALIGN_CENTER);
        celulaCliente.addElement(nomeCliente);
        Paragraph docCliente = new Paragraph("CPF/CNPJ: " + nvl(cliente.getDocumento()), FONT_RODAPE);
        docCliente.setAlignment(Element.ALIGN_CENTER);
        celulaCliente.addElement(docCliente);
        Paragraph labelCliente = new Paragraph("CONTRATANTE", FONT_LABEL);
        labelCliente.setAlignment(Element.ALIGN_CENTER);
        celulaCliente.addElement(labelCliente);
        tabela.addCell(celulaCliente);

        PdfPCell celulaTenant = new PdfPCell();
        celulaTenant.setBorder(Rectangle.NO_BORDER);
        Paragraph linhaTenant = new Paragraph("_______________________________", FONT_VALOR);
        linhaTenant.setAlignment(Element.ALIGN_CENTER);
        celulaTenant.addElement(linhaTenant);
        Paragraph nomeTenant = new Paragraph(nvl(tenant.getNome()), FONT_LABEL);
        nomeTenant.setAlignment(Element.ALIGN_CENTER);
        celulaTenant.addElement(nomeTenant);
        Paragraph labelTenant = new Paragraph("CONTRATADA", FONT_LABEL);
        labelTenant.setAlignment(Element.ALIGN_CENTER);
        celulaTenant.addElement(labelTenant);
        tabela.addCell(celulaTenant);

        doc.add(tabela);
    }

    private void adicionarRodape(Document doc, Locacao locacao) throws DocumentException {
        adicionarLinha(doc);
        String geradoEm = locacao.getCreatedAt() != null
                ? locacao.getCreatedAt().format(FMT_DT)
                : java.time.LocalDateTime.now().format(FMT_DT);
        Paragraph rodape = new Paragraph(
                "Contrato gerado em: " + geradoEm + "  |  Locação ID: " + locacao.getId(),
                FONT_RODAPE);
        rodape.setAlignment(Element.ALIGN_CENTER);
        doc.add(rodape);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Paragraph secao(String titulo) {
        Paragraph p = new Paragraph(titulo, FONT_SECAO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        return p;
    }

    private void adicionarLinha(Document doc) throws DocumentException {
        Paragraph linha = new Paragraph("────────────────────────────────────────────────────────────────────────────", FONT_RODAPE);
        linha.setSpacingBefore(6);
        linha.setSpacingAfter(6);
        doc.add(linha);
    }

    private void adicionarCelula(PdfPTable tabela, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        tabela.addCell(cell);
    }

    private void adicionarCelulaDireita(PdfPTable tabela, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabela.addCell(cell);
    }

    private void adicionarCelulaHeader(PdfPTable tabela, String texto) {
        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontHeader));
        cell.setBackgroundColor(new Color(80, 80, 80));
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabela.addCell(cell);
    }

    private String formatarMoeda(java.math.BigDecimal valor) {
        if (valor == null) return "0,00";
        return String.format("%,.2f", valor).replace('.', ',').replace(',', '.');
    }

    private String nvl(String valor) {
        return valor != null ? valor : "";
    }

    private Tenant tenantPadrao() {
        Tenant t = new Tenant();
        t.setNome("Atelier");
        return t;
    }
}
