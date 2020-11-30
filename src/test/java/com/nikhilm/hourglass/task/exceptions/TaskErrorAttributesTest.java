package com.nikhilm.hourglass.task.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskErrorAttributesTest {

    @Test
    public void testCreateMap()  {
        TaskErrorAttributes taskErrorAttributes = new TaskErrorAttributes();
        Map<String, Object> map = new HashMap<>();
        TaskException taskException = new TaskException(404, "Exception 404");
        assertTrue(taskErrorAttributes.createMap(map, taskException).containsValue(HttpStatus.NOT_FOUND));
        TaskException taskException1 = new TaskException(409, "Exception 409");
        assertTrue(taskErrorAttributes.createMap(map, taskException1).containsValue(HttpStatus.CONFLICT));
        TaskException taskException2 = new TaskException(500, "Exception generic");
        assertTrue(taskErrorAttributes.createMap(map, taskException2).containsValue(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}