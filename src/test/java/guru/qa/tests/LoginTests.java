package guru.qa.tests;

import models.login.*;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.Test;
import testData.TestData;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.*;
import static specs.registration.RegistrationSpec.registrationRequestSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;
import static testData.TestData.*;

public class LoginTests extends TestBase {

    TestData td = new TestData();

    @Test
    public void successfulLoginTest() {

        RegistrationBodyModel registrationData =
                new RegistrationBodyModel(td.username, td.password);

        given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel data = new LoginBodyModel(td.username, td.password);
        SuccessfulLoginResponseModel loginResponse = given(loginRequestSpec)
                .body(data)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class);

        String expectedTokenPart = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String actualAccess = loginResponse.access();
        String actualRefresh = loginResponse.refresh();

        assertThat(actualAccess).startsWith(expectedTokenPart);
        assertThat(actualRefresh).startsWith(expectedTokenPart);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    public void invalidCredentialsLoginTest() {
        LoginBodyModel data = new LoginBodyModel(td.username, td.wrongPassword);

        InvalidCredentialsLoginResponseModel loginResponse = given(loginRequestSpec)
                .body(data)
                .when()
                .post("/auth/token/")
                .then()
                .spec(invalidCredentialsLoginResponseSpec)
                .extract().as(InvalidCredentialsLoginResponseModel.class);

        String actualErrorInvalidUsernameOrPassword = loginResponse.detail();
        assertThat(actualErrorInvalidUsernameOrPassword).isEqualTo(EXPECTED_ERROR_INVALID_USERNAME_OR_PASSWORD);

    }

    @Test
    public void emptyRefreshTokenLoginNegativeTest() {
        WithoutRefreshTokenLoginBodyModel emptyRefreshToken = new WithoutRefreshTokenLoginBodyModel();
        WithoutRefreshTokenLoginResponseModel emptyRefreshResponseModel = given(loginRequestSpec)
                .body(emptyRefreshToken)
                .when()
                .post("/auth/token/refresh/")
                .then()
                .spec(withoutRefreshTokenResponseSpec)
                .extract().as(WithoutRefreshTokenLoginResponseModel.class);

        String actualRefresh = emptyRefreshResponseModel.refresh().get(0);
        assertThat(actualRefresh).isEqualTo(EXPECTED_REQUIRED_FIELD);
    }

    @Test
    public void invalidRefreshTokenLoginNegativeTest() {
        InvalidRefreshTokenBodyModel invalidTokenBodyModel = new InvalidRefreshTokenBodyModel(EXPECTED_ERROR_INVALID_REFRESH_TOKEN);
        InvalidRefreshTokenResponseModel loginResponse = given(loginRequestSpec)
                .body(invalidTokenBodyModel)
                .when()
                .post("/auth/token/refresh/")
                .then()
                .spec(invalidRefreshTokenResponseSpec)
                .extract().as(InvalidRefreshTokenResponseModel.class);

        String actualDetailInvalidRefreshToken = loginResponse.detail();
        String actualCodeInvalidRefreshToken = loginResponse.code();

        assertThat(actualDetailInvalidRefreshToken).isEqualTo(EXPECTED_ERROR_VALID_TOKEN);
        assertThat(actualCodeInvalidRefreshToken).isEqualTo(EXPECTED_TOKEN_NOT_VALID_CODE);
    }

    @Test
    public void accessTokenInsteadRefreshTokenLoginNegativeTest() {
        RegistrationBodyModel registrationData =
                new RegistrationBodyModel(td.username, td.password);

        given(registrationRequestSpec)

                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec);

        LoginBodyModel data = new LoginBodyModel(td.username, td.password);

        SuccessfulLoginResponseModel refreshResponse = given(loginRequestSpec)
                .body(data)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract().as(SuccessfulLoginResponseModel.class);
        String accessToken = refreshResponse.access();

        InvalidRefreshTokenBodyModel invalidTokenBodyModel = new InvalidRefreshTokenBodyModel(accessToken);
        InvalidRefreshTokenResponseModel loginResponse = given(loginRequestSpec)
                .body(invalidTokenBodyModel)
                .when()
                .post("/auth/token/refresh/")
                .then()
                .spec(invalidRefreshTokenResponseSpec)
                .extract().as(InvalidRefreshTokenResponseModel.class);

        String actualDetailInvalidRefreshToken = loginResponse.detail();
        String actualCodeInvalidRefreshToken = loginResponse.code();

        assertThat(actualDetailInvalidRefreshToken).isEqualTo(EXPECTED_ERROR_WRONG_TOKEN_TYPE);
        assertThat(actualCodeInvalidRefreshToken).isEqualTo(EXPECTED_TOKEN_NOT_VALID_CODE);

    }

}
