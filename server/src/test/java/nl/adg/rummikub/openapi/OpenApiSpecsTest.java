package nl.adg.rummikub.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * The three specs in openapi/ must not overlap: each says "no path + method may appear in more than
 * one of them". Two generated controllers mapping the same route would make Spring pick one at
 * request time, so the overlap is caught here instead.
 */
class OpenApiSpecsTest {

    private static final Path SPEC_DIR = Path.of("..", "openapi");
    private static final List<String> SPECS =
            List.of("gameroom-api.yml", "game-session-api.yml", "rummikub-api.yml");
    private static final List<String> HTTP_METHODS =
            List.of("get", "put", "post", "delete", "patch", "head", "options");

    @Test
    void noPathAndMethodAppearsInMoreThanOneSpec() throws IOException {
        Map<String, List<String>> specsPerOperation = new HashMap<>();
        for (String spec : SPECS) {
            for (String operation : operationsIn(spec)) {
                specsPerOperation.computeIfAbsent(operation, k -> new ArrayList<>()).add(spec);
            }
        }

        assertThat(specsPerOperation).allSatisfy((operation, specs) -> assertThat(specs)
                .as("specs declaring %s", operation)
                .hasSize(1));
    }

    /**
     * The server generates one interface per tag (useTags), so a tag shared by two specs would give
     * two classes the same simple name, and an operation with two tags would be mapped twice.
     */
    @Test
    void everyOperationHasOneTagAndNoTagAppearsInMoreThanOneSpec() throws IOException {
        Map<String, List<String>> specsPerTag = new HashMap<>();
        for (String spec : SPECS) {
            for (Map<String, Object> operation : operationObjectsIn(spec)) {
                List<?> tags = (List<?>) operation.get("tags");
                assertThat(tags).as("tags of %s in %s", operation.get("operationId"), spec).hasSize(1);
                List<String> specs = specsPerTag.computeIfAbsent(String.valueOf(tags.getFirst()), k -> new ArrayList<>());
                if (!specs.contains(spec)) {
                    specs.add(spec);
                }
            }
        }

        assertThat(specsPerTag).allSatisfy((tag, specs) -> assertThat(specs)
                .as("specs using tag %s", tag)
                .hasSize(1));
    }

    @Test
    void routesDifferingOnlyInParameterNamesCountAsTheSame() {
        assertThat(normalise("/games/{sessionId}/players")).isEqualTo(normalise("/games/{id}/players"));
    }

    /** Every "METHOD /path" in the spec, with path parameter names erased. */
    private static List<String> operationsIn(String spec) throws IOException {
        Map<String, Map<String, Object>> paths = pathsIn(spec);
        List<String> operations = new ArrayList<>();
        paths.forEach((path, item) -> item.keySet().stream()
                .filter(HTTP_METHODS::contains)
                .forEach(method -> operations.add(method.toUpperCase(Locale.ROOT) + " " + normalise(path))));
        assertThat(operations).as("operations in %s", spec).isNotEmpty();
        return operations;
    }

    /** The operation objects (summary, tags, responses, ...) of every route in the spec. */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> operationObjectsIn(String spec) throws IOException {
        List<Map<String, Object>> operations = new ArrayList<>();
        pathsIn(spec).values().forEach(item -> item.forEach((method, operation) -> {
            if (HTTP_METHODS.contains(method)) {
                operations.add((Map<String, Object>) operation);
            }
        }));
        return operations;
    }

    private static Map<String, Map<String, Object>> pathsIn(String spec) throws IOException {
        try (Reader reader = Files.newBufferedReader(SPEC_DIR.resolve(spec))) {
            Map<String, Map<String, Map<String, Object>>> root = new Yaml().load(reader);
            return root.get("paths");
        }
    }

    /** /games/{sessionId} and /games/{id} are the same route to Spring. */
    private static String normalise(String path) {
        return path.replaceAll("\\{[^}]+}", "{}");
    }
}
