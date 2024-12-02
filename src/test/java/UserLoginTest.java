import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import praktikum.models.JsonUserResponse;
import praktikum.models.User;
import praktikum.utils.BaseTest;
import praktikum.utils.UserUtils;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserLoginTest extends BaseTest {

    private String bearerToken;

    @Test
    @DisplayName("Авторизация нового пользователя")
    public void testCreateAndLoginUser()  {

        User newUser = UserUtils.generateUserData(true, true, true);

        Response createUserResponse = UserUtils.createUser(newUser);

        JsonUserResponse jsonUserResponse = checkStatusCodeAndResponse(createUserResponse, 200, true);
        assertNotNull("Access token не должен быть null", jsonUserResponse.getAccessToken());

        bearerToken = jsonUserResponse.getAccessToken();
        logResponse(createUserResponse);

        User authUser = new User(newUser.getEmail(), newUser.getPassword(), null);
        Response loginResponse = UserUtils.login(authUser);

        JsonUserResponse loginJsonUserResponse = checkStatusCodeAndResponse(loginResponse, 200, true); // Ожидаем статус 200

        assertTrue("Email не совпадает без учета регистра", newUser.getEmail().equalsIgnoreCase(loginJsonUserResponse.getUser().getEmail()));
        logResponse(loginResponse);
    }

    @Test
    @DisplayName("Авторизация пользователя с неверными данными")
    public void testLoginWithInvalidData() {
        User invalidAuthUser = new User("wrongemail@example.com", "wrongpassword", null);

        Response loginResponse = UserUtils.login(invalidAuthUser);

        checkClientErrorResponse(loginResponse, 403, "email or password are incorrect");
        logResponse(loginResponse);
    }
    @After
    public void tearDown() {
        if (bearerToken != null) {
            UserUtils.deleteUser(bearerToken);
            bearerToken = null;
        }
    }
}