package com.api.demo.utilityfunctions;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;

public class JsonResponse {

    public static JSONObject JsonResponse(String message, Object object, HttpStatus status) {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", status);
        jsonResponse.put("object", object);
        jsonResponse.put("message", message);
        return jsonResponse;
    }
}
