package com.inventory.config;
import com.inventory.dto.ApiError;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.core.converter.ModelConverters;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
/** Documents the API and its shared error responses. */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI inventoryApi() {
        return new OpenAPI().info(new Info().title("Inventory Management System").version("1.0.0")
            .description("Local learning API. Money is a decimal string; timestamps use UTC. No authentication."));
    }
    /** Adds common errors to the generated Swagger operations. */
    @Bean
    public OpenApiCustomizer errorResponses() {
        return api -> {
            if (api.getComponents() == null) api.setComponents(new Components());
            ModelConverters.getInstance().read(ApiError.class).forEach(api.getComponents()::addSchemas);
            api.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
                for (String status : new String[]{"400", "404", "409", "500"}) {
                    operation.getResponses().addApiResponse(status, new ApiResponse().description(switch (status) {
                        case "400" -> "Invalid request or field validation error";
                        case "404" -> "Record not found";
                        case "409" -> "Duplicate value or category in use";
                        default -> "Unexpected server error";
                    }).content(new Content().addMediaType("application/json", new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiError")))));
                }
            }));
        };
    }
}
