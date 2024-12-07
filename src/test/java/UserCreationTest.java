import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import praktikum.models.JsonUserResponse;
import praktikum.models.User;
import praktikum.utils.BaseTest;
import praktikum.utils.UserUtils;

import static praktikum.utils.UserUtils.generateUserData;

public class UserCreationTest extends BaseTest {
    private String bearerToken;

    @Test
    @DisplayName("Создание уникального пользователя")
    public void testCreateUser() {
        User newUser = generateUserData(true, true, true);
        Response createUserResponse = UserUtils.createUser(newUser);

        JsonUserResponse jsonUserResponse = checkStatusCodeAndResponse(createUserResponse, 200, true);
        this.bearerToken = jsonUserResponse.getAccessToken(); // Сохранение токена для дальнейшего использования
        logResponse(createUserResponse);
    }

    @Test
    @DisplayName("Создание дублирующего пользователя")
    public void testCreateDuplicateUser() {

        User newUser = generateUserData(true, true, true);


        Response createUserResponse = UserUtils.createUser(newUser);
        JsonUserResponse jsonUserResponse = checkStatusCodeAndResponse(createUserResponse, 200, true); // Ожидаем статус 201
        this.bearerToken = jsonUserResponse.getAccessToken();
        logResponse(createUserResponse);

        Response duplicateUserResponse = UserUtils.createUser(newUser);
        checkClientErrorResponse(duplicateUserResponse, 403, "User already exists");
        logResponse(duplicateUserResponse);
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля")
    public void testCreateUserWithoutRequiredField() {
        User newUser = generateUserData(false, true, true);

        Response createUserResponse = UserUtils.createUser(newUser);

        checkClientErrorResponse(createUserResponse, 403, "Email, password and name are required fields"); // Ожидаем сообщение об ошибке
        logResponse(createUserResponse);
    }


    @After
    public void tearDown() {
        if (bearerToken != null) {
            UserUtils.deleteUser(bearerToken);
            bearerToken = null;
        }
    }
}