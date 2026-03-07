package com.jtarcio.closetrental.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configura serving de arquivos estáticos locais (imagens em dev).
 *
 * Em produção (STORAGE_PROVIDER=s3) as imagens ficam no CloudFront — este handler
 * ainda estará registrado mas nunca será chamado para imagens reais.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.storage.local.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath);
    }
}
