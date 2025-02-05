import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.practicum.CourierId;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Тесты логина курьера в системе")
public class CourierLoginTests {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }


    @DisplayName("Логин курьера в системе")
    @Test
    public void testLoginCourierWithValidData(){
        String json = "{\n" +
                "    \"login\": \"DartVader\",\n" +
                "    \"password\": \"1234568\"\n" +
                "}";
        createCourier();
        Response response = sendPostRequestCourierLogin(json);
        compareStatusCode(response, 200);
        checkResponseBodyNotNull(response, "id");
        printResponseBodyToConsole(response);
        deleteCourier();
    }

    @DisplayName("Логин курьера в системе без параметра")
    @Test
    public void testLoginCourierWithInvalidPass(){
        String json = "{\n" +
                "    \"login\": \"DartVader\",\n" +
                "    \"password\": \"\"\n" +
                "}";
        createCourier();
        Response response = sendPostRequestCourierLogin(json);
        compareStatusCode(response, 400);
        compareResponseToText(response, "message","Недостаточно данных для входа");
        printResponseBodyToConsole(response);
        deleteCourier();;
    }
    @DisplayName("Логин несуществующего курьера в системе")
    @Test
    public void testLoginNonExistentCourier(){
        String json = "{\n" +
                "    \"login\": \"enakin89\",\n" +
                "    \"password\": \"321\"\n" +
                "}";
        Response response = sendPostRequestCourierLogin(json);
        compareStatusCode(response, 404);
        compareResponseToText(response, "message","Учетная запись не найдена");
        printResponseBodyToConsole(response);
    }

    @Step("Send POST request to /api/v1/courier")
    public Response sendPostRequestCourier(String json){
        Response response = given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier");
        return response;
    }
    @Step("Send POST request to /api/v1/courier/login")
    public Response sendPostRequestCourierLogin(String json){
        Response response = given()
                .header("Content-type", "application/json")
                .body(json)
                .when()
                .post("/api/v1/courier/login");
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

    @Step("Clearing the test data. deleting a courier by id")
    public void deleteCourier(){
        String json = "{\n" +
                "    \"login\": \"DartVader\",\n" +
                "    \"password\": \"1234568\"\n" +
                "}";
        CourierId courierId = given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/v1/courier/login").body().as(CourierId.class);
        int id = courierId.getId();
        compareStatusCode(sendDeleteRequestCourier(id), 200);
        System.out.println("Курьер удален");
    }

    @Step("Создание курьера")
    public void createCourier(){
        String json = "{\n" +
                "    \"login\": \"DartVader\",\n" +
                "    \"password\": \"1234568\",\n" +
                "    \"firstName\": \"DartVader\"\n" +
                "}";
        Response response = sendPostRequestCourier(json);
        String resp = response.asString();
        boolean isTrue = resp.contains("Этот логин уже используется. Попробуйте другой.");
        if(isTrue){
            deleteCourier();
            response = sendPostRequestCourier(json);
            compareStatusCode(response, 201);
        }
        else {
            printResponseBodyToConsole(response);
            compareStatusCode(response, 201);
        }
    }
}
