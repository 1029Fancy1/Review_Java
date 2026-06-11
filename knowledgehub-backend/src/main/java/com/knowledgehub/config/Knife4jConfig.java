package com.knowledgehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口文档配置
 *
 * 访问地址：http://localhost:8080/doc.html
 *
 * 学习要点：
 * 1. Knife4j 基于 Swagger/OpenAPI 3.0 规范
 * 2. @Configuration 声明这是一个配置类
 * 3. @Bean 方法返回的对象由 Spring 容器管理
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KnowledgeHub AI 接口文档")
                        .version("1.0.0")
                        .description("个人知识库智能问答系统 - 后端 API")
                        .contact(new Contact()
                                .name("yourname")
                                .email("yourname@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
