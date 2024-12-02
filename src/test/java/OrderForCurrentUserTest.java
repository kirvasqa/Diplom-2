import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import praktikum.models.Ingredients;
import praktikum.models.JsonUserResponse;
import praktikum.models.User;
import praktikum.utils.BaseTest;
import praktikum.utils.OrderUtils;
import praktikum.utils.UserUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

public class OrderForCurrentUserTest extends BaseTest {
    private String bearerToken;
    private List<Ingredients> ingredientsList;
    private Map<String, String> ingredientIdToNameMap;

    @Before
    public void setup() {

        User newUser = UserUtils.generateUserData(true, true, true);
        Response createUserResponse = UserUtils.createUser(newUser);
        JsonUserResponse jsonUserResponse = checkStatusCodeAndResponse(createUserResponse, 200, true);
        bearerToken = jsonUserResponse.getAccessToken();
        logResponse(createUserResponse);

        Response ingredientsResponse = OrderUtils.getIngredients();
        ingredientsList = ingredientsResponse.jsonPath().getList("data", Ingredients.class);
        logResponse(ingredientsResponse);

        ingredientIdToNameMap = new HashMap<>();
        for (Ingredients ingredient : ingredientsList) {
            ingredientIdToNameMap.put(ingredient.get_id(), ingredient.getName());
        }
        OrderUtils orderUtils = new OrderUtils(ingredientsList);
        Response orderResponse = orderUtils.createOrder(bearerToken);
        logResponse(orderResponse);
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    public void getUserOrdersAuthorized() {
        Response orderResponse = OrderUtils.getUserOrders(bearerToken);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", is(not(empty())))
                .body("total", isA(Integer.class))
                .body("totalToday", isA(Integer.class));
        logResponse(orderResponse);
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    public void getUserOrdersUnauthorized() {
        Response orderResponse = OrderUtils.getUserOrdersUnauthorized();
        orderResponse.then()
                .statusCode(401)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
        logResponse(orderResponse);
    }

    @After
    public void tearDown() {
        if (bearerToken != null) {
            UserUtils.deleteUser(bearerToken);
            bearerToken = null;
        }
    }
}