package org.acme.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.acme.model.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ServerControllerTest {

    @Test
    void getAllServers_Success(){
        given().when().get("/api/servers")
                .then()
                .statusCode(200);
    }

    @Test
    void getServerById_Success(){
        Server server = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Server saved = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(server)
                .post("/api/servers")
                .then()
                .statusCode(201)
                .extract().as(Server.class);

        Server got = given()
                .pathParam("serverId", saved.getId())
                .when()
                .get("/api/servers/{serverId}")
                .then()
                .statusCode(200)
                .extract().as(Server.class);

        assertThat(saved).isEqualTo(got);
    }

    @Test
    void getServerById_Failure(){
        UUID uuid = UUID.randomUUID();
        given()
                .when()
                .pathParam("serverId", uuid)
                .get("/api/servers/{serverId}")
                .then()
                .statusCode(404);
    }

    @Test
    void createServer_Success(){
        Server server = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        given()
                .contentType(ContentType.JSON)
                .body(server)
                .when()
                .post("/api/servers")
                .then()
                .statusCode(201)
                .extract().as(Server.class);
    }

    @Test
    void updateServer_Success(){
        Server server = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();
        Server saved = given()
                .contentType(ContentType.JSON)
                .body(server)
                .when()
                .post("/api/servers")
                .then()
                .statusCode(201)
                .extract().as(Server.class);

        saved.setName("server2");

        Server updated = given()
                .contentType(ContentType.JSON)
                .body(saved)
                .pathParam("serverId", saved.getId())
                .when()
                .put("/api/servers/{serverId}")
                .then()
                .statusCode(200)
                .extract().as(Server.class);

        assertThat(updated.getName()).isEqualTo("server2");
    }

    @Test
    void updateServer_Failure(){
        UUID uuid = UUID.randomUUID();
        Server server = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();
        Server saved = given()
                .contentType(ContentType.JSON)
                .body(server)
                .when()
                .post("/api/servers")
                .then()
                .statusCode(201)
                .extract().as(Server.class);

        saved.setName("server2");

        given()
                .contentType(ContentType.JSON)
                .body(saved)
                .pathParam("serverId", uuid)
                .when()
                .put("/api/servers/{serverId}")
                .then()
                .statusCode(404);

    }

    @Test
    void deleteServer_Success(){
        Server server = Server.builder()
                .cores(2)
                .ram(200)
                .storage(30)
                .name("server1").build();

        Server saved = given()
                .contentType(ContentType.JSON)
                .body(server)
                .when()
                .post("/api/servers")
                .then()
                .statusCode(201)
                .extract().as(Server.class);

        given()
                .pathParam("serverId", saved.getId())
                .when()
                .delete("/api/servers/{serverId}")
                .then()
                .statusCode(204);
    }
    @Test
    void deleteServer_Failure(){
        UUID uuid = UUID.randomUUID();
        given()
                .pathParam("serverId", uuid)
                .when()
                .delete("/api/servers/{serverId}")
                .then()
                .statusCode(404);
    }
}
