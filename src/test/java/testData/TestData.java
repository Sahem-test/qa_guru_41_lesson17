package testData;

import net.datafaker.Faker;

public class TestData {

    public static Faker faker = new Faker();

    public String
            username = faker.name().firstName(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
            email = faker.internet().emailAddress(),
            password = faker.regexify("[A-Za-z0-9]{8}"),
            wrongPassword = password + "1",
            tooLongPassword = "a".repeat(129);

    public static final String
            IP_ADDRESS_REGEXP = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
            EXPECTED_ERROR_INVALID_USERNAME_OR_PASSWORD = "Invalid username or password.",
            EXPECTED_UNAUTHORIZED_ERROR = "Authentication credentials were not provided.",
            EXPECTED_ERROR_EXISTING_USER = "A user with that username already exists.",
            EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.",
            EXPECTED_ERROR_NOT_BE_BLANK = "This field may not be blank.",
            EXPECTED_ERROR_LONGER_REQUIRED_LENGTH_PASSWORD = "Ensure this field has no more than 128 characters.",
            EXPECTED_REQUIRED_FIELD = "This field is required.",
            EXPECTED_ERROR_INVALID_REFRESH_TOKEN = "something text",
            EXPECTED_ERROR_VALID_TOKEN = "Token is invalid",
            EXPECTED_ERROR_WRONG_TOKEN_TYPE = "Token has wrong type",
            EXPECTED_ERROR_TOKEN_IS_BLACKLISTED = "Token is blacklisted",
            EXPECTED_TOKEN_NOT_VALID_CODE = "token_not_valid";

}
