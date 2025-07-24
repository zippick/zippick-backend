package zippick.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    Server server = new Server();
//    server.setUrl("https://zippick.n-e.kr"); // Swagger 내부 서버 주소 고정
    server.setDescription("Production");

    return new OpenAPI().servers(List.of(server));
  }
}
