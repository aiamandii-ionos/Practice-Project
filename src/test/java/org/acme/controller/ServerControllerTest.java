package org.acme.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.acme.model.Server;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class ServerControllerTest {

    @Test
    public void getAll(){
        given().when().get("/api/servers")
                .then()
                .statusCode(200);
    }

    @Test
    public void getById(){
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
                .when()
                .get("/api/servers/{serverId}", saved.getId())
                .then()
                .statusCode(200)
                .extract().as(Server.class);

        assertThat(saved).isEqualTo(got);
    }

}
