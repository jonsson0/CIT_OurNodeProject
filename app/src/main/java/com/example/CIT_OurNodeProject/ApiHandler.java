package com.example.CIT_OurNodeProject;

import android.util.Pair;

import org.json.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node){
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        Request request = new Request();
        System.out.println("44444444444444444444");
        request.fromString(input);

        return request;
    }

    public String createHttpRequestAsString(String method, String path, String body) {

        Request request = new Request();

        JSONObject json = request.toJson(method, path, body);

        System.out.printf("7777777777777");
        System.out.println(json);

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

    // For server:
    public String requestHandler(String requestString) {
        System.out.println("33333333333333333333");
        System.out.println(requestString);
        Request request = readHttpRequest(requestString);
        String answer = "";

        switch (request.path.toLowerCase()) {
            case "getid":
                answer = createHttpResponseAsString("200 ok", node.IP);

            case "newneighbor(left)":
                // answer = createHttpResponseAsString("200 ok", node.IP);

            case "getphonebook":
                JSONObject json = new JSONObject();
                node.phoneBookRight.toString();
                System.out.println("22222222222222222222222");
                System.out.println(node.phoneBookRight.toString());
                try {
                    json.put("rightNeighbors", node.phoneBookRight.toString());
                    json.put("leftNeighbors", node.phoneBookLeft.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                answer = createHttpResponseAsString("200 ok", json.toString());

            case "getdata":
                System.out.println("This is getData case");
                String Id;
                try {
                    JSONObject data = request.body.getJSONObject("Data");
                    Id = data.getString("Id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(request.body);
                System.out.println(node.listOfData.get(0).id);
                for (Data data : node.listOfData) {
                    if (data.id.equalsIgnoreCase(Id)) {
                        answer = createHttpResponseAsString("200 ok", data.data);
                        break;
                    } else {
                        System.out.println("else get data");
                    }
                }
            case "adddata":

        }

        return answer;
    }

    // for client:

    public void getId() {
        createHttpRequestAsString("get", "getid", "");
    }
    public void NewNeighbor() {

    }
    public void getPhonebook() {
        createHttpRequestAsString("get", "getphonebook", "");
    }
    public String getData(String value) {
        System.out.println("88888888888");
        String hashedValue;
        try {
            hashedValue = SHA256.toHexString(SHA256.getSHA(value));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject json = new JSONObject();
        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Id", hashedValue );

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            json.put("Data", innerJson);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println("999999999");

       return createHttpRequestAsString("get", "getid", json.toString());

    }
    public String addData(String data, boolean isParent, boolean isGlobal) {
        String hashedValueId = "";
        try {
            hashedValueId = SHA256.toHexString(SHA256.getSHA(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject bodyJson = new JSONObject();
        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Hashed value id", hashedValueId );
            innerJson.put("value"          , data);
            innerJson.put("IsParent"       , isParent);
            innerJson.put("IsGlobal"       , isGlobal);
            bodyJson.put("data", innerJson);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return createHttpRequestAsString("put", "adddata", bodyJson.toString());


    }
    public void DeleteData() {

    }

//    private JSONObject createJsonObject(HashMap<String, String>[] values){
//    private JSONObject createJsonObject(String... strings2){
//
//        JSONObject json = new JSONObject();
//        for (Pair value: values) {
//
//            json.put(value.getKey(), value.getValue());
//        }
//
//
//    }





}
