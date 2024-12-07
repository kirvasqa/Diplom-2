package praktikum.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import praktikum.models.JsonUserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static praktikum.utils.UserUtils.gson;

public class BaseTest {

    @Step("Парсинг ответа")
    protected JsonUserResponse parseResponse(Response response) {
        String responseBody = response.getBody().asString();
        JsonUserResponse jsonUserResponse = gson.fromJson(responseBody, JsonUserResponse.class);

        if (jsonUserResponse == null) {
            throw new IllegalArgumentException("Неверный формат ответа. Ожидался JSON, получен: " + responseBody);
        }
        return jsonUserResponse;
    }

    @Step("Проверка успешного ответа")
    protected void checkSuccessfulResponse(JsonUserResponse jsonUserResponse, boolean requireTokens) {
        assertThat(jsonUserResponse.isSuccess())
                .as("Ответ не успешен, ожидался успех")
                .isTrue();
        checkUserData(jsonUserResponse);

        if (requireTokens) {
            checkTokens(jsonUserResponse);
        }
    }

    @Step("Проверка данных пользователя")
    private void checkUserData(JsonUserResponse jsonUserResponse) {
        assertThat(jsonUserResponse.getUser()).isNotNull()
                .as("Данные пользователя отсутствуют в ответе");
        assertThat(jsonUserResponse.getUser().getEmail()).isNotBlank()
                .as("Email пользователя отсутствует или пустой");
        assertThat(jsonUserResponse.getUser().getName()).isNotBlank()
                .as("Имя пользователя отсутствует или пустое");
    }

    @Step("Проверка токенов")
    private void checkTokens(JsonUserResponse jsonUserResponse) {
        assertThat(jsonUserResponse.getAccessToken()).isNotNull()
                .as("Access token отсутствует или пустой");

        assertThat(jsonUserResponse.getRefreshToken()).isNotNull()
                .as("Refresh token отсутствует или пустой");
    }

    @Step("Проверка успешного статуса и тела ответа")
    protected JsonUserResponse checkStatusCodeAndResponse(Response response, int expectedStatusCode, boolean requireTokens) {
        int statusCode = response.statusCode();

        if (statusCode == expectedStatusCode) {
            JsonUserResponse jsonUserResponse = parseResponse(response);
            checkSuccessfulResponse(jsonUserResponse, requireTokens);
            return jsonUserResponse;
        } else {
            throw new IllegalArgumentException("Неожидаемый статус-код: " + statusCode);
        }
    }

    @Step("Проверка статус-кода и сообщения об ошибке")
    protected void checkClientErrorResponse(Response response, int expectedStatusCode, String expectedErrorMessage) {
        int statusCode = response.statusCode();

        if (statusCode >= 400 && statusCode < 500) {
            assertEquals("Статус код не соответствуем ожидаемому",expectedStatusCode,statusCode);
            handleClientErrorResponse(response, expectedErrorMessage);
        } else {
            throw new IllegalArgumentException("Статус-код не соответствует ошибке клиента: " + statusCode);
        }
    }

    @Step("Обработка ошибок клиента")
    public void handleClientErrorResponse(Response response, String expectedErrorMessage) {
        JsonUserResponse jsonUserResponse = parseResponse(response);


        assertThat(jsonUserResponse.isSuccess()).isFalse()
                .as("Ответ должен быть неуспешным");


        assertThat(jsonUserResponse.getMessage())
                .as("Должно быть сообщение об ошибке")
                .isEqualTo(expectedErrorMessage);
    }

    @Step("Проверка ответа сервера на ошибку")
    public void checkServerErrorResponse(Response response, int expectedStatusCode) {
        assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    }

    @Step("Логирование ответа")
    public static void logResponse(Response response) {
        if (response != null) {
            String responseBody = response.getBody().asString();
            String responseHeaders = response.getHeaders().toString();
            int statusCode = response.statusCode();

            String message = String.format("Response Status Code: %d\nHeaders: %s\nBody: %s",
                    statusCode, responseHeaders, responseBody);

            Allure.addAttachment("API Response", message);
        } else {
            Allure.addAttachment("API Response", "Ответ сервера равен null");
        }
    }

}
