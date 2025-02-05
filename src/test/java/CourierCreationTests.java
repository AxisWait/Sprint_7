import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.practicum.CourierId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты по созданию курьера")
public class CourierCreationTests {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }
    private static final String ENDPOINT_COURIER = "/api/v1/courier";

    @DisplayName("Создание курьера")
    @Test
    public void testCreateCourierWithValidData(){
        String json = "{\n" +
                "    \"login\": \"DartVader\",\n" +
                "    \"password\": \"1234568\",\n" +
                "    \"firstName\": \"DartVader\"\n" +
                "}";
        Response response = sendPostRequestCourier(json);
        printResponseBodyToConsole(response);
        responseCheckTrue(response);
        compareStatusCode(response,201);
        deleteCourier(json);
    }

    @DisplayName("Создание уже существующего курьера")
    @Test
    public void testCreateCourierWithExistingLogin() {
        String json = "{\n" +
                "    \"login\": \"ninja\",\n" +
                "    \"password\": \"1234\",\n" +
                "    \"firstName\": \"saske\"\n" +
                "}";
        Response response = sendPostRequestCourier(json);
        compareResponseToText(response, "message", "Этот логин уже используется. Попробуйте другой.");
        compareStatusCode(response,409);
        printResponseBodyToConsole(response);
    }

    @DisplayName("Создание курьера с невалидными данными (без логина и пароля)")
    @Test
    public void testCreateCourierWithInvalidLogin() {
        String json = "{\n" +
                "    \"login\": \"\",\n" +
                "    \"password\": \"\",\n" +
                "    \"firstName\": \"enakin\"\n" +
                "}";
        Response response = sendPostRequestCourier(json);
        compareResponseToText(response, "message", "Недостаточно данных для создания учетной записи");
        compareStatusCode(response, 400);
        printResponseBodyToConsole(response);
    }


    @Step("Send POST request to /api/v1/courier")
    public Response sendPostRequestCourier(String json){
        Response response = given()
                .log()
                .all()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post(ENDPOINT_COURIER);
        return response;
    }
    @Step("Send DELETE request to /api/v1/courier/:id")
    public Response sendDeleteRequestCourier(int id){
        Response response = given()
                .header("Content-type", "application/json")
                .when()
                .delete("/api/v1/courier/{id}", id);
        return response;
    }


    @Step("Compare response to something")
    public void compareResponseToText(Response response, String key, String message){
        response
                .then()
                .assertThat()
                .body(key,equalTo(message));
    }
    @Step("Checking for successful account creation")
    public void responseCheckTrue(Response response){
        String resp = response.asString();
        boolean isOkTrue = resp.contains("{\"ok\":true}");
        assertTrue(isOkTrue);
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

    @Step("Clearing the test data. deleting a courier by id")
    public void deleteCourier(String json){
        CourierId courierId = given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/v1/courier/login").body().as(CourierId.class);
        int id = courierId.getId();
        compareStatusCode(sendDeleteRequestCourier(id), 200);
        System.out.println("Курьер удален");
    }
}
