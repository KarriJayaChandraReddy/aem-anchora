/*
 *  Copyright 2026 Anchora
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anchora.core.testcontext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * Shared helpers for Sling model unit tests backed by JSON fixtures.
 */
public final class ModelTestJsonSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ModelTestJsonSupport() {
    }

    /**
     * Loads {@code TestClassName/fileName} from the test class package.
     *
     * @param testClass test class used to resolve the classpath resource
     * @param fileName fixture file name
     * @return parsed JSON root
     * @throws IOException if the fixture cannot be read
     */
    public static JsonNode loadJson(Class<?> testClass, String fileName) throws IOException {
        String path = testClass.getSimpleName() + "/" + fileName;
        InputStream stream = Objects.requireNonNull(testClass.getResourceAsStream(path), "Missing JSON fixture " + path);
        return MAPPER.readTree(stream);
    }

    /**
     * @param node JSON object
     * @param fieldName field name
     * @return text value, or {@code null} when the field is missing or null
     */
    public static String textOrNull(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return null;
        }
        return node.get(fieldName).asText();
    }

    /**
     * @param resource resource to adapt
     * @param modelClass model type
     * @param <T> model type
     * @return adapted model instance
     */
    public static <T> T adaptResource(Resource resource, Class<T> modelClass) {
        return resource.adaptTo(modelClass);
    }

    /**
     * Copies JSON properties onto a resource value map. Nested objects and arrays are skipped.
     *
     * @param resource target resource
     * @param properties JSON properties to apply
     */
    public static void applyProperties(Resource resource, JsonNode properties) {
        if (resource == null || properties == null || properties.isNull()) {
            return;
        }
        ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        Iterator<String> fieldNames = properties.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode value = properties.get(fieldName);
            if (value.isBoolean()) {
                valueMap.put(fieldName, value.asBoolean());
            } else if (value.isNumber()) {
                valueMap.put(fieldName, value.numberValue());
            } else if (value.isObject() || value.isArray()) {
                continue;
            } else if (!value.isNull()) {
                valueMap.put(fieldName, value.asText());
            }
        }
    }

    /**
     * Creates a resource tree from a JSON object. Nested objects become child resources.
     *
     * @param context AEM mock context
     * @param path resource path to create
     * @param node JSON object for that resource
     */
    public static void createTree(AemContext context, String path, JsonNode node) {
        context.create().resource(path);
        if (node == null || !node.isObject()) {
            return;
        }
        Resource resource = context.resourceResolver().getResource(path);
        applyProperties(resource, node);
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode value = node.get(fieldName);
            if (value.isObject()) {
                createTree(context, path + "/" + fieldName, value);
            }
        }
    }
}
