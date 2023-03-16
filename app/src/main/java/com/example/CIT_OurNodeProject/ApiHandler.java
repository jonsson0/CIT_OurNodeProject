package com.example.CIT_OurNodeProject;

import org.json.*;

import java.util.Locale;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node){
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        Request request = new Request();
        request.fromString(input);

        return request;
    }

    public String createHttpRequestAsString(String method, String path, String body) {

        Request request = new Request();
        JSONObject json = request.toJson(method, path, body);

        return json.toString();
    }

    public  Response readHttpResponse(String input) {

        Response response = new Response();
        response.fromString(input);
        return response;
    }

    public  String createHttpResponseAsString(String status, String body) {

        Response response = new Response();
        return response.toJson(status, body).toString();
    }

    public String requestHandler(String requestString) {

        Request request = readHttpRequest(requestString);
        String answer = "";

        switch (request.path.toLowerCase(Locale.ROOT)) {
            case "getid":
                answer = createHttpResponseAsString("200 ok", node.IP);

            case "newneighbor(left)":
                // answer = createHttpResponseAsString("200 ok", node.IP);

            case "getphonebook":
                JSONObject json = new JSONObject();
                node.phoneBookRight.toString();
                try {
                    json.put("rightNeighbors", node.phoneBookRight.toString());
                    json.put("leftNeighbors", node.phoneBookLeft.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                answer = createHttpResponseAsString("200 ok", json.toString());

            case "getdata":

                for (Data data : node.listOfData) {
                    if (data.id.equalsIgnoreCase(request.body)) {
                        answer = createHttpResponseAsString("200 ok", node.listOfData.toString());
                        break;
                    } else {

                    }
                }
                
                if (node.listOfData.contains(request.body)) {
                    answer = createHttpResponseAsString("200 ok", node.listOfData.toString());

                }
        }

        return answer;
    }


}
