package specs;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.JSON;
import io.qameta.allure.restassured.AllureRestAssured;

public class BaseSpec {

    public static RequestSpecification baseRequestSpec = with()
            .filter(new AllureRestAssured())
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1");
}
