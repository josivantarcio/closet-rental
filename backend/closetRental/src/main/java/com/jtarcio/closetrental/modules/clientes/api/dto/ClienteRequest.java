package com.jtarcio.closetrental.modules.clientes.api.dto;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação e atualização de clientes.
 * Validações de formato garantem integridade antes de chegar ao service.
 */
public record ClienteRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 200, message = "Email deve ter no máximo 200 caracteres")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(
            regexp = "^\\(?[1-9]{2}\\)?[\\s-]?[2-9][0-9]{3,4}[\\s-]?[0-9]{4}$",
            message = "Telefone inválido. Use o formato (DDD) 9XXXX-XXXX ou similar"
        )
        String telefone,

        @NotBlank(message = "Documento é obrigatório")
        @Pattern(
            regexp = "^(\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}|\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2})$",
            message = "Documento inválido. Informe um CPF (11 dígitos) ou CNPJ (14 dígitos)"
        )
        String documento,

        @Size(max = 300, message = "Endereço deve ter no máximo 300 caracteres")
        String endereco

) {
    /** Converte o DTO para o domain model. */
    public Cliente toDomain() {
        Cliente c = new Cliente();
        c.setNome(this.nome.trim());
        c.setEmail(this.email.trim().toLowerCase());
        c.setTelefone(this.telefone.trim());
        c.setDocumento(this.documento.replaceAll("[^0-9]", "")); // normaliza: só dígitos
        c.setEndereco(this.endereco != null ? this.endereco.trim() : null);
        return c;
    }
}
