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

    // What should happen if we get a request with any of these:
    public Response handleRequestFromClient(Request request, String clientIP) {
        Response response = new Response();
        switch (request.path.toLowerCase()) {
            case "getid":
                response = buildResponseToGetId(response);
                break;
            case "updatephonebook":
                response = buildResponseToUpdatePhonebook(request);
                break;
            case "getphonebook":
                response = buildResponseToGetPhonebook();
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

    // What should happen when we get a response from any of these requests:
    public Response handleResponseFromServer(Request request, Response response, String clientIP) throws JSONException, IOException {

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
                // Do nothing
                break;
            default:

                break;
        }
        return response;
    }

    /** BUILDING RESPONSES **/

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

    public Response buildResponseToUpdatePhonebook(Request request) {

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
                    node.neighborLeft.removeAllData();
                }
                node.phoneBookLeft = phoneBook;
                System.out.println("New phonebookLeft: " + node.phoneBookLeft.IPs);

            } else if (side.equalsIgnoreCase("right")) {

                if (phoneBook.IPs.get(0).equals(node.neighborRight.IP)) {
                    System.out.println("I got a new phonebook, and I dont need to swap my right neighbor");
                } else {
                    System.out.println("I got a new phonebook, and I need to swap right neighbor");
                    node.neighborRight.IP = phoneBook.IPs.get(0);
                    node.neighborRight.removeAllData();
                }

                node.phoneBookRight = phoneBook;
                System.out.println("New phonebookRight:" + node.phoneBookLeft.IPs);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        response.body = new JSONObject();
        return response;
    }

    public Response buildResponseToGetPhonebook() {

        Response response = new Response();

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


    /** BUILDING REQUESTS **/

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
            innerJson.put("isParent", isParent);
         //   innerJson.put("SenderIP", )
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
        JSONObject RequestData = request.body.getJSONObject("Data");
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
                            JSONObject j=new JSONObject();
                            j.put("Added", "True");
                            response = new Response("200",j);
                        } else {
                            JSONObject k=new JSONObject();
                            k.put("Added", "False");
                            response = new Response("400", k);
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

    public Request buildRequestToUpdatePhoneBook(PhoneBook phoneBook, String side) {

        String jsonArrayStringOfPhoneBook = phoneBook.toJsonArrayString();

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put("Side", side);
            innerJson.put("PhoneBook", jsonArrayStringOfPhoneBook);
            jsonBody.put("Data", innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Request request = new Request("get", "updatePhoneBook", jsonBody);

        return request;
    }



    private Request makeRequestForParentToCallDeleteOnChildren(Request request) throws JSONException {
        JSONObject jsonBody = new JSONObject();
        JSONObject RequestData = request.body.getJSONObject("Data");

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

            String senderIP = requestData.getString("senderIP");

            if (senderIP.equals(node.neighborLeft.IP)) {
                node.neighborLeft.listOfData.add(data);
            } else if (senderIP.equals(node.neighborRight.IP)) {
                node.neighborRight.listOfData.add(data);
            }

            JSONObject responseBody=new JSONObject();
            responseBody.put("Neighbor","ADDEDSUCCESS");
            Response response =new Response("200", responseBody);

            return response;
        } else if(isParent){
            //Adds to own
            System.out.println("REQUEST TO PARENTS:");

            Data data = new Data(value,true);
            node.listOfData.add(data);

            // make new request with isParent=false
            Request requestForNeighbor = createRequestForNeighborReplication(data, request, node.IP);

            // data for internal representation of neighbors data
            Data dataForNeighbor = new Data(value, false);
            node.neighborLeft.listOfData.add(dataForNeighbor);
            Response r1 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
            System.out.println("PASSING ONTO FIRST CHILD" + requestForNeighbor.toString());
            Response r2 = sendRequestToNeighbor(node.neighborLeft.IP,requestForNeighbor);
            System.out.println("PASSING ONTO SECOND CHILD" + requestForNeighbor.toString());
            JSONObject j = new JSONObject();
            j.put("Data Replicated on neighbors", "True");
            if (r1.status=="200"&& r2.status=="200"){
                Response response =new Response("OK 200", j);
                return response;
                }

        }
        Response response =new Response("400",new JSONObject());
        return response;
    }

    public  Response sendRequestToNeighbor(String IP, Request request) throws IOException, JSONException {
        try {

        Socket connectionToNeighbor;
        connectionToNeighbor = new Socket(IP, 4444);
        DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
        outputStream.writeUTF(request.toString());
        connectionToNeighbor.close();

        DataInputStream instream = new DataInputStream(connectionToNeighbor.getInputStream());

        }catch (Exception e){
           Response response = new Response("400", new JSONObject());

        }
        String value=request.body.getJSONObject("Data").getString("Value");

        JSONObject responseObject=new JSONObject();
        responseObject.put("Added",value);
        Response response = new Response("200", responseObject);
        return response;
    }
    public  Request createRequestForNeighborReplication(Data data, Request originalRequest, String senderIP) throws JSONException {
        JSONObject jsonBody=new JSONObject();

        JSONObject jsonData = new JSONObject();

        jsonData.put("ID",data.id);
        jsonData.put("Value",data.value);
        jsonData.put("isParent",false);
        jsonData.put("senderIP", senderIP);

        jsonBody.put("Data",jsonData);

        Request newRequest = new Request("get", originalRequest.path, jsonBody);
        return newRequest;


    }
    public Response DeleteData(Response response) {

        return response;
    }

    public Response buildResponseBadRequest(Response response) {

        return response;
    }


    public Response getDataHandler(Request request, Response response) {

        System.out.println("Inside getDataHandler");

        Request originalRequest = new Request(request.method, request.path, request.body);

        if (response.status.contains("OK")) {
            System.out.println("Response status contains OK");
            return response;
        } else {
            System.out.println("Response didnt have the Data");
            boolean hasGottenData = false;
            PhoneBook copyPhonebook = node.phoneBookLeft.copy();

            System.out.println("THIS IS THE IPS:");
            System.out.println(copyPhonebook.IPs);

            while (!hasGottenData) {
                System.out.println("while we dont have data");
                String IP = copyPhonebook.IPs.get(0);
                System.out.println(" the IP is we ask for data now: " + IP);
                Socket connectionToServer;
                try {
                    connectionToServer = new Socket(IP, 4444);
                    DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                    DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());

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

                    System.out.println("Is the copyPhoneBook size == 1? ");
                    if (copyPhonebook.IPs.size() == 1) {
                        System.out.println("The size of copyPhoneBook is 1, we get a new phonebook");
                        request = new Request("get", "getPhoneBook", new JSONObject());
                        String newMessage = request.toString();
                        System.out.println("Sending this request: " + newMessage);
                        outstream.writeUTF(newMessage);
                        outstream.flush();

                        // TODO implement something to check if response is coming or not if not ask new person
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response: " + messageFromServer);
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
                    } else {
                        System.out.println("The copyPhonebook is not == 1");

                        outstream.writeUTF(originalRequest.toString());
                        outstream.flush();
                        System.out.println("This is the request we send: " + originalRequest.toString());
                        String messageFromServer = instream.readUTF();
                        System.out.println("This is the response we got: " + messageFromServer);
                        response = new Response(messageFromServer);

                        if (response.status.contains("OK")) {
                            hasGottenData = true;
                        }
                    } // else for copyPhoneBook == 1
                    connectionToServer.close();
                    copyPhonebook.IPs.remove(0);

                } catch (IOException e) {
                    System.out.println("problem is: " + e.toString());
                    throw new RuntimeException(e);
                }
            } // while we havent gotten the data
            return response;
        } // else for response.status contain ok)
    } // getDataHandler
} // ApiHandler
