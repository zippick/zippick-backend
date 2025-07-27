package zippick.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    // 서버 설정
    Server server = new Server();
    server.setUrl("https://zippick.n-e.kr");
//    server.setUrl("http://localhost:8080");
    server.setDescription("Zippick Server");

    // Bearer JWT
    SecurityScheme bearerAuthScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

    // 전체 API에 Auth 추가
    SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList("bearerAuth");

    return new OpenAPI()
            .servers(List.of(server))
            .components(new Components().addSecuritySchemes("bearerAuth", bearerAuthScheme))
            .addSecurityItem(securityRequirement);
  }
}
