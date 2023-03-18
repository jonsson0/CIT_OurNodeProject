package com.example.CIT_OurNodeProject;

import android.util.Pair;

import org.json.*;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ApiHandler {

    Node node;
    MainActivity activity;

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
        String rep = response.toJson(status, body).toString();

        return rep;

    }

    // For server:
    public String requestHandler(String requestString) {
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
                try {
                    json.put("rightNeighbors", node.phoneBookRight.toString());
                    json.put("leftNeighbors", node.phoneBookLeft.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                answer = createHttpResponseAsString("200 ok", json.toString());

            case "getdata":

                String res = "";

                String Id;
                try {
                    JSONObject data = request.body.getJSONObject("Data");
                    Id = data.getString("Id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                for (Data data : node.listOfData) {
                    if (data.id.equalsIgnoreCase(Id)) {
                        answer = createHttpResponseAsString("200 ok", data.data);
                        res = createHttpResponseAsString("200 ok", data.data);
                        break;
                    } else {
                        System.out.println("else get data");
                    }
                }
                answer = res;
            case "adddata":

                JSONObject body;


                String id;
                String dataValue;
                String isParent;
                String isGlobal;
                try {
                    body = request.body.getJSONObject("data");
                    id = body.getString("Hashed value id");
                    dataValue = body.getString("value");
                    isParent = body.getString("IsParent");
                    isGlobal = body.getString("IsGlobal");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("before toJson");
                addData(dataValue, isParent, isGlobal);

                answer = createHttpResponseAsString("200 ok","");
        System.out.println("------------------" + answer);
        }

        return answer;
    }

    public void addData(String dataValue, String isParent, String isGlobal){

        System.out.println("1010010100101001010100101010100101010100");
        Data data = new Data(dataValue);
//        System.out.println(node.getData(data));

        if (!Boolean.parseBoolean(isParent)){
            node.addData(data);
            node.getData(data).isParentData = false;
        } else if (Boolean.parseBoolean(isParent)) {
            node.addData(data);
            node.getData(data).isParentData = true;
            activity.makeRequest(node.phoneBookLeft.IPs.get(0), requestAddData(data, false, false));
            // trigger MainActivity to make requests to neighbours
        }
    }



    // for client:

    public String getId() {
        return createHttpRequestAsString("get", "getid", "");
    }
    public String requestNewNeighbor(String side) {

        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Side", side );

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return createHttpRequestAsString("get", "getid", innerJson.toString());
    }
    public void getPhonebook() {
        createHttpRequestAsString("get", "getphonebook", "");
    }
    public String getData(String value) {
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

       return createHttpRequestAsString("get", "getid", json.toString());

    }

    public String requestGetData(String value) {
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

        return createHttpRequestAsString("get", "getid", json.toString());

    }
    public String requestAddData(Data data, boolean isParent, boolean isGlobal) {
        String hashedValueId;
        try {
            hashedValueId = SHA256.toHexString(SHA256.getSHA(data.data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject bodyJson = new JSONObject();
        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Hashed value id", hashedValueId );
            innerJson.put("value"          , data.data);
            innerJson.put("IsParent"       , isParent);
            innerJson.put("IsGlobal"       , isGlobal);
            bodyJson.put("data", innerJson);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return createHttpRequestAsString("put", "adddata", bodyJson.toString());

    }
    public String deleteData(Data data, boolean isParent, boolean isGlobal) {
        String hashedValueId;
        try {
            hashedValueId = SHA256.toHexString(SHA256.getSHA(data.data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        JSONObject innerJson = new JSONObject();

        try {
            innerJson.put("Hashed value id", hashedValueId );
            innerJson.put("IsParent"       , isParent);
            innerJson.put("IsGlobal"       , isGlobal);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return createHttpRequestAsString("delete", "deletedata", innerJson.toString());

    }






}
