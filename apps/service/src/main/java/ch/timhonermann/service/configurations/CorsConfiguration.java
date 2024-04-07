package ch.timhonermann.service.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins("http://localhost:4200", "https://dev-smf.eu.auth0.com")
      .allowedMethods("OPTIONS", "POST", "GET", "DELETE", "PUT", "PATCH")
      .allowCredentials(true);
  }
}
