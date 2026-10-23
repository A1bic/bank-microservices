package ru.kovalenko.bank.presentation.openapi;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

@Configuration
@SecurityScheme(name = OpenApiConfig.BASIC_AUTH, type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
    static final String BASIC_AUTH = "basicAuth";
    private static final String PROBLEM_DETAIL_SCHEMA = "ProblemDetail";

    @Bean
    public OpenAPI bankServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Service API")
                        .version("1.0.0")
                        .description("Users, friends, accounts, money transfers and multi-currency balances"))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }

    /**
     * Every endpoint requires authentication, so 401/403 are documented here once instead of on every method.
     * All 4xx responses are described with the problem detail schema.
     */
    @Bean
    public OpenApiCustomizer errorResponsesCustomizer() {
        return openApi -> {
            var problemSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ProblemDetail.class))
                    .schema;
            openApi.getComponents().addSchemas(PROBLEM_DETAIL_SCHEMA, problemSchema);

            var problemContent = new Content().addMediaType(
                    MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    new io.swagger.v3.oas.models.media.MediaType().schema(new Schema<>().$ref(PROBLEM_DETAIL_SCHEMA)));

            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
                var responses = operation.getResponses();
                responses.putIfAbsent("401", new ApiResponse().description("Authentication required"));
                responses.putIfAbsent("403", new ApiResponse().description("Not enough permissions"));
                responses.forEach((code, response) -> {
                    if (code.startsWith("4")) {
                        response.setContent(problemContent);
                    }
                });
            }));
        };
    }
}
