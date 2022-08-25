package com.ionos.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ionos.project.dto.ServerDto;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@QuarkusTest
@TestHTTPEndpoint(ServerController.class)
@TestSecurity(authorizationEnabled = false)
class ServerControllerTest {

    @Test
    void createServer_RamIsNotMultipleOf1024_Returns422() throws JsonProcessingException {
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 2, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    void createServer_CoreIsZero_Returns422() throws JsonProcessingException {
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 0, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    void createServer_NameIsBlank_Returns422() throws JsonProcessingException {
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, " ", 0, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    void createServer_StorageIsZero_Returns422() throws JsonProcessingException {
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 3, 200, 0);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    void createServer_RamIsNull_Returns422() throws JsonProcessingException {
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 0, null, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .when()
                .post()
                .then()
                .statusCode(422);
    }

    @Test
    void updateServer_RamIsNotMultipleOf1024_Returns422() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 2, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .pathParam("serverId", uuid)
                .when()
                .put("/{serverId}")
                .then()
                .statusCode(422);
    }

    @Test
    void updateServer_CoreIsZero_Returns422() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 0, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .pathParam("serverId", uuid)
                .when()
                .put("/{serverId}")
                .then()
                .statusCode(422);
    }

    @Test
    void updateServer_NameIsBlank_Returns422() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, " ", 0, 200, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .pathParam("serverId", uuid)
                .when()
                .put("/{serverId}")
                .then()
                .statusCode(422);
    }

    @Test
    void updateServer_StorageIsZero_Returns422() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 3, 200, 0);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .pathParam("serverId", uuid)
                .when()
                .put("/{serverId}")
                .then()
                .statusCode(422);
    }

    @Test
    void updateServer_RamIsNull_Returns422() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        ServerDto toBeSavedServer = new ServerDto(null, null, null, null, "server1", 0, null, 30);

        given()
                .contentType(APPLICATION_JSON)
                .body(new ObjectMapper().writeValueAsString(toBeSavedServer))
                .pathParam("serverId", uuid)
                .when()
                .put("/{serverId}")
                .then()
                .statusCode(422);
    }

}
