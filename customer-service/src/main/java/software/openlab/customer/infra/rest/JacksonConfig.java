package software.openlab.customer.infra.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

/**
 * All JSON payloads use snake_case field names (category_id, short_description,
 * page_size, ...) to stay wire-compatible with the other ERP services.
 */
@Singleton
public class JacksonConfig implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
