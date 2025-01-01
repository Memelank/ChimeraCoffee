package com.chimera.weapp.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.media.StringSchema;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public OpenAPI customOpenAPI() {
        // 定义服务器信息
        Server server = new Server();
        server.setUrl("https://www.chimeracoffee.top");
        server.setDescription("Production server");

        return new OpenAPI()
                .info(new Info()
                        .title("SpringShop API")
                        .description("Spring shop sample application")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .servers(Collections.singletonList(server))
                .externalDocs(new ExternalDocumentation()
                        .description("SpringShop Wiki Documentation")
                        .url("https://springshop.wiki.github.org/docs"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSchemas(ObjectId.class.getSimpleName(), new StringSchema()));
    }
}
