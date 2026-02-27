package com.jtarcio.closetrental;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de smoke test de integracao.
 * Requer PostgreSQL rodando. Execute com: mvn test -Pintegration
 */
@SpringBootTest
@ActiveProfiles("test")
class ClosetRentalApplicationTests {

    @Test
    void contextLoads() {
    }

}
