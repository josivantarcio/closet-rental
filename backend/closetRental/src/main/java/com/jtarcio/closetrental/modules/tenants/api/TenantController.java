package com.jtarcio.closetrental.modules.tenants.api;

import com.jtarcio.closetrental.modules.tenants.api.dto.TenantRequest;
import com.jtarcio.closetrental.modules.tenants.api.dto.TenantResponse;
import com.jtarcio.closetrental.modules.tenants.application.TenantService;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Gerenciamento de tenants (SUPER_ADMIN only)")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar tenant")
    public TenantResponse criar(@Valid @RequestBody TenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setNome(request.nome());
        tenant.setSlug(request.slug());
        tenant.setPlano(request.plano());
        if (request.pctCancelamento3d() != null) tenant.setPctCancelamento3d(request.pctCancelamento3d());
        if (request.pctCancelamento1d() != null) tenant.setPctCancelamento1d(request.pctCancelamento1d());
        if (request.pctCancelamento0d() != null) tenant.setPctCancelamento0d(request.pctCancelamento0d());
        return toResponse(tenantService.criar(tenant));
    }

    @GetMapping
    @Operation(summary = "Listar tenants")
    public Page<TenantResponse> listar(Pageable pageable) {
        return tenantService.listar(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tenant por ID")
    public TenantResponse buscarPorId(@PathVariable UUID id) {
        return toResponse(tenantService.buscarPorId(id));
    }

    @PutMapping("/{id}/ativar")
    @Operation(summary = "Ativar tenant")
    public TenantResponse ativar(@PathVariable UUID id) {
        return toResponse(tenantService.ativar(id));
    }

    @PutMapping("/{id}/desativar")
    @Operation(summary = "Desativar tenant")
    public TenantResponse desativar(@PathVariable UUID id) {
        return toResponse(tenantService.desativar(id));
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(
                t.getId(), t.getNome(), t.getSlug(), t.getPlano(), t.isAtivo(),
                t.getPctCancelamento3d(), t.getPctCancelamento1d(), t.getPctCancelamento0d(),
                t.getCreatedAt());
    }
}
