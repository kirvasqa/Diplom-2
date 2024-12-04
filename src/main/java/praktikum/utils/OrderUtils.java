package praktikum.utils;

import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.RestAssured;
import praktikum.models.Ingredients;

import java.util.*;
import java.util.stream.Collectors;

public class OrderUtils {

    private List<Ingredients> ingredientsList;
    private Random random;


    public OrderUtils(List<Ingredients> ingredients) {
        this.ingredientsList = ingredients;
        this.random = new Random();
    }

    @Step("Получение списка ингредиентов")
    public static Response getIngredients() {
        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .when()
                .get("api/ingredients");
    }

    @Step("Выбор случайных ингредиентов")
    public List<String> selectRandomIngredients() {
        List<String> ingredientTypes = Arrays.asList("bun", "main", "sauce");
        List<String> selectedIngredientIds = new ArrayList<>();


        for (String type : ingredientTypes) {
            selectedIngredientIds.add(getRandomIdByType(type));
        }

        return selectedIngredientIds;
    }

    private String getRandomIdByType(String type) {
        List<String> filteredIds = ingredientsList.stream()
                .filter(ingredient -> ingredient.getType().equals(type))
                .map(Ingredients::get_id)
                .collect(Collectors.toList());


        if (!filteredIds.isEmpty()) {
            return filteredIds.get(random.nextInt(filteredIds.size()));
        }
        return null;
    }

    @Step("Создание заказа")
    public Response createOrder(String bearerToken) {
        List<String> selectedIngredientIds = selectRandomIngredients();
        return createOrderRequest(selectedIngredientIds, bearerToken);
    }
    public String createEmptyOrderRequestBody() {
        Map<String, List<String>> requestBodyMap = new HashMap<>();
        requestBodyMap.put("ingredients", Collections.emptyList()); // Пустой список ингредиентов

        return new Gson().toJson(requestBodyMap);
    }


    @Step("Отправка запроса на создание заказа")
    public static Response createOrderRequest(List<String> ingredientIds, String bearerToken) {
        // Если ingredientIds равен null, создаем пустой список
        if (ingredientIds == null) {
            ingredientIds = new ArrayList<>();
        }

        Map<String, List<String>> body = new HashMap<>();
        body.put("ingredients", ingredientIds);


        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Content-type", "application/json")
                .header("Authorization", bearerToken != null ? bearerToken : "")
                .body(body)
                .post("api/orders");
    }
    @Step("Получение списка заказов")
    public static Response getUserOrders(String bearerToken) {
        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site/")
                .header("Authorization", bearerToken)
                .when()
                .get("api/orders");
    }

    @Step("Получение списка заказов без авторизации")
    public static Response getUserOrdersUnauthorized() {
        return RestAssured.given()
                .baseUri("https://stellarburgers.nomoreparties.site")
                .when()
                .get("/api/orders");
    }
}