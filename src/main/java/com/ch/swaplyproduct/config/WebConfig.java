package com.ch.swaplyproduct.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // =====================================================
    // CORS 설정
    //   - 게이트웨이(8882)를 통해 오는 경우 origin은 5173/5174
    //   - 직접 호출(개발용) 포함 둘 다 허용
    // =====================================================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://34.64.87.67.nip.io:5173",  // ✅ 추가 (Vite 기본 포트)
                        "http://34.64.87.67.nip.io:5174"   // ✅ 기존 유지
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // =====================================================
    // 정적 리소스 서빙: /uploads/** → 실제 파일 경로
    //   - Spring Boot 기동 위치 기준 상대경로 "uploads/"
    //   - Windows: "file:///C:/path/to/uploads/"  로 변경 가능
    //   - 실제 서버 절대경로가 필요하면 System.getProperty("user.dir") 활용
    // =====================================================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
