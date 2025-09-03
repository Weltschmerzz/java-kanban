package ru.yandex.practicum.TaskTracker.test;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.TaskTracker.src.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpHandlersIntegrationTest {

    private static final int PORT = 8080;
    private static final String BASE = "http://localhost:" + PORT;
    private static HttpTaskServer server;

    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws Exception {
        server = new HttpTaskServer("test");
        server.start();
    }

    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        delete("/tasks");
        delete("/subtasks");
        delete("/epics");

        stopServer();
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).header("Accept", "application/json").GET().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> delete(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).DELETE().build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).header("Content-Type", "application/json; charset=utf-8").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private static void assertStatus(int expected, HttpResponse<?> resp) {
        assertEquals(expected, resp.statusCode(), "Неверный HTTP статус. Тело: " + resp.body());
    }

    private static void assertStatus2xx(HttpResponse<?> resp) {
        Assertions.assertTrue(resp.statusCode() >= 200 && resp.statusCode() < 300, "Ожидался 2xx. Было " + resp.statusCode() + ". Тело: " + resp.body());
    }

    private static int extractFirstId(String body) {
        Matcher m = Pattern.compile("id:\\s*(\\d+)").matcher(body);
        if (m.find()) return Integer.parseInt(m.group(1));
        Assertions.fail("Не удалось извлечь id из ответа: " + body);
        return -1;
    }

    //Task

    //1. POST /tasks -> 201; GET /tasks возвращает созданную задачу
    @Test
    void task_create_thenList() throws Exception {
        HttpResponse<String> create = post("/tasks", """
                    {
                      "name": "Задача А",
                      "description": "Описание задачи",
                      "status": "NEW",
                      "duration": "PT1H30M",
                      "startTime": "2025-08-19 10:30:22"
                    }
                """);
        assertStatus(201, create);

        HttpResponse<String> list = get("/tasks");
        assertStatus(200, list);
        Assertions.assertTrue(list.body().contains("Задача А"));
    }

    //2. GET /tasks/{id} возвращает задачу по id
    @Test
    void task_getById() throws Exception {
        HttpResponse<String> create = post("/tasks", """
                    { "name": "Задача B", "description": "X", "status":"NEW" }
                """);
        assertStatus(201, create);
        int taskId = extractFirstId(create.body());

        HttpResponse<String> getById = get("/tasks/" + taskId);
        assertStatus(200, getById);
        Assertions.assertTrue(getById.body().contains("\"id\":" + taskId));
        Assertions.assertTrue(getById.body().contains("Задача B"));
    }

    //3. POST /tasks с существующим id -> обновляет (2xx)
    @Test
    void task_update_existing() throws Exception {
        HttpResponse<String> create = post("/tasks", """
                    { "name": "Задача C", "description": "X", "status":"NEW" }
                """);
        int id = extractFirstId(create.body());

        HttpResponse<String> update = post("/tasks", """
                    {
                      "id": %d,
                      "name": "Задача C (обновлена)",
                      "description": "Y",
                      "status": "DONE",
                      "duration": "PT45M",
                      "startTime": "2025-08-20 12:00:00"
                    }
                """.formatted(id));
        assertStatus2xx(update);

        HttpResponse<String> getById = get("/tasks/" + id);
        assertStatus(200, getById);
        Assertions.assertTrue(getById.body().contains("обновлена"));
        Assertions.assertTrue(getById.body().contains("\"status\":\"DONE\""));
    }

    //4. POST /tasks с несуществующим id -> 404
    @Test
    void task_update_notFound() throws Exception {
        HttpResponse<String> update = post("/tasks", """
                    { "id": 999, "name": "Не существует", "description":"...", "status":"NEW" }
                """);
        assertStatus(404, update);
    }

    //5. DELETE /tasks/{id} удаляет одну
    //   DELETE /tasks удаляет все
    @Test
    void task_delete_one_and_all() throws Exception {
        int id1 = extractFirstId(post("/tasks", """
                { "name":"T1","description":"d","status":"NEW" }""").body());
        int id2 = extractFirstId(post("/tasks", """
                { "name":"T2","description":"d","status":"NEW" }""").body());

        HttpResponse<String> delOne = delete("/tasks/" + id1);
        assertStatus2xx(delOne);

        HttpResponse<String> list1 = get("/tasks");
        assertStatus(200, list1);
        Assertions.assertFalse(list1.body().contains("\"id\":" + id1));
        Assertions.assertTrue(list1.body().contains("\"id\":" + id2));

        HttpResponse<String> delAll = delete("/tasks");
        assertStatus2xx(delAll);

        HttpResponse<String> list2 = get("/tasks");
        assertStatus(200, list2);
        Assertions.assertTrue(list2.body().equals("[]") || !list2.body().contains("\"id\":"));
    }

    //6. Пересечение интервалов по времени -> 406
    @Test
    void task_overlap_returnsConflict() throws Exception {
        // первая задача 10:00 на 2 часа
        assertStatus(201, post("/tasks", """
                    { "name":"A","description":"d","status":"NEW","duration":"PT2H","startTime":"2025-08-21 10:00:00" }
                """));

        // вторая пересекается 11:00 на 1 час
        HttpResponse<String> resp = post("/tasks", """
                    { "name":"B","description":"d","status":"NEW","duration":"PT1H","startTime":"2025-08-21 11:00:00" }
                """);
        int sc = resp.statusCode();
        assertEquals(406, sc, "Ожидался 406, был " + sc + ". Тело: " + resp.body());
    }

    //7. пустое имя задачи -> 400
    @Test
    void task_validation_emptyName() throws Exception {
        HttpResponse<String> resp = post("/tasks", """
                { "name":"", "description":"d", "status":"NEW" }""");
        assertStatus(400, resp);
    }

    //Epic

    //1. POST /epics -> 201; GET /epics возвращает созданный эпик
    @Test
    void epic_create_thenList() throws Exception {
        HttpResponse<String> create = post("/epics", """
                    { "name": "Эпик 1", "description": "Очень важный эпик!" }
                """);
        assertStatus(201, create);

        HttpResponse<String> list = get("/epics");
        assertStatus(200, list);
        Assertions.assertTrue(list.body().contains("Эпик 1"));
        Assertions.assertTrue(list.body().contains("\"status\":\"NEW\""));
    }

    //2. GET /epics/{id} возвращает эпик
    @Test
    void epic_getById() throws Exception {
        int epicId = extractFirstId(post("/epics", """
                    { "name": "Эпик X", "description": "..." }
                """).body());

        HttpResponse<String> resp = get("/epics/" + epicId);
        assertStatus(200, resp);
        Assertions.assertTrue(resp.body().contains("\"id\":" + epicId));
        Assertions.assertTrue(resp.body().contains("\"status\":\"NEW\""));
    }

    //3. POST /epics с существующим id -> обновляет (2xx)
    @Test
    void epic_update_existing() throws Exception {
        int epicId = extractFirstId(post("/epics", """
                    { "name": "Эпик к апдейту", "description": "D" }
                """).body());

        HttpResponse<String> update = post("/epics", """
                    { "id": %d, "name": "Эпик обновлён", "description": "D2", "status":"NEW" }
                """.formatted(epicId));
        assertStatus2xx(update);

        HttpResponse<String> resp = get("/epics/" + epicId);
        assertStatus(200, resp);
        Assertions.assertTrue(resp.body().contains("обновлён"));
    }

    //4. POST /epics с несуществующим id -> 404
    @Test
    void epic_update_notFound() throws Exception {
        HttpResponse<String> update = post("/epics", """
                    { "id": 777, "name": "Нет такого", "description": "N/A", "status":"NEW" }
                """);
        assertStatus(404, update);
    }

    //5. DELETE /epics/{id} удаляет эпик и его подзадачи
    @Test
    void epic_delete_cascadesSubtasks() throws Exception {
        int epicId = extractFirstId(post("/epics", """
                    { "name":"Эпик с сабтасками", "description":"root" }
                """).body());

        // создаём сабтаск под эпик
        int subId = extractFirstId(post("/subtasks", """
                    {
                      "epicId": %d,
                      "id": 0,
                      "name": "Child",
                      "description": "sub",
                      "status": "NEW",
                      "duration": "PT30M",
                      "startTime": "2025-08-19 10:30:22"
                    }
                """.formatted(epicId)).body());

        // удаляем эпик
        assertStatus2xx(delete("/epics/" + epicId));

        // сабтаск тоже должен исчезнуть
        HttpResponse<String> subGet = get("/subtasks/" + subId);
        assertStatus(404, subGet);

        // список сабтасков эпика теперь 404 (эпик не найден)
        HttpResponse<String> listAfter = get("/epics/" + epicId + "/subtasks");
        assertStatus(404, listAfter);
    }

    //6. DELETE /epics очищает все эпики
    @Test
    void epic_deleteAll() throws Exception {
        assertStatus(201, post("/epics", """
                { "name":"E1","description":"d"}
                """));
        assertStatus(201, post("/epics", """
                { "name":"E2","description":"d"}
                """));

        assertStatus2xx(delete("/epics"));

        HttpResponse<String> list = get("/epics");
        assertStatus(200, list);
        Assertions.assertTrue(list.body().equals("[]") || !list.body().contains("\"id\":"));
    }

    //7. Валидатор: пустое имя эпика -> 400
    @Test
    void epic_validation_emptyName() throws Exception {
        HttpResponse<String> resp = post("/epics", """
                { "name":"", "description":"d" }
                """);
        assertStatus(400, resp);
    }

    //Subtasks

    //1. POST /epics -> 201 и GET /epics возвращает созданный эпик
    @Test
    void createEpic_thenList() throws Exception {
        String epicJson = """
                {
                  "name": "Эпик 1",
                  "description": "Очень важный эпик!"
                }
                """;
        HttpResponse<String> createResp = post("/epics", epicJson);
        assertStatus(201, createResp);

        HttpResponse<String> listResp = get("/epics");
        assertStatus(200, listResp);
        String body = listResp.body();

        Assertions.assertTrue(body.contains("Эпик 1"));
        Assertions.assertTrue(body.contains("\"status\":\"NEW\""));
    }

    //2. POST /subtasks с epicId=0 -> 400"
    @Test
    void createSubtask_withZeroEpicId_returns400() throws Exception {
        String json = """
                {
                  "epicId": 0,
                  "id": 1,
                  "name": "Bad Subtask",
                  "description": "no epic",
                  "status": "DONE",
                  "duration": "PT1H",
                  "startTime": "2025-08-18 10:30:22"
                }
                """;
        HttpResponse<String> resp = post("/subtasks", json);
        assertStatus(400, resp);
    }

    //3. POST /subtasks с несуществующим epicId -> 404
    @Test
    void createSubtask_withUnknownEpic_returns404() throws Exception {
        String json = """
                {
                  "epicId": 999,
                  "id": 0,
                  "name": "Orphan",
                  "description": "no parent",
                  "status": "NEW",
                  "duration": "PT30M",
                  "startTime": "2025-08-18 10:30:22"
                }
                """;
        HttpResponse<String> resp = post("/subtasks", json);
        assertStatus(404, resp);
    }

    //4. Cоздать эпик -> создать сабтаск -> обновить сабтаск
    @Test
    void subtask_fullCycle_createAndUpdate() throws Exception {
        HttpResponse<String> epicResp = post("/epics", """
                { "name": "Эпик для сабтасков", "description": "Корневой эпик" }
                """);
        assertStatus(201, epicResp);

        //создаём сабтаск
        HttpResponse<String> subCreate = post("/subtasks", """
                {
                  "epicId": 1,
                  "id": 0,
                  "name": "Первая подзадача",
                  "description": "Описание",
                  "status": "NEW",
                  "duration": "PT1H",
                  "startTime": "2025-08-19 10:30:22"
                }
                """);
        assertStatus(201, subCreate);

        //обновляем сабтаск той же ветки
        HttpResponse<String> subUpdate = post("/subtasks", """
                {
                  "epicId": 1,
                  "id": 2,
                  "name": "Подзадача обновлена",
                  "description": "Новое описание",
                  "status": "DONE",
                  "duration": "PT2H",
                  "startTime": "2025-08-19 11:00:00"
                }
                """);
        assertStatus(201, subUpdate);

        //проверим, что GET /epics/1/subtasks содержит обновлённую подзадачу
        HttpResponse<String> list = get("/epics/1/subtasks");
        assertStatus(200, list);
        String body = list.body();
        Assertions.assertTrue(body.contains("Подзадача обновлена"));
        Assertions.assertTrue(body.contains("\"status\":\"DONE\""));
    }

    //5. POST /subtasks когда id == epicId -> 400"
    @Test
    void subtask_idEqualsEpicId_returns400() throws Exception {
        // создаём эпик id=1
        HttpResponse<String> epic = post("/epics", """
                { "name": "Эпик X", "description": "..." }
                """);
        assertStatus(201, epic);

        // пытаемся создать сабтаск с id=1 и epicId=1
        HttpResponse<String> bad = post("/subtasks", """
                {
                  "epicId": 1,
                  "id": 1,
                  "name": "Ломаем",
                  "description": "должно быть 400",
                  "status": "NEW"
                }
                """);
        assertStatus(400, bad);
    }

    // Prioritized

    //1. Пустой приоритизованный список -> 200 и []
    @Test
    void prioritized_empty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = get("/prioritized");
        assertStatus(200, resp);
        Assertions.assertEquals("[]", resp.body(), "Ожидался пустой массив для /prioritized");
    }

    //2. Сортировка по времени старта (возрастание)
    @Test
    void prioritized_ordersByStartTimeAscending() throws Exception {
        // Позже
        assertStatus(201, post("/tasks", """
                {
                  "name":"P-09",
                  "description":"d",
                  "status":"NEW",
                  "duration":"PT30M",
                  "startTime":"2025-08-21 09:00:00"
                }
                """));
        // Раньше
        assertStatus(201, post("/tasks", """
                {
                  "name":"P-08",
                  "description":"d",
                  "status":"NEW",
                  "duration":"PT30M",
                  "startTime":"2025-08-21 08:00:00"
                }
                """));

        HttpResponse<String> resp = get("/prioritized");
        assertStatus(200, resp);
        String body = resp.body();

        //задача с 08:00 идёт раньше задачи с 09:00
        int i08 = body.indexOf("\"name\":\"P-08\"");
        int i09 = body.indexOf("\"name\":\"P-09\"");
        Assertions.assertTrue(i08 >= 0 && i09 >= 0);
        Assertions.assertTrue(i08 < i09);
    }

    //History

    //1. История изначально пуста -> 200 и []
    @Test
    void history_initiallyEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = get("/history");
        assertStatus(200, resp);
        Assertions.assertEquals("[]", resp.body(), "Ожидался пустой массив для /history");
    }

    //2. После GET /tasks/{id} задача попадает в историю; повторный просмотр не даёт дублей
    @Test
    void history_populatesOnGetTaskAndHasNoDuplicates() throws Exception {
        HttpResponse<String> create = post("/tasks", """
                {
                  "name": "H-Task",
                  "description": "for history",
                  "status": "NEW",
                  "duration": "PT15M",
                  "startTime": "2025-08-22 10:00:00"
                }
                """);
        assertStatus(201, create);
        int id = extractFirstId(create.body());

        // Дважды откроем задачу по id — в истории должна быть одна запись
        HttpResponse<String> one = get("/tasks/" + id);
        assertStatus(200, one);
        HttpResponse<String> two = get("/tasks/" + id);
        assertStatus(200, two);

        HttpResponse<String> history = get("/history");
        assertStatus(200, history);
        String body = history.body();

        // Должна присутствовать задача
        Assertions.assertTrue(body.contains("\"name\":\"H-Task\""), "Задача должна быть в истории. Тело: " + body);
        // ровно один раз
        int first = body.indexOf("\"name\":\"H-Task\"");
        int last  = body.lastIndexOf("\"name\":\"H-Task\"");
        Assertions.assertEquals(first, last, "В истории не должно быть дублей. Тело: " + body);
    }

}
