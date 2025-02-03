import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.practicum.OrderData;
import org.practicum.OrderTrack;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Тесты создания заказа")
public class OrderCreationTests {
    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @ParameterizedTest
    @DisplayName("Создание заказа")
    @MethodSource("orderData")
    public void testCreateOrder(String firstName, String lastName, String address, int metroStation, String phone, int rentTime, String deliveryDate, String comment, String[] color){
        OrderData orderData = createOrderDataObj(firstName,lastName,address,metroStation,phone,rentTime,deliveryDate,comment,color);
        Response response = sendPostRequestOrders(orderData);
        printResponseBodyToConsole(response);
        compareStatusCode(response,201);
        checkResponseBodyNotNull(response,"track");
        closeOrder(response);
    }
    @Step("Create orderData")
    public OrderData createOrderDataObj(String firstName, String lastName, String address, int metroStation, String phone, int rentTime, String deliveryDate, String comment, String[] color){
        OrderData orderData = new OrderData(firstName,lastName,address,metroStation,phone,rentTime,deliveryDate,comment,color);
        return orderData;
    }
    @Step("Send POST request to /api/v1/orders")
    public Response sendPostRequestOrders(OrderData json){
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/orders");
        return response;
    }
    @Step("Send PUT request to /api/v1/orders/cancel")
    //fixme в документации баг, ручка на отмену заказа, только через параметр.
    public Response sendPutRequestOrders(int track){
        Response response = given()
                .header("Content-type", "application/json")
                .queryParam("track", Integer.toString(track))
                .when()
                .put("/api/v1/orders/cancel");
        printResponseBodyToConsole(response);
        return response;
    }
    @Step("Clearing the test data. closing a order by track")
    public void closeOrder(Response response){
        OrderTrack orderTrack = response.as(OrderTrack.class);
        int track = orderTrack.getTrack();
        compareStatusCode(sendPutRequestOrders(track), 200);
        System.out.println("Заказ отменен");
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


    @Step("Print response body to console")
    public void printResponseBodyToConsole(Response response){
        System.out.println(response.body().asString());
    }

    static Stream<Arguments> orderData() {
        String[] colors = {"BLACK", "GREY"};
        String[] black = {"BLACK"};
        String[] grey = {"GREY"};
        String[] emptyStr = {""};
        return Stream.of(
                arguments("Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 5, "2020-06-06", "Saske, come back to Konoha", colors),
                arguments("Nar", "Uchiha", "Konoha, 143 apt.", 5, "+7 800 355 35 36", 6, "2020-06-07", "Saske, come back to Sipoha", black),
                arguments("Nuto", "Uchiha", "Konoha, 144 apt.", 6, "+7 800 355 35 37", 7, "2020-06-08", "Saske, come back to Totoha", grey),
                arguments("Nato", "Uchiha", "Konoha, 145 apt.", 7, "+7 800 355 35 38", 8, "2020-06-09", "Saske, come back to Momoha", emptyStr)
        );
    }
}
