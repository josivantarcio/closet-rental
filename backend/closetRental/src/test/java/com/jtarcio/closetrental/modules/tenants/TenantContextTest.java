package com.jtarcio.closetrental.modules.tenants;

import com.jtarcio.closetrental.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TenantContext")
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("deve retornar null quando nenhum tenant estiver definido")
    void deveRetornarNullQuandoVazio() {
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    @DisplayName("deve armazenar e recuperar o tenantId corretamente")
    void deveArmazenarTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        assertThat(TenantContext.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("deve limpar o tenantId após clear()")
    void deveLimparAposClear() {
        TenantContext.setTenantId(UUID.randomUUID());
        TenantContext.clear();
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    @DisplayName("deve isolar tenantId por thread")
    void deveIsolarpThread() throws InterruptedException {
        UUID tenantMain = UUID.randomUUID();
        UUID tenantThread = UUID.randomUUID();
        TenantContext.setTenantId(tenantMain);

        UUID[] threadResult = new UUID[1];
        Thread thread = new Thread(() -> {
            TenantContext.setTenantId(tenantThread);
            threadResult[0] = TenantContext.getTenantId();
            TenantContext.clear();
        });
        thread.start();
        thread.join();

        assertThat(TenantContext.getTenantId()).isEqualTo(tenantMain);
        assertThat(threadResult[0]).isEqualTo(tenantThread);
    }
}
