import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import praktikum.models.JsonUserResponse;
import praktikum.models.User;
import praktikum.utils.BaseTest;
import praktikum.utils.UserUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class UserDataUpdateTest extends BaseTest {

    private String bearerToken;



    @Test
    @DisplayName("Обновление данных пользователя с авторизацией")
    public void testUpdateUserWithAuthorization() {
        // Генерация уникальных данных пользователя
        User newUser = UserUtils.generateUserData(true, true, true);


        Response createUserResponse = UserUtils.createUser(newUser);
        JsonUserResponse jsonUserResponse = checkStatusCodeAndResponse(createUserResponse, 200, true); // Ожидаем статус 201
        bearerToken = jsonUserResponse.getAccessToken();
        logResponse(createUserResponse);

        // Изменение данных пользователя
        Map<String, String> updatedData = new HashMap<>();

        // Генерация новых случайных данных для обновления
        User updatedUser = UserUtils.generateUserData(true, true, true); // Генерируем нового пользователя с случайным именем и email
        updatedData.put("email", updatedUser.getEmail()); // Запрашиваем email из нового пользователя
        updatedData.put("name", updatedUser.getName()); // Запрашиваем имя из нового пользователя

        Response updateResponse = UserUtils.updateUser(bearerToken, updatedData);
        JsonUserResponse updateJsonUserResponse = checkStatusCodeAndResponse(updateResponse, 200, false);

        assertTrue(updateJsonUserResponse.isSuccess());
        assertTrue(updatedUser.getEmail().equalsIgnoreCase(updateJsonUserResponse.getUser().getEmail()));
        assertEquals(updatedUser.getName(), updateJsonUserResponse.getUser().getName());
        logResponse(updateResponse);
    }

    @Test
    @DisplayName("Обновление данных пользователя без авторизации")
    public void testUpdateUserWithoutAuthorization() {
        Map<String, String> updatedData = new HashMap<>();
        updatedData.put("email", "newemail@example.com");
        updatedData.put("name", "New Name");

        Response updateResponse = UserUtils.updateUser(null, updatedData); // Передаем null вместо токена
        checkClientErrorResponse(updateResponse, 401, "You should be authorised");
        logResponse(updateResponse);
    }

    @After
    public void tearDown() {
        if (bearerToken != null) {
            UserUtils.deleteUser(bearerToken);
            bearerToken = null;
        }
    }
}