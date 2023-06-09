package com.example.CIT_OurNodeProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;


public class ServerManager {


    Node node;

    ClientManager clientManager;

    public ServerManager(Node node){
        this.node = node;
        clientManager = new ClientManager(node);
    }

    // Take any request and call the correct request handle methods
    public Response handleRequestFromClient(Request request, String clientIP){
        Response response = new Response();

        switch (request.path.toLowerCase()) {
            case "getid":
                response = generateResponse_GetId();
                break;
            case "updatephonebook":
                response = generateResponse_UpdatePhonebook(request);
                break;
            case "getphonebook":
                response = generateResponse_GetPhonebook(request);
                break;
            case "getdata":
                response = generateResponse_GetData(request);
                break;
            case "fixneighbor":
                response = generateResponse_FixNeighbor(request);
                break;
            case "adddata":
                try {
                    response = generateResponse_AddData(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                break;
            case "deletedata":
                try {
                   response = generateResponse_DeleteData(request);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            default:
                generateResponse_BadRequest();
                break;
        }

        return response;

    }

    // Return ID
    public Response generateResponse_GetId() {
        Response response = new Response();
        response.status = "200 OK";
        JSONObject jsonBody = new JSONObject();

        // creating the json body for the response:
        try {
            jsonBody.put("Id", node.IP);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return response;
    }

    // Method for handling update phonebook requests
    public Response generateResponse_UpdatePhonebook(Request request) {

        // TODO check if request is correct

        Response response = new Response();

        response.status = "200 OK";

        JSONObject jsonBody = request.body;
        JSONArray jsonArrayOfIP;
        JSONObject jsonBodyData;
        PhoneBook phoneBook = new PhoneBook();
        // Getting side and phonebook from the request:
        try {
            // outer most layer of json
            jsonBodyData = jsonBody.getJSONObject("Data");

            // inner json
            String side = jsonBodyData.getString("Side");
            jsonArrayOfIP = new JSONArray(jsonBodyData.getString("PhoneBook"));
            // foreach json in jsonArrayOfIP add it to the new phonebook
            for (int i = 0; i < jsonArrayOfIP.length(); i++) {

                JSONObject IpJsonObject = jsonArrayOfIP.getJSONObject(i);
                String IP = IpJsonObject.getString("IP");

                phoneBook.IPs.add(IP);
            }

            // replacing the old phonebook with the new
            if (side.equalsIgnoreCase("left")) {

                // checking if first element in the new phonebook is not my neighbor
                if (phoneBook.IPs.get(0).equals(node.neighborLeft.IP)) {
                    System.out.println("I got a new phonebook, and I dont need to swap my left neighbor");
                } else {
                    System.out.println("I got a new phonebook, and I need to swap left neighbor");

                    node.neighborLeft.IP = phoneBook.IPs.get(0);
                    // clean the data from the Neighbor
                    node.neighborLeft.removeAllData();
                    // Also send the data
                    clientManager.sendOutRequest_addData(node.neighborLeft.IP, node.listOfData, false);

                }
                node.phoneBookLeft = phoneBook;
                System.out.println("New phonebookLeft: " + node.phoneBookLeft.IPs);

            // If we we're asked to update our right phonebook
            } else if (side.equalsIgnoreCase("right")) {

                if (phoneBook.IPs.get(0).equals(node.neighborRight.IP)) {
                    System.out.println("I got a new phonebook, and I dont need to swap my right neighbor");
                } else {
                    System.out.println("I got a new phonebook, and I need to swap right neighbor");
                    node.neighborRight.IP = phoneBook.IPs.get(0);
                    node.neighborRight.removeAllData();

                    clientManager.sendOutRequest_addData(node.neighborRight.IP, node.listOfData, false);
                }

                node.phoneBookRight = phoneBook;
                System.out.println("New phonebookRight:" + node.phoneBookLeft.IPs);
            }

        } catch (JSONException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }

        response.body = new JSONObject();
        return response;
    }

    public Response generateResponse_GetPhonebook(Request request) {
        Response response = new Response();

        response.status = "200 OK";
        ArrayList<String> phoneBookRight = node.phoneBookRight.IPs;
        ArrayList<String> phoneBookLeft = node.phoneBookLeft.IPs;

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();

        JSONArray jsonPhoneBookRight = new JSONArray();
        JSONArray jsonPhoneBookLeft  = new JSONArray();
        try {

            for (String IP : phoneBookRight) {
                JSONObject ipInPhoneBook = new JSONObject();
                ipInPhoneBook.put("Id", "Id???");
                ipInPhoneBook.put("IP", IP);
                jsonPhoneBookRight.put(ipInPhoneBook);
            }
            for (String IP : phoneBookLeft) {
                JSONObject ipInPhoneBook = new JSONObject();
                ipInPhoneBook.put("Id", "Id???");
                ipInPhoneBook.put("IP", IP);
                jsonPhoneBookLeft.put(ipInPhoneBook);
            }

            jsonBody.put("RightNeighbors", jsonPhoneBookRight);
            jsonBody.put("LeftNeighbors", jsonPhoneBookLeft);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return response;
    }

    public Response generateResponse_FixNeighbor(Request request) {

        Response response = new Response();

        String side = "left";
        JSONObject jsonDataFromRequest;

        try {
            jsonDataFromRequest = request.body.getJSONObject("Data");
            side = jsonDataFromRequest.getString("Side");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        clientManager.handleDeadNeighbor(side);


        return response;
    }

    public Response generateResponse_GetData(Request request) {

        Response response = new Response();

        String id;
        JSONObject jsonDataFromRequest;

        try {
            jsonDataFromRequest = request.body.getJSONObject("Data");
            id = jsonDataFromRequest.getString("Id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Data data : node.listOfData) {
            if (data.id.equals(id)) {
                System.out.println("into we have the data");
                JSONObject jsonBody = new JSONObject();
                JSONObject jsonData = new JSONObject();

                try {
                    jsonData.put("Id", data.id);
                    jsonData.put("Value", data.value);

                    jsonBody.put("data", jsonData);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                response.status = "200 OK";
                response.body = jsonBody;
                return response;
            } else {
                response.status = "404 Not Found";
                response.body = new JSONObject();
            }
        }
        return response;
    }

    public Response generateResponse_AddData(Request request) throws IOException, JSONException {

        JSONObject requestData = null;

        try {
            requestData = request.body.getJSONObject("Data");
            String value = requestData.getString("Value");
            boolean isParent = requestData.getBoolean("isParent");

            if (!isParent){
                //if not parent
                Data data = new Data(value,false);

                String senderIP = requestData.getString("senderIP");

                if (senderIP.equals(node.neighborLeft.IP)
                        && !node.neighborLeft.hasData(data)) {
                    node.neighborLeft.listOfData.add(data);
                } else if (senderIP.equals(node.neighborRight.IP)
                        && !node.neighborRight.hasData(data)) {
                    node.neighborRight.listOfData.add(data);
                }

                JSONObject responseBody = new JSONObject();
                responseBody.put("Neighbor","ADDEDSUCCESS");
                Response response = new Response("200", responseBody);

                System.out.println("Response is: " + response.body);
                return response;
            } else if(isParent){
                //Adds to own

                Data data = new Data(value,true);
                node.listOfData.add(data);

//                Request requestForNeighbor =
                // make new request with isParent=false
                Request requestForNeighbor = createRequestForNeighborReplication(data, request, node.IP);




                Response r1 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
                System.out.println("PASSING ONTO FIRST CHILD" + requestForNeighbor.toString());
                Response r2 = sendRequestToNeighbor(node.neighborRight.IP,requestForNeighbor);
                System.out.println("PASSING ONTO SECOND CHILD" + requestForNeighbor.toString());
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("Data Replicated on neighbors", "True");

                if (r1.status.contains("200") && r2.status.contains("200")) {
                    Response response = new Response("OK 200", jsonBody);
                    return response;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Response response =new Response("400",new JSONObject());
        return response;
    }

    public Response generateResponse_DeleteData(Request request) throws JSONException  {

        JSONObject RequestData = request.body.getJSONObject("Data");
        String id = RequestData.getString("Id");
        Response response = new Response();

        boolean hasData = node.checkForData(id);

        if (!hasData & RequestData.getBoolean("isParent")) {

            JSONObject jsonBody = new JSONObject();
            JSONObject innerJson = new JSONObject();

            innerJson.put("Id", id);
            innerJson.put("Value", "EmptyValue");
            innerJson.put("isParent", true);

            jsonBody.put("Data", innerJson);

            Request getDataRequest = new Request("get", "getData", jsonBody);

            response = clientManager.getDataHandler(getDataRequest);

            if (response.status.contains("200 OK")) {
                String IP = response.body.getString("IP");

                String value = RequestData.getString("Value");

                Request requestToDelete = clientManager.generateRequest_DeleteData(value, true);

                sendRequestToNeighbor(IP, requestToDelete);
            }
            return response;
        }

        System.out.println(RequestData.getBoolean("isParent"));

        // delete own data if we have it
        if (RequestData.getBoolean("isParent")) {
            // if parent ask us to delete, we delete if we can. Will always give 200 even if we dont hold data
            boolean isDeleted = node.deleteDataLocally(id);

            // IF we are parent of the data, ask children to delete the data
            // create request for children with isParent=True
            Request requestForChild = clientManager.generateRequest_DeleteData_For_Child_Data(request, false);
            // send requests to children
            System.out.println("Sending request to neighborLeft: " + requestForChild);
            Response responseLeft = sendRequestToNeighbor(node.neighborLeft.IP, requestForChild);
            System.out.println("Sending request to neighborRight: " + requestForChild);
            Response responseRight = sendRequestToNeighbor(node.neighborRight.IP, requestForChild);

            if (responseLeft.status.contains("OK") && responseRight.status.contains("OK")) {
                response = new Response("200 OK", new JSONObject());
            } else {
                response = new Response("400", new JSONObject());
            }

        } else {
            node.neighborRight.deleteDataLocally(id);
            node.neighborLeft.deleteDataLocally(id);
        }
        return response;
    }

    public Response generateResponse_BadRequest() {
        Response response = new Response();
        return response;
    }

    public  Response sendRequestToNeighbor(String IP, Request request) {
        Response response;
        try {
            Socket connectionToNeighbor;
            connectionToNeighbor = new Socket(IP, 4444);
            DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
            outputStream.writeUTF(request.toString());

            // TODO: if we have time actaully get the response instead of making a "fake" one

            connectionToNeighbor.close();

            response = new Response("200 OK", new JSONObject());

        }catch (Exception e){
            response = new Response("400", new JSONObject());
            System.out.println(e);

        }

        return response;
    }

    public  Request     createRequestForNeighborReplication(Data data, Request originalRequest, String senderIP) throws JSONException {
        JSONObject jsonBody=new JSONObject();

        JSONObject jsonData = new JSONObject();

        jsonData.put("Id", data.id);
        jsonData.put("Value", data.value);
        jsonData.put("isParent", false);
        jsonData.put("senderIP", senderIP);

        jsonBody.put("Data", jsonData);

        Request newRequest = new Request("get", originalRequest.path, jsonBody);
        return newRequest;
    }


}
