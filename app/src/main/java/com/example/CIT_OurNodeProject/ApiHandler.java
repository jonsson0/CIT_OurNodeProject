package com.example.CIT_OurNodeProject;

import org.json.*;

public class ApiHandler {


    public Request readHttpRequest(String input) throws RuntimeException{

        Request request = new Request();
        request.fromString(input);

        return request;
    }

    public String createHttpRequest(String method, String path, String body) throws RuntimeException{

        Request request = new Request();
        JSONObject json = request.toJson(method, path, body);

        return json.toString();
    }

    public static Response readHttpResponse(String input) throws RuntimeException{

        Response response = new Response();
        response.fromString(input);
        return response;

    }

    public static String createHttpResponse(String status, String body) throws RuntimeException{
        Response response = new Response();

        return response.toJson(status, body).toString();

    }

}
