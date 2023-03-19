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

    public Request createHttpRequest(String method, String path, String body) {

        Request request = new Request();
        request.method = method;
        request.body = request.bodyToJson(body);
        request.path= path;


//        JSONObject json = request.toJson(method, path, body);
//

        return request;
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
//    public String requestHandler(String requestString) {
    public String requestHandler(Request request) {
//        Request request = readHttpRequest(requestString);
        String answer = "";

        switch (request.path.toLowerCase()) {
            case "getid":
                answer = createHttpResponseAsString("200 ok", node.IP);
                break;

            case "newneighbor(left)":
                // answer = createHttpResponseAsString("200 ok", node.IP);
                break;

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
                break;


            case "getdata":

                boolean dataFound = false;
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
                        dataFound = true;
                        break;
                    } else {
                        System.out.println("else get data");
                    }
                }

                if (!dataFound) {
                    answer = createHttpResponseAsString("404 not found", "");
            }

                break;

            case "adddata":

                JSONObject body;


                String id, dataValue, isParent, isGlobal;
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
                break;

        }

        return answer;
    }

    public void addData(String dataValue, String isParent, String isGlobal){

        Data data = new Data(dataValue);

        if (!Boolean.parseBoolean(isParent)){
            node.addData(data);
            node.getData(data).isParentData = false;
        } else if (Boolean.parseBoolean(isParent)) {
            node.addData(data);
            node.getData(data).isParentData = true;
            activity.makeRequest(node.phoneBookLeft.IPs.get(0), requestAddData(data, false, false));
            activity.makeRequest(node.phoneBookRight.IPs.get(0), requestAddData(data, false, false));
        }

        // global?

        //
    }


    public boolean checkResponse(String resp) {
//        Request request = new Request();
//        request.fromString(req);


        Request request = activity.latestRequest;

        Response response = new Response();
        response.fromString(resp);

        if (response.status.contains("202")) {
            return true;
        }
        return false;
    }

    public void checkResponse2(String resp){
//        Request request = new Request();
//        request.fromString(req);



        Request request = activity.latestRequest;

        Response response = new Response();
        response.fromString(resp);

        if (response.status.contains("202")){
            return;
        }


        switch (request.path){
            case "getdata":
//                if (activity.latestRequest.requestNumber <= 3){
//                if (activity.latestRequest.requestNumber != 1){

                    try {
//                        String dataString = request.body.getString("Data");
                        JSONObject dataJson = request.body.getJSONObject("Data");

//                        String dumb = dataString.
                        requestGetData(dataJson.getString("Id"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    String requestIp;
                    int phonebookNumber = activity.latestRequest.requestNumber  % 3 ;
                    if (phonebookNumber == 2 && activity.latestRequest.requestNumber >= 2){
    //                    if (phonebookNumber == 2 ){
                        activity.makeRequest(node.phoneBookLeft.IPs.get(2), getPhonebook());



                    }




//                    else if (phonebookNumber == 2){
//                        activity.makeRequest(node.phoneBookLeft.IPs.get(2), getPhonebook());
//                        System.out.println("response.body bitch " + response.body);
//                    }


                    requestIp = node.phoneBookLeft.IPs.get(phonebookNumber);
                    activity.makeRequest(requestIp, request);
                    request.requestNumber ++;
//                }



                break;


            case "w":
                break;
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
    public String getPhonebook() {




        return createHttpRequestAsString("get", "getphonebook", "");
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

       return createHttpRequestAsString("get", "getdata", json.toString());

    }

    public String requestGetData2(String value) {
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

//        return createHttpRequestAsString("get", "getid", json.toString());
        return createHttpRequestAsString("get", "getdata", json.toString());

    }

    public Request requestGetData(String value) {
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

//        return createHttpRequestAsString("get", "getid", json.toString());
        return createHttpRequest("get", "getdata", json.toString());

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
