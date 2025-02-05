import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Тесты получение списка заказов")
public class OrdersListTests {
    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }
    @DisplayName("Получение списка заказов")
    @Test
    public void testGetListOrders(){
        Response response = sendGetRequestOrdersList();
        compareStatusCode(response, 200);
        checkResponseBodyNotNull(response, "orders");
    }

    @Step("Send GET request to /api/v1/orders")
    public Response sendGetRequestOrdersList(){
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders");
        return response;
    }
    @Step("Check the key for a null value")
    public void checkResponseBodyNotNull(Response response, String key){
        response
                .then()
                .assertThat()
                .body(key, notNullValue());
    }

    @Step("Checking the status code")
    public void compareStatusCode(Response response, int statusCode){
        response
                .then()
                .statusCode(statusCode);
    }
}
