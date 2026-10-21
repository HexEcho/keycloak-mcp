package io.github.sakc.keycloak.mcp.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sakc.keycloak.mcp.domain.service.ResponseFieldCatalog;
import org.junit.jupiter.api.Test;

class ResponseProjectorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ResponseProjector projector = new ResponseProjector(mapper, new ResponseFieldCatalog());

    @Test
    void defaultsToMinimalClientFields() throws Exception {
        JsonNode data = mapper.readTree("[{\"id\":\"1\",\"clientId\":\"a\",\"name\":\"A\",\"description\":\"d\",\"publicClient\":true,\"secret\":\"x\"}]");
        JsonNode result = projector.project(data, projector.resolveFields("list_clients", null));
        assertThat(result.get(0).fieldNames()).toIterable().containsExactly("id", "clientId", "name", "description");
    }

    @Test
    void honoursRequestedFieldsAndWildcard() throws Exception {
        JsonNode data = mapper.readTree("{\"id\":\"1\",\"clientId\":\"a\",\"publicClient\":true}");
        var fields = projector.resolveFields("get_client", mapper.readTree("{\"fields\":[\"clientId\",\"publicClient\"]}"));
        assertThat(projector.project(data, fields).fieldNames()).toIterable().containsExactly("clientId", "publicClient");
        assertThat(projector.resolveFields("get_client", mapper.readTree("{\"fields\":\"*\"}"))).isNull();
    }
}
