package com.example.CIT_OurNodeProject;

import android.provider.ContactsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ClientManager implements IClientManager{

    Node node;

    ServerManager serverManager;

    public ClientManager(Node node) {
        this.node = node;
    }

    public Response handleResponseFromServer(Request request, Response response) throws JSONException, IOException {

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
               // handleResponse_GetData(request, response);
                break;
            case "adddata":
                // Do nothing
                break;
            default:

                break;
        }
        return response;
    }

    @Override
    public Request generateRequest_UpdatePhoneBook(PhoneBook phoneBook, String side) {
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

    @Override
    public Request generateRequest_GetData(String value) {
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

    @Override
    public Request generateRequest_AddData(String value, Boolean isParent) {
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
           innerJson.put("senderIP", node.IP);
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

    public Request generateRequest_DeleteData(String value, boolean isParent) throws JSONException {

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();

        String id = null;
        try {
            id = SHA256.toHexString(SHA256.getSHA(value));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        innerJson.put("Id", id);
        innerJson.put("isParent",isParent);

        jsonBody.put("Data", innerJson);

        Request newRequest = new Request("get", "deleteData", jsonBody);
        return newRequest;
    }

    public Request generateRequest_DeleteData_For_Child_Data(Request request, boolean isParent) throws JSONException {

        JSONObject RequestData = null;

        String id = null;

        try {
            RequestData = request.body.getJSONObject("Data");
            id = RequestData.getString("Id");

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();


        innerJson.put("Id", id);
        innerJson.put("isParent",isParent);

        jsonBody.put("Data", innerJson);

        Request newRequest = new Request("get", "deleteData", jsonBody);
        return newRequest;
    }


    @Override
    public String DeleteData(Request request) {
        Response response = new Response();
        JSONObject requestData = null;
        try {
            requestData = request.body.getJSONObject("Data");
            String id = requestData.getString("Id");

        boolean hasData = node.checkForData(id);

        if (!hasData) {

            JSONObject jsonBody = new JSONObject();
            JSONObject innerJson = new JSONObject();

            innerJson.put("Id", id);
            innerJson.put("Value", "emptyValue");
            innerJson.put("isParent", true);

            jsonBody.put("Data", innerJson);

            Request getDataRequest = new Request("get", "getData", jsonBody);

            response = getDataHandler(getDataRequest);

            if (response.status.contains("OK")) {
                String IP = response.body.getString("IP");

                Request requestToDelete = generateRequest_DeleteData(requestData.getString("Value"), true);

                sendRequestToNeighbor(IP, requestToDelete);
            }

            return response.toString();
        }

        //delete own data if we have it
        if (requestData.getBoolean("isParent")) {
            // if parent ask us to delete, we delete if we can. Will always give 200 even if we dont hold data
            boolean isDeleted = node.deleteDataLocally(id);

            // IF we are parent of the data, ask children to delete the data
            // create request for children with isParent=True
            Request requestForChild = generateRequest_DeleteData_For_Child_Data(request, false);
            // send requests to children
            System.out.println("sending request to neighborLeft:" + requestForChild.toString());
            Response responseLeft = sendRequestToNeighbor(node.neighborLeft.IP, requestForChild);
            System.out.println("sending request to neighborRight:" + requestForChild.toString());
            Response responseRight = sendRequestToNeighbor(node.neighborRight.IP, requestForChild);

            if (responseLeft.status.contains("OK") && responseRight.status.contains("OK")) {
                response = new Response("200 OK", new JSONObject());
            } else {
                response = new Response("400", new JSONObject());
            }

            if (isDeleted) {
                response = new Response("200 - deleted", new JSONObject());
            } else {
                response = new Response("404 Not Found - I dont have it", new JSONObject());
            }
        } else {
            node.neighborRight.deleteDataLocally(id);
            node.neighborLeft.deleteDataLocally(id);

        }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }

    public Response handleResponse_GetData(Request request, Response response) {

        System.out.println("Inside getDataHandler");

        Request originalRequest = request.copy();

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
                        Request phonebookRequest = new Request("get", "getPhoneBook", new JSONObject());
                        // request = new Request("get", "getPhoneBook", new JSONObject());
//                        String newMessage = request.toString();
                        System.out.println("Sending this request: " + phonebookRequest.toString());
                        outstream.writeUTF(phonebookRequest.toString());
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

                            JSONObject jsonBody = new JSONObject();

                            try {
                                jsonBody.put("IP", node.IP);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            response.body = jsonBody;
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


    public Response getDataHandler(Request request) {

        Response response = null;
        String id;

        try {
            JSONObject jsonBody = request.body.getJSONObject("Data");
            id = jsonBody.getString("Id");

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Inside getDataHandler");

        Request originalRequest = new Request(request.method, request.path, request.body);

      //  boolean iHaveTheData = node.checkForData(id);

        if (node.checkForData(id)) {
            System.out.println("Response status contains OK");

            JSONObject jsonBody = new JSONObject();
            JSONObject innerJson = new JSONObject();

            String value = "";

            for (Data data : node.listOfData) {
                if (data.id.equals(id)) {
                    value = data.value;
                }
            }

            try {
                innerJson.put("Id", id);
                innerJson.put("Value", value);
                innerJson.put("IP", node.IP);

                jsonBody.put("Data", innerJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            response = new Response("200 OK", jsonBody);

            return response;
        } else {
            System.out.println("I didnt have the Data");
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

                        if (response.status.contains("200 OK")) {
                            hasGottenData = true;

                            JSONObject jsonBody = new JSONObject();

                            try {
                                jsonBody.put("IP", node.IP);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            response.body = jsonBody;
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

    public  Response sendRequestToNeighbor(String IP, Request request) throws IOException, JSONException {
        Response response;
        try {

            Socket connectionToNeighbor;
            connectionToNeighbor = new Socket(IP, 4444);
            DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
            outputStream.writeUTF(request.toString());

            // TODO: if we have time actaully get the response instead of making a "fake" one

            connectionToNeighbor.close();

            // DataInputStream instream = new DataInputStream(connectionToNeighbor.getInputStream());
            String value=request.body.getJSONObject("Data").getString("Value");
            JSONObject responseObject=new JSONObject();
            responseObject.put("Added",value);
            response = new Response("200 OK", responseObject);

        }catch (Exception e){
            response = new Response("400", new JSONObject());

        }

        return response;
    }

    public Request joinNetwork(String inviterIP) {
//        Socket connectionToServer;

        // Get the phonebook of the inviter
        Response phonebookResponse_Inviter = sendOutRequest_getPhonebook(inviterIP);

        // saving ips from invter's left phonebook
        PhoneBook invitersLeftPhonebook = phonebookResponse_Inviter.retrievePhonebook("left");
        String invitersLeftNeighborIP = invitersLeftPhonebook.IPs.get(0); // "Other
        String inviters2ndLeftNeighborIP = invitersLeftPhonebook.IPs.get(1);

        // saving ips from invter's right phonebook
        PhoneBook invitersRightPhonebook = phonebookResponse_Inviter.retrievePhonebook("right");
        String invitersRightNeighborIP =  invitersRightPhonebook.IPs.get(0);
        String inviters2ndRightNeighborIP =  invitersRightPhonebook.IPs.get(1);


        node.phoneBookLeft  = invitersLeftPhonebook.copy();
        node.phoneBookRight = new PhoneBook(inviterIP,invitersRightNeighborIP, inviters2ndRightNeighborIP );


        PhoneBook newInviterPhonebook = new PhoneBook(node.IP, invitersLeftNeighborIP, inviters2ndLeftNeighborIP);
        Response updatePhonebookResponse = sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebook, "left");

        PhoneBook newInvitersRightNeighborPhonebook = new PhoneBook(inviterIP, node.IP, invitersLeftNeighborIP);
        Response updatePhonebookResponseRight = sendOutRequest_updatePhonebook(invitersRightNeighborIP, newInvitersRightNeighborPhonebook, "left");

        PhoneBook newInviters2ndRightNeighborPhonebook = new PhoneBook(invitersLeftNeighborIP, inviterIP, node.IP);
        Response updatePhonebookResponseRightRight = sendOutRequest_updatePhonebook(inviters2ndRightNeighborIP, newInviters2ndRightNeighborPhonebook, "left");

        String otherIP = invitersLeftNeighborIP;
        Response phonebookResponse_Other = sendOutRequest_getPhonebook(otherIP);

        // saving ips from other's left phonebook
        PhoneBook othersLeftPhonebook = phonebookResponse_Inviter.retrievePhonebook("left");
        String othersLeftNeighborIP = invitersLeftPhonebook.IPs.get(0); // "Other
        String others2ndLeftNeighborIP = invitersLeftPhonebook.IPs.get(1);

        // saving ips from other's right phonebook
        PhoneBook othersRightPhonebook = phonebookResponse_Inviter.retrievePhonebook("right");
        String othersRightNeighborIP =  invitersRightPhonebook.IPs.get(0);
        String others2ndRightNeighborIP =  invitersRightPhonebook.IPs.get(1);

        // Handling phonebooks to the left
        PhoneBook newOtherPhonebook = new PhoneBook(node.IP, inviterIP, invitersRightNeighborIP);
        Response updatePhonebookResponseOther = sendOutRequest_updatePhonebook(otherIP, newOtherPhonebook, "right");

        PhoneBook newOthersLeftNeighborPhonebook = new PhoneBook(otherIP, node.IP, inviterIP);
        Response updatePhonebookResponseLeft = sendOutRequest_updatePhonebook(othersRightNeighborIP, newOthersLeftNeighborPhonebook, "right");

        PhoneBook newOthers2ndLeftNeighborPhonebook = new PhoneBook(othersRightNeighborIP, otherIP, node.IP);
        Response updatePhonebookResponseLeftLeft = sendOutRequest_updatePhonebook(others2ndRightNeighborIP, newOthers2ndLeftNeighborPhonebook, "right");

        // ADD DATA
        sendOutRequest_addData(inviterIP, node.listOfData, false );
        sendOutRequest_addData(otherIP, node.listOfData, false );



        return new Request();
        // TODO: send add data to both inviter and other

    }

    private Response sendOutRequest_getPhonebook(String targetIP){
        Socket connectionToServer = null;
        Response response;
        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response = new DataInputStream(connectionToServer.getInputStream());
            Request request = new Request("get", "getPhonebook", new JSONObject());
            outstream_request.writeUTF(request.toString()); //RIGHT?
            outstream_request.flush();

            String responseFromServer = instream_response.readUTF();
            response = new Response(responseFromServer);
        outstream_request.close();
        instream_response.close();
        connectionToServer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private Response sendOutRequest_updatePhonebook(String targetIP, PhoneBook newPhonebook, String side){
        Socket connectionToServer = null;
        Response response;
        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response = new DataInputStream(connectionToServer.getInputStream());
            Request request = generateRequest_UpdatePhoneBook(newPhonebook, side);

//
//            Request request = new Request("get", "updatePhonebook", new JSONObject(newPhonebook);
            outstream_request.writeUTF(request.toString()); //RIGHT?
            outstream_request.flush();

            String responseFromServer = instream_response.readUTF();
            response = new Response(responseFromServer);
            outstream_request.close();
            instream_response.close();
        connectionToServer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private Response sendOutRequest_addData(String targetIP, ArrayList<Data> dataList, boolean isParent){
        Response response = new Response();
        Socket connectionToServer = null;

        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response  = new DataInputStream(connectionToServer.getInputStream());
            for (Data data: dataList) {
                Request request = generateRequest_AddData(data.value, isParent);

                System.out.println("Request \n " + request.body);
                outstream_request.writeUTF(request.toString()); //RIGHT?
                outstream_request.flush();
                System.out.println("Instream --- \n" + instream_response.read());
                String responseFromServer = instream_response.readUTF();
                response = new Response(responseFromServer);
                System.out.println("something");
            }
            outstream_request.close();
            instream_response.close();
            connectionToServer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;

    }

}
