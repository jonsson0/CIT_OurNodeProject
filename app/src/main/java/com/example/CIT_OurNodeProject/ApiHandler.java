package com.example.CIT_OurNodeProject;

import org.json.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ApiHandler {

    Node node;

    public ApiHandler(Node node) {
        this.node = node;
    }

    public Request readHttpRequest(String input) {

        Request request = new Request(input);

        return request;
    }

    public Response readHttpResponse(String input) {

        Response response = new Response(input);
        return response;
    }

    // For server:
    public Response requestHandler(Request request, String IP) {
        Response response = new Response();
        switch (request.path.toLowerCase()) {
            case "getid":
                response = buildResponseToGetId(response);
                break;
            case "updatephonebook":
                response = buildResponseToNewNeighbor(response, IP);
                break;

            case "getphonebook":
                response = buildResponseToGetPhonebook(response);
                break;
            case "getdata":
                response = buildResponseToGetData(response, request);
                break;
            case "adddata":
                try {
                    response = buildResponseToAddData(request);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                buildResponseBadRequest(response);
                break;
        }

        return response;
    }

    public Response respondHandler(Request request, Response response) throws JSONException, IOException {

        switch (request.path.toLowerCase()) {
            case "getid":
                // Do nothing
                break;
            case "updatephonebook":
                // Do nothing
                break;
            case "getphonebook":
                // Do nothing
                break;
            case "getdata":
                getDataHandler(request, response);
                break;
            case "adddata":
                buildResponseToAddData(request);
                break;
            default:

                break;
        }
        return response;
    }


    // for client:

    public Response buildResponseToGetId(Response response) {
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

    public Response buildResponseToNewNeighbor(Response response, String IP) {
        response.status = "200 OK";

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonArrayOfData = new JSONArray();


        try {

            if (jsonBody.getString("Side").equals("left")) {
                Neighbor neighbor = new Neighbor();
                neighbor.IP = IP;
                node.neighborLeft = neighbor;
            }


            for (Data data : node.listOfData) {
                JSONObject jsonDataObject = new JSONObject();
                jsonDataObject.put("Id", data.id);
                jsonDataObject.put("Value", data.value);
                jsonDataObject.put("IsParent", "False");
                jsonDataObject.put("IsGlobal", "False");

                jsonArrayOfData.put(jsonDataObject);
            }
            jsonBody.put("data", jsonArrayOfData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        response.body = jsonBody;
        return response;
    }

    public Response buildResponseToGetPhonebook(Response response) {
        response.status = "200 OK";
        ArrayList<String> phoneBookRight = node.phoneBookRight.IPs;
        ArrayList<String> phoneBookLeft = node.phoneBookLeft.IPs;

        // creating the json body for the response:
        JSONObject jsonBody = new JSONObject();

        JSONArray jsonPhoneBookRight = new JSONArray();
        JSONArray jsonPhoneBookLeft = new JSONArray();
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

    public Response buildResponseToGetData(Response response, Request request) {

        String id;
        JSONObject jsonDataFromRequest;

        try {
            jsonDataFromRequest = request.body.getJSONObject("Data");
            id = jsonDataFromRequest.getString("Id");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Data data : node.listOfData) {
            System.out.println(data.id);
            if (data.id.equals(id)) {
                    System.out.println("into if");
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

    public Request buildRequestToGetData(String value) {
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

        Request request = new Request("get", "getData", json);
        return request;
    }

    public Request buildRequestToAddData(String value, Boolean isParent) {

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
            innerJson.put("Value", value);
            innerJson.put("isParent", isParent.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            json.put("Data", innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request("put", "addData", json);
        return request;
    }


    public Response DeleteData(Request request) throws IOException, JSONException {
        JSONObject RequestData = request.body.getJSONObject("data");
        String Value = RequestData.getString("Value");
        Response response = new Response();

        //delete own data if we have it
        if (RequestData.getBoolean("isParent")) {
            //if parent ask us to delete, we delete if we can. Will always give 200 even if we dont hold data
            node.deleteDataLocally(Value);
            response = new Response("200 - deleted locally");
        } else {
            for (int i = 0; i < node.listOfData.size(); i++) {
                //if our node has the data
                if (node.listOfData.get(i).equals(Value)) {
                    if (node.listOfData.get(i).isParent == true) {
                        //IF we are parent of the data, ask children to delete the data
                        //create request for children with isParent=True
                        Request requestsForChildren = makeRequestForParentToCallDeleteOnChildren(request);
                        //send requests to children
                        Response responseLeft = sendRequestToNeighbor(node.neighborLeft.IP, requestsForChildren);
                        Response responseRight = sendRequestToNeighbor(node.neighborRight.IP, requestsForChildren);
                        //delete locally
                        node.deleteDataLocally(Value);
                        //delete from neighbor representation objects.
                        node.neighborRight.deleteDataLocally(Value);
                        node.neighborLeft.deleteDataLocally(Value);


                        if (responseLeft.status == "200" && responseRight.status == "200") {
                            response = new Response("200 - deleted locally and on children");
                        } else {
                            response = new Response("400 - unable to delete data from one or more children");
                        }
                    }
                    //IF we are not the parent we should just pass the request along to a neighbor
                    else {
                         response = sendRequestToNeighbor(node.neighborLeft.IP, request);
                    }
                }
            }
        }
        return response;
    }



    private Request makeRequestForParentToCallDeleteOnChildren(Request request) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        JSONObject RequestData = request.body.getJSONObject("data");

        jsonBody.put("ID",RequestData.get("id"));
        jsonBody.put("Value",RequestData.get("Value"));
        jsonBody.put("isParent",true);

        Request newRequest = new Request("get", request.path, jsonBody);
        return newRequest;


    }


    public Response buildResponseToAddData(Request request) throws IOException, JSONException {

        JSONObject requestData = request.body.getJSONObject("Data");
        String value = requestData.getString("Value");
        boolean isParent = requestData.getBoolean("isParent");

        if (!isParent){
            //if not parent
            Data data = new Data(value,false);

            // TODO Check what neighbor sent the data and add on correct side add the data

            node.listOfData.add(data);
           // node.neighborLeft.listOfData.add();
            JSONObject responseBody=new JSONObject();
            Response response =new Response("200", responseBody);
            return response;
        } else if(isParent){
            //Adds to own

            Data data = new Data(value,true);
            node.listOfData.add(data);
            System.out.println("THIS IS THE DATA ID:");
            System.out.println(data.id);
            //make new request with isParent=false
            Request requestForNeighbor=createRequestForNeighborReplication(data, request);

            //data for internal representation of neighbors data
            Data dataForNeighbor = new Data(value,Boolean.FALSE);
            node.neighborLeft.listOfData.add(dataForNeighbor);
            Response r1 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
            Response r2 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
            if (r1.status=="200"&& r2.status=="200"){
                Response response =new Response("OK 200", new JSONObject("{\"data\":\"Data added to parent and replicated to children\""));
                return response;
            }
        }
        Response response =new Response("400", new JSONObject("{\"data\":\"\""));
        return response;
    }

    public  Response sendRequestToNeighbor(String IP, Request request) throws IOException {
        try {
        Socket connectionToNeighbor;
        connectionToNeighbor = new Socket(IP, 4444);
        DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
        outputStream.writeUTF(request.toString());
        connectionToNeighbor.close();

        DataInputStream instream = new DataInputStream(connectionToNeighbor.getInputStream());

        }catch (Exception e){
           // Response response = new Response("400", new JSONObject());

        }


        Response response = new Response("OK 200", new JSONObject());
        return response;
    }
    public  Request createRequestForNeighborReplication(Data data, Request originalRequest) throws JSONException {
        JSONObject jsonBody = new JSONObject();

        jsonBody.put("ID",data.id);
        jsonBody.put("Value",data.value);
        jsonBody.put("isParent",false);
        jsonBody.put("Data", jsonBody);
        Request newRequest = new Request("get", originalRequest.path, originalRequest.body);
        return newRequest;


    }
    public Response DeleteData(Response response) {

        return response;
    }

    public Response buildResponseBadRequest(Response response) {

        return response;
    }


    public Response getDataHandler(Request request, Response response) {

        Request originalRequest = new Request(request.method, request.path, request.body);

        if (response.status.contains("OK")) {
            return response;
        } else {
            boolean hasGottenData = false;
            PhoneBook copyPhonebook = node.phoneBookLeft.copy();

            System.out.println("THIS IS THE IPS:");
            for (String IP : copyPhonebook.IPs) {
                System.out.println(IP);
            }
            while (!hasGottenData) {
                System.out.println("1");
                String IP = copyPhonebook.IPs.get(0);
                System.out.println(" the IP is: " + IP);
                Socket connectionToServer;
                try {
                    connectionToServer = new Socket(IP, 4444);
                    System.out.println("2");
                    DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                    System.out.println("3");
                    DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());
                    System.out.println("4");
//                            System.out.println(instream.readUTF());
//                            String messageFromServer = instream.readUTF();
//                            response = new Response(messageFromServer);

                            /*
                            if (node.IP.equals(copyPhonebook.IPs.get(0))) {
                                response = new Response();
                                response.status = "404 Not Found";
                                response.body = new JSONObject();
                            }
                            */

                    if (copyPhonebook.IPs.size() == 1) {
                        request = new Request("get", "getPhoneBook", new JSONObject());
                        String newMessage = request.toString();
                        System.out.println("5");
                        outstream.writeUTF(newMessage);
                        outstream.flush();
                        String messageFromServer = instream.readUTF();
                        response = new Response(messageFromServer);
                        JSONArray jsonPhoneBookCopy;
                        try {
                            jsonPhoneBookCopy = response.body.getJSONArray("LeftNeighbors");
                            for (int i = 0; i < jsonPhoneBookCopy.length(); i++) {
                                copyPhonebook.IPs.add(jsonPhoneBookCopy.getJSONObject(i).getString("IP"));
//                                        System.out.println(" - " + copyPhonebook.IPs.get());
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        System.out.println("6");
                    } else {
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("Id", "left");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        request = new Request("get", "getData", jsonBody);
                        System.out.println("7");
                        outstream.writeUTF(originalRequest.toString());
                        outstream.flush();
                        System.out.println();
                        String messageFromServer = instream.readUTF();
                        System.out.println("Response from server: " + messageFromServer);

                        response = new Response(messageFromServer);
                        if (response.status.contains("OK")) {
                            hasGottenData = true;
                            break;
                        }
                    }
                    connectionToServer.close();
                    copyPhonebook.IPs.remove(0);
                    System.out.println("Phonebook: " + copyPhonebook.IPs);

                } catch (IOException e) {
                    System.out.println("problem is: " + e.toString());
                    throw new RuntimeException(e);
                }
            }
            return response;
        }
    }


}
