package com.jtarcio.closetrental.modules.clientes.api.dto;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;

import java.time.LocalDateTime;

/**
 * DTO de saída para respostas da API de clientes.
 * Documento mascarado para conformidade com LGPD em listagens.
 */
public record ClienteResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String documentoMascarado,
        String endereco,
        boolean ativo,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Cria o response a partir do domain model, mascarando o documento. */
    public static ClienteResponse fromDomain(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                mascararDocumento(cliente.getDocumento()),
                cliente.getEndereco(),
                cliente.isAtivo(),
                cliente.getCreatedBy(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }

    // Mascara CPF (ex: 123.xxx.xxx-45) e CNPJ (ex: 12.xxx.xxx/xx01-89). Complexidade O(1).
    private static String mascararDocumento(String doc) {
        if (doc == null) return null;
        String d = doc.replaceAll("[^0-9]", "");
        if (d.length() == 11) {
            return d.substring(0, 3) + ".xxx.xxx-" + d.substring(9);
        } else if (d.length() == 14) {
            return d.substring(0, 2) + ".xxx.xxx/" + d.substring(10, 12) + "xx-" + d.substring(12);
        }
        return "xxx";
    }
}
