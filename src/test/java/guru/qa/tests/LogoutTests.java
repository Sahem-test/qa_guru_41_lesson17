package guru.qa.tests;

import models.login.LoginBodyModel;
import models.logout.LogoutBodyModel;
import models.logout.WithoutRefreshTokenLogoutBodyModel;
import models.logout.WithoutRefreshTokenLogoutResponseModel;
import models.logout.WrongReusedRefreshTokenResponseModel;
import models.registration.RegistrationBodyModel;
import org.junit.jupiter.api.Test;
import testData.TestData;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.logout.LogoutSpec.*;
import static specs.registration.RegistrationSpec.registrationRequestSpec;
import static specs.registration.RegistrationSpec.successfulRegistrationResponseSpec;
import static testData.TestData.*;

public class LogoutTests extends TestBase {


    TestData td = new TestData();

    @Test
    public void successfulLogoutTest() {
        RegistrationBodyModel registrationData = new RegistrationBodyModel(td.username, td.password);

        step("Зарегистрировать пользователя", () -> {
            given(registrationRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });
        String refreshToken = step("Авторизоваться и получить refresh token", () -> {
            LoginBodyModel data = new LoginBodyModel(td.username, td.password);
            return given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().path("refresh");
        });
        step("Выполнить logout с refresh token", () -> {
            LogoutBodyModel logoutData = new LogoutBodyModel(refreshToken);
            given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(successfulLogoutResponseSpec);
        });
    }

    @Test
    public void logoutWithReusedRefreshTokenShouldReturn401Test() {

        step("Зарегистрировать пользователя", () -> {
            RegistrationBodyModel registrationData = new RegistrationBodyModel(td.username, td.password);
            given(registrationRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });
        String refreshToken = step("Авторизоваться и получить refresh token", () -> {
            LoginBodyModel data = new LoginBodyModel(td.username, td.password);
            return given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().path("refresh");
        });
        step("Выполнить logout с refresh token", () -> {
            LogoutBodyModel logoutFirstData = new LogoutBodyModel(refreshToken);
            given(logoutRequestSpec)
                    .body(logoutFirstData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(successfulLogoutResponseSpec);
        });
        step("Повторно выполнить logout с тем же refresh token", () -> {
            LogoutBodyModel logoutSecondData = new LogoutBodyModel(refreshToken);
            WrongReusedRefreshTokenResponseModel logoutResponse =
                    given(logoutRequestSpec)
                            .body(logoutSecondData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(invalidTokenLogoutResponseSpec)
                            .extract().as(WrongReusedRefreshTokenResponseModel.class);

            String actualDetailReusedRefreshToken = logoutResponse.detail();
            String actualCodeReusedRefreshToken = logoutResponse.code();
            assertThat(actualDetailReusedRefreshToken).isEqualTo(EXPECTED_ERROR_TOKEN_IS_BLACKLISTED);
            assertThat(actualCodeReusedRefreshToken).isEqualTo(EXPECTED_TOKEN_NOT_VALID_CODE);
        });
    }

    @Test
    public void logoutWithoutRefreshTokenNegativeTest() {

        step("Проверить ошибку при logout без refresh token", () -> {
            WithoutRefreshTokenLogoutBodyModel logoutData = new WithoutRefreshTokenLogoutBodyModel();
            WithoutRefreshTokenLogoutResponseModel logoutResponse =
                    given(logoutRequestSpec)
                            .body(logoutData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(withoutRefreshTokenLogoutResponseSpec)
                            .extract().as(WithoutRefreshTokenLogoutResponseModel.class);

            String actualErrorWithoutRefreshToken = logoutResponse.refresh().get(0);
            assertThat(actualErrorWithoutRefreshToken).isEqualTo(EXPECTED_REQUIRED_FIELD);
        });
    }

    @Test
    public void accessTokenInsteadOfRefreshTokenNegativeTest() {

        step("Зарегистрировать пользователя", () -> {
            RegistrationBodyModel registrationData = new RegistrationBodyModel(td.username, td.password);
            given(registrationRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec);
        });
        String accessToken = step("Авторизоваться и получить access token", () -> {
            LoginBodyModel data = new LoginBodyModel(td.username, td.password);
            return given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract().path("access");
        });
        step("Проверить ошибку при logout с access token вместо refresh token", () -> {
            LogoutBodyModel logoutData = new LogoutBodyModel(accessToken);
            WrongReusedRefreshTokenResponseModel logoutResponse =
                    given(logoutRequestSpec)
                            .body(logoutData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(invalidTokenLogoutResponseSpec)
                            .extract().as(WrongReusedRefreshTokenResponseModel.class);

            String actualDetailReusedRefreshToken = logoutResponse.detail();
            String actualCodeReusedRefreshToken = logoutResponse.code();
            assertThat(actualDetailReusedRefreshToken).isEqualTo(EXPECTED_ERROR_WRONG_TOKEN_TYPE);
            assertThat(actualCodeReusedRefreshToken).isEqualTo(EXPECTED_TOKEN_NOT_VALID_CODE);
        });
    }

}
