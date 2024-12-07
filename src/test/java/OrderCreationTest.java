import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import praktikum.models.Ingredients;
import praktikum.models.JsonUserResponse;
import praktikum.models.User;
import praktikum.utils.BaseTest;
import praktikum.utils.UserUtils;
import praktikum.utils.OrderUtils;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

public class OrderCreationTest extends BaseTest {
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
    }

    @Test
    @DisplayName("Создание заказа")
    public void createOrderWithAuthorization() {
        OrderUtils orderUtils = new OrderUtils(ingredientsList);

        Response orderResponse = orderUtils.createOrder(bearerToken);

        logResponse(orderResponse);

        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());

        String burgerName = orderResponse.jsonPath().getString("name");
        assertThat("Имя бургера должно быть задано", burgerName, is(not(emptyOrNullString())));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthorization() {

        OrderUtils orderUtils = new OrderUtils(ingredientsList);


        Response orderResponse;
        orderResponse = orderUtils.createOrder(null);


        logResponse(orderResponse);

        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());


        String burgerName = orderResponse.jsonPath().getString("name");
        assertThat("Имя бургера должно быть задано", burgerName, is(not(emptyOrNullString())));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {


        Response orderResponse = OrderUtils.createOrderRequest(Collections.emptyList(), bearerToken);

        logResponse(orderResponse);

        checkClientErrorResponse(orderResponse, 400, "Ingredient ids must be provided");
    }

    @Test
    @DisplayName("Создание заказа с невалидным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHashes() {


        List<String> invalidIngredientIds = Arrays.asList("", "");


        Response orderResponse = OrderUtils.createOrderRequest(invalidIngredientIds, bearerToken);


        logResponse(orderResponse);


        checkServerErrorResponse(orderResponse, 500);
    }

    @After
    public void tearDown() {
        if (bearerToken != null) {
            UserUtils.deleteUser(bearerToken);
            bearerToken = null;
        }
    }
}