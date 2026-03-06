package com.jtarcio.closetrental.shared.tenant;

import java.util.UUID;

/**
 * Holder ThreadLocal do tenantId corrente.
 * Populado pelo TenantFilter a cada requisição e limpo no finally.
 * SUPER_ADMIN sem X-Tenant-Id terá tenantId = null (sem filtro).
 */
public class TenantContext {

    private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(UUID id) {
        TENANT.set(id);
    }

    public static UUID getTenantId() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}
