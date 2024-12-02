package praktikum.utils;

import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import praktikum.generators.UserDataGenerator;
import praktikum.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserUtils extends BaseTest {
    static Gson gson = new Gson();

    @Step("Генерация данных пользователя")
    public static User generateUserData(boolean includeLogin, boolean includePassword, boolean includeName) {
        String email = includeLogin ? UserDataGenerator.generateRandomEmail(8) : null;
        String password = includePassword ? UserDataGenerator.generateRandomPassword(8) : null;
        String name = includeName ? UserDataGenerator.generateRandomName(9) : null;

        return new User(email, password, name);
    }

    @Step("Создание пользователя")
    public static Response createUser(User user) {
        String userJson = gson.toJson(user);
        return createUserOnServer(userJson);
    }

    @Step("Создание пользователя на сервере")
    public static Response createUserOnServer(String userJson) {
        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Content-type", "application/json")
                .body(userJson)
                .when()
                .post("api/auth/register");
    }


    @Step("Авторизация пользователя")
    public static Response login(User user) {

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", user.getEmail());
        authRequest.put("password", user.getPassword());

        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Content-type", "application/json")
                .body(authRequest)
                .when()
                .post("api/auth/login");
    }

    @Step("Обновление данных пользователя")
    public static Response updateUser(String bearerToken, Map<String, String> updatedData) {
        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Content-type", "application/json")
                .when()
                .header("Authorization", bearerToken != null ? bearerToken : "") // Проверка на null
                .body(updatedData)
                .patch("api/auth/user");
    }


    @Step("Удаление пользователя по Bearer токену")
    public static void deleteUser(String bearerToken) {
        Response response = RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Authorization", bearerToken)
                .when()
                .delete("api/auth/user");

        logResponse(response);


    }
}