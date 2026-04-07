package me.dohyeon.deepdive.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeepDive API")
                        .description("AI 기반 모의 면접 서비스 API 명세서")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("accessToken"))
                .components(new Components()
                        .addSecuritySchemes("accessToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("accessToken")
                                .description("로그인 후 발급되는 JWT accessToken 쿠키")));
    }
}
