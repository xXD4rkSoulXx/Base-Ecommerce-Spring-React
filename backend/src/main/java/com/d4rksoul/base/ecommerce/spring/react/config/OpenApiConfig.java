package com.d4rksoul.base.ecommerce.spring.react.config;

import jakarta.annotation.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    private static final String RESPONSE_PACKAGE =
        "com.d4rksoul.base.ecommerce.spring.react.dto.response";

    @Bean
    public OpenApiCustomizer requiredPropertiesCustomizer() {
        Map<String, Class<?>> records = findResponseRecords();

        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }

            openApi.getComponents().getSchemas().forEach((schemaName, schema) ->
                records.entrySet().stream()
                    .filter(entry -> matches(schemaName, entry.getKey(), entry.getValue()))
                    .findFirst()
                    .ifPresent(entry -> markRequired(schema, entry.getValue())));
        };
    }

    private static boolean matches(String schemaName, String recordName, Class<?> type) {
        boolean generic = type.getTypeParameters().length > 0;
        return schemaName.equals(recordName) || (generic && schemaName.startsWith(recordName));
    }

    private static void markRequired(Schema<?> schema, Class<?> type) {
        if (schema.getProperties() == null) {
            return;
        }

        List<String> required = Arrays.stream(type.getRecordComponents())
            .filter(component -> schema.getProperties().containsKey(component.getName()))
            .filter(component -> !component.getAccessor().isAnnotationPresent(Nullable.class))
            .map(RecordComponent::getName)
            .toList();

        schema.setRequired(required.isEmpty() ? null : required);
    }

    private static Map<String, Class<?>> findResponseRecords() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Record.class));

        Map<String, Class<?>> records = new HashMap<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(RESPONSE_PACKAGE)) {
            String className = Objects.requireNonNull(definition.getBeanClassName());

            try {
                Class<?> type = ClassUtils.forName(className, OpenApiConfig.class.getClassLoader());
                records.put(type.getSimpleName(), type);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load response record " + className, e);
            }
        }

        return records;
    }
}
