package com.example.CIT_OurNodeProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ClientManager implements IClientManager {

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

    public Request generateRequest_GetId() {
        return new Request("get", "getId");
    }

    public Request generateRequest_GetPhonebook() {
        return new Request("get", "getPhonebook");
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

        Request request = new Request("get", "updatephonebook", jsonBody);

        return request;
    }

    public Request generateRequest_FixNeighbor(String side) {
//        String jsonArrayStringOfPhoneBook = phoneBook.toJsonArrayString();

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();
        try {
            innerJson.put("Side", side);
//            innerJson.put("PhoneBook", jsonArrayStringOfPhoneBook);
            jsonBody.put("Data", innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new Request("get", "fixNeighbor", jsonBody);
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
            innerJson.put("Id", hashedValue);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            json.put("Data", innerJson);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new Request("get", "getData", json);
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
            innerJson.put("Id", hashedValue);
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

    public Request generateRequest_DeleteData(String value, boolean isParent) {

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();

        String id = null;
        try {
            id = SHA256.toHexString(SHA256.getSHA(value));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        try {
            innerJson.put("Id", id);
            innerJson.put("isParent", isParent);

            jsonBody.put("Data", innerJson);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return new Request("get", "deleteData", jsonBody);
    }

    public Request generateRequest_DeleteData_For_Child_Data(Request request, boolean isParent) throws JSONException {

        JSONObject RequestData;

        String id;

        try {
            RequestData = request.body.getJSONObject("Data");
            id = RequestData.getString("Id");

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();


        innerJson.put("Id", id);
        innerJson.put("isParent", isParent);

        jsonBody.put("Data", innerJson);

        return new Request("get", "deleteData", jsonBody);
    }


    @Override
    public String DeleteData(Request request) {
        Response response = new Response();
        JSONObject requestData;
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
                node.deleteDataLocally(id);

                // IF we are parent of the data, ask children to delete the data
                // create request for children with isParent=True
                Request requestForChild = generateRequest_DeleteData_For_Child_Data(request, false);
                // send requests to children
                System.out.println("sending request to neighborLeft:" + requestForChild);
                Response responseLeft = sendRequestToNeighbor(node.neighborLeft.IP, requestForChild);
                System.out.println("sending request to neighborRight:" + requestForChild);
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
        } catch (RuntimeException | JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        return response.toString();
    }


    public PhoneBook getPhonebookAndUpdateCopy(PhoneBook copyPhonebook, String IP) throws IOException {
        Socket connectionToServer = new Socket(IP, 4444);
        DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
        DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());

        Request request = new Request("get", "getPhoneBook", new JSONObject());
        String newMessage = request.toString();

        System.out.println("Sending this request: " + newMessage);
        outstream.writeUTF(newMessage);
        outstream.flush();

        // TODO implement something to check if response is coming or not if not ask new person
        String messageFromServer = instream.readUTF();
        System.out.println("This is the response: " + messageFromServer);
        Response response = new Response(messageFromServer);
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
        return copyPhonebook;
    }

    public Response passOnRequest(String IP, Request originalRequest) throws IOException {
        System.out.println("The copyPhonebook is not == 1");

        Socket connectionToServer = new Socket(IP, 4444);
        DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
        DataOutputStream outstream = new DataOutputStream(connectionToServer.getOutputStream());
        outstream.writeUTF(originalRequest.toString());
        outstream.flush();
        System.out.println("This is the request we send: " + originalRequest);
        String messageFromServer = instream.readUTF();
        System.out.println("This is the response we got: " + messageFromServer);
        Response response = new Response(messageFromServer);
        instream.close();
        outstream.close();
        connectionToServer.close();
        return response;

    }

    public Response createResponseForFoundData(String id) {
        JSONObject jsonBody = new JSONObject();
        JSONObject innerJson = new JSONObject();
        String value = node.getData(id);
        try {
            innerJson.put("Id", id);
            innerJson.put("Value", value);
            innerJson.put("IP", node.IP);

            jsonBody.put("Data", innerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new Response("200 OK", jsonBody);
    }


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

        if (node.checkForData(id)) {
            return createResponseForFoundData(id);

        } else {
            PhoneBook copyPhonebook = node.phoneBookLeft.copy();
            System.out.println("THIS IS THE IPS:");
            System.out.println(copyPhonebook.IPs);

            while (true) {
                System.out.println("while we dont have data");
                String IP = copyPhonebook.IPs.get(0);
                if (node.IP.equals(copyPhonebook.IPs.get(0))) {
                    response = new Response();
                    response.status = "404 Not Found";
                    response.body = new JSONObject();
                    break;
                }
                System.out.println(" the IP is we ask for data now: " + IP);
                if (copyPhonebook.IPs.size() == 1) {
                    try {
                        copyPhonebook = getPhonebookAndUpdateCopy(copyPhonebook, IP);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {

                    try {
                        response = passOnRequest(IP, originalRequest);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    copyPhonebook.IPs.remove(0);
                    if (response.status.contains("200 OK")) {
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("IP", node.IP);
                        } catch (JSONException a) {
                            throw new RuntimeException(a);
                        }
                        response.body = jsonBody;
                        return response;
                    }

                }
            }
        }
        return response;
    }


    public Response sendRequestToNeighbor(String IP, Request request) throws IOException, JSONException {
        Response response;
        try {

            Socket connectionToNeighbor;
            connectionToNeighbor = new Socket(IP, 4444);
            DataOutputStream outputStream = new DataOutputStream(connectionToNeighbor.getOutputStream());
            outputStream.writeUTF(request.toString());

            // TODO: if we have time actaully get the response instead of making a "fake" one

            connectionToNeighbor.close();

            // DataInputStream instream = new DataInputStream(connectionToNeighbor.getInputStream());
            String value = request.body.getJSONObject("Data").getString("Value");
            JSONObject responseObject = new JSONObject();
            responseObject.put("Added", value);
            response = new Response("200 OK", responseObject);
            outputStream.close();


        } catch (Exception e) {
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

        PhoneBook newInviterPhonebook = new PhoneBook(node.IP, invitersLeftNeighborIP, inviters2ndLeftNeighborIP);
        PhoneBook invitersRightPhonebook = phonebookResponse_Inviter.retrievePhonebook("right");

        String invitersRightNeighborIP = invitersRightPhonebook.IPs.get(0);
        String inviters2ndRightNeighborIP = invitersRightPhonebook.IPs.get(1);
        PhoneBook newInviterPhonebookRight = new PhoneBook(invitersRightNeighborIP, inviters2ndRightNeighborIP, node.IP);

        String otherIP = invitersLeftNeighborIP;

        node.phoneBookLeft = invitersLeftPhonebook.copy();
        node.phoneBookRight = new PhoneBook(inviterIP, invitersRightNeighborIP, inviters2ndRightNeighborIP);

        System.out.println("invitersLeftPhonebook.IPs.get(0).equals(inviterIP): " + invitersLeftPhonebook.IPs.get(0).equals(inviterIP));
        System.out.println("inviterIP: " + inviterIP);

        if (invitersLeftPhonebook.IPs.get(0).equals(inviterIP)) {
            System.out.println("HAPPENED");
            newInviterPhonebook = new PhoneBook(node.IP, inviterIP, node.IP);
            newInviterPhonebookRight = new PhoneBook(node.IP, inviterIP, node.IP);
            Response updatePhonebookResponse = sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebook, "left");
            sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebookRight, "right");
            node.phoneBookRight = new PhoneBook(inviterIP, node.IP, inviterIP);
            node.neighborLeft.IP = inviterIP;
            node.neighborRight.IP = inviterIP;
            node.phoneBookLeft = new PhoneBook(inviterIP, node.IP, inviterIP);
//            node.phoneBookLeft  = invitersLeftPhonebook.copy();

        } else if (invitersLeftPhonebook.IPs.get(1).equals(inviterIP)) {
            newInviterPhonebook = new PhoneBook(invitersLeftPhonebook.IPs.get(0), node.IP, inviterIP);
            newInviterPhonebookRight = new PhoneBook(node.IP, invitersLeftPhonebook.IPs.get(0), inviterIP);
            sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebookRight, "right");
//            node.phoneBookRight = new PhoneBook(inviterIP,node.IP, inviterIP );
            node.phoneBookLeft = new PhoneBook(invitersLeftPhonebook.IPs.get(0), inviterIP, node.IP);
            node.phoneBookRight = new PhoneBook(inviterIP, invitersRightPhonebook.IPs.get(0), node.IP);
            node.neighborLeft.IP = invitersLeftPhonebook.IPs.get(0);
            node.neighborRight.IP = inviterIP;


            Response updatePhonebookResponse = sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebook, "left");

            PhoneBook newInvitersRightNeighborPhonebook = new PhoneBook(inviterIP, node.IP, invitersLeftNeighborIP);
            Response updatePhonebookResponseRight = sendOutRequest_updatePhonebook(invitersRightNeighborIP, newInvitersRightNeighborPhonebook, "left");

            PhoneBook othersLeftPhonebook = phonebookResponse_Inviter.retrievePhonebook("left");
            String othersLeftNeighborIP = invitersLeftPhonebook.IPs.get(0); // "Other
            PhoneBook newOtherPhonebook = new PhoneBook(node.IP, inviterIP, invitersRightNeighborIP);
            Response updatePhonebookResponseOther = sendOutRequest_updatePhonebook(otherIP, newOtherPhonebook, "right");

            PhoneBook newOthersLeftNeighborPhonebook = new PhoneBook(otherIP, node.IP, inviterIP);
            String othersRightNeighborIP = invitersRightPhonebook.IPs.get(0);
            Response updatePhonebookResponseLeft = sendOutRequest_updatePhonebook(othersRightNeighborIP, newOthersLeftNeighborPhonebook, "right");
        } else {

            sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebookRight, "right");
            node.neighborLeft.IP = node.phoneBookLeft.IPs.get(0);
            node.neighborRight.IP = node.phoneBookRight.IPs.get(0);
            System.out.println("INVITER PHONEBOOK: " + newInviterPhonebook);
            sendOutRequest_updatePhonebook(inviterIP, newInviterPhonebook, "left");


            PhoneBook newInvitersRightNeighborPhonebook = new PhoneBook(inviterIP, node.IP, invitersLeftNeighborIP);
            sendOutRequest_updatePhonebook(invitersRightNeighborIP, newInvitersRightNeighborPhonebook, "left");


            PhoneBook newInviters2ndRightNeighborPhonebook = new PhoneBook(invitersLeftNeighborIP, inviterIP, node.IP);
            sendOutRequest_updatePhonebook(inviters2ndRightNeighborIP, newInviters2ndRightNeighborPhonebook, "left");


            Response phonebookResponse_Other = sendOutRequest_getPhonebook(otherIP);


            // saving ips from other's left phonebook
            PhoneBook othersLeftPhonebook = phonebookResponse_Other.retrievePhonebook("left");
            String othersLeftNeighborIP = invitersLeftPhonebook.IPs.get(0); // "Other
            String others2ndLeftNeighborIP = invitersLeftPhonebook.IPs.get(1);

            // saving ips from other's right phonebook
            PhoneBook othersRightPhonebook = phonebookResponse_Other.retrievePhonebook("right");
            String othersRightNeighborIP = invitersRightPhonebook.IPs.get(0);
            String others2ndRightNeighborIP = invitersRightPhonebook.IPs.get(1);


            // Handling phonebooks to the left
            PhoneBook newOtherPhonebook = new PhoneBook(node.IP, inviterIP, invitersRightNeighborIP);
            System.out.println(newOtherPhonebook.toString());
            System.out.println("OtherIP: " + otherIP + "          ----  " + node.IP);
            Response updatePhonebookResponseOther = sendOutRequest_updatePhonebook(otherIP, newOtherPhonebook, "right");

            PhoneBook newOthersLeftNeighborPhonebook = new PhoneBook(otherIP, node.IP, inviterIP);
            Response updatePhonebookResponseLeft = sendOutRequest_updatePhonebook(othersRightNeighborIP, newOthersLeftNeighborPhonebook, "right");

            PhoneBook newOthers2ndLeftNeighborPhonebook = new PhoneBook(othersRightNeighborIP, otherIP, node.IP);
            Response updatePhonebookResponseLeftLeft = sendOutRequest_updatePhonebook(others2ndRightNeighborIP, newOthers2ndLeftNeighborPhonebook, "right");

        }
        // ADD DATA
        sendOutRequest_addData(inviterIP, node.listOfData, false);
        sendOutRequest_addData(otherIP, node.listOfData, false);


        return new Request();
        // TODO: send add data to both inviter and other

    }

    public Response sendOutRequest_getPhonebook(String targetIP) {
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

    public Response sendOutRequest_getData(String targetIP) {
        Socket connectionToServer = null;
        Response response;
        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response = new DataInputStream(connectionToServer.getInputStream());
//            Request request = new Request("get", "getPhonebook", new JSONObject());
            Request request = new Request("get", "getData");
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


    private Response sendOutRequest_updatePhonebook(String targetIP, PhoneBook newPhonebook, String side) {
        Socket connectionToServer = null;
        Response response;
        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response = new DataInputStream(connectionToServer.getInputStream());
            Request request = generateRequest_UpdatePhoneBook(newPhonebook, side);
//            Request request = generateRequest_AddData("33322", false);

//
//            Request request = new Request("get", "updatePhonebook", new JSONObject(newPhonebook);
            System.out.println("IN SEND UPDATE PHONEBOOK REQUEST:  " + newPhonebook.toString());
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


    public void sendOutRequest_addData(String targetIP, ArrayList<Data> dataList, boolean isParent) {
        Socket connectionToServer = null;
        Response response;
        try {
            connectionToServer = new Socket(targetIP, 4444);
            DataOutputStream outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
            DataInputStream instream_response = new DataInputStream(connectionToServer.getInputStream());
//            Request request = generateRequest_UpdatePhoneBook(newPhonebook, side);
            for (Data data : dataList) {
                connectionToServer = new Socket(targetIP, 4444);
                outstream_request = new DataOutputStream(connectionToServer.getOutputStream());
                instream_response = new DataInputStream(connectionToServer.getInputStream());

//                Request request = generateRequest_AddData(data.value, false);
                Request request = generateRequest_AddData(data.value, false);

                //
                //            Request request = new Request("get", "updatePhonebook", new JSONObject(newPhonebook);
                outstream_request.writeUTF(request.toString()); //RIGHT?
                outstream_request.flush();

                String responseFromServer = instream_response.readUTF();
                System.out.println("HELLO");
                response = new Response(responseFromServer);
            }
            outstream_request.close();
            instream_response.close();
            connectionToServer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        return response;
    }

    public void findDeadNodeNeighbor(String deadIP) {
//        phonebook = requestGetPhonebook(leftNeighbor):
        PhoneBook phonebook = sendOutRequest_getPhonebook(node.neighborLeft.IP).retrievePhonebook("left");
        String requestedNodeIP = phonebook.IPs.get(0);
        while (true) {
            phonebook = sendOutRequest_getPhonebook(requestedNodeIP).retrievePhonebook("left");
            if (phonebook.IPs.get(0) == deadIP) break;
            requestedNodeIP = phonebook.IPs.get(0);
        }
        String targetIP = requestedNodeIP;

//
//        requstedsLeftNeighbor  =  phonebook.left.get(0);
//        while requstedsLeftNeighbor.ip != B.ip:
//        phonebook = requestGetPhonebook(requstedsLeftNeighbor):
//        requstedsLeftNeighbor  =  phonebook.left.get(0);
//        H = deadsRightNeighbor = requstedsLeftNeighbor:
//        requestedsLeft.sendRequest_fixDeadNeighbor(<side>, H.ip);


    }


    //    public void handleDeadNeighbor(String deadIP){
    public void handleDeadNeighbor(String side) {

        // dead nodes other neighbor
        String newNeighborIp = node.phoneBookLeft.IPs.get(1);
        Response newNeighborIp_phonebook = sendOutRequest_getPhonebook(newNeighborIp);

        // dead nodes other neighbor's phonebook and neighbors
        PhoneBook newNeighborLeftPhonebook = newNeighborIp_phonebook.retrievePhonebook("left");
        String newNeighborNeighborIP = newNeighborLeftPhonebook.IPs.get(0); // "Other
        String newNeighbor2ndLeftNeighborIP = newNeighborLeftPhonebook.IPs.get(1);

        // Updating own phonebook and neighbor
        node.phoneBookLeft = new PhoneBook(node.phoneBookLeft.IPs.get(1), node.phoneBookLeft.IPs.get(2), newNeighbor2ndLeftNeighborIP);
        node.addData(node.neighborLeft.listOfData);
        node.neighborLeft.removeAllData();
        node.neighborLeft.IP = newNeighborIp;
        PhoneBook newNeighborNewPhonebook = new PhoneBook(node.IP, node.phoneBookRight.IPs.get(0), node.phoneBookRight.IPs.get(1));
        sendOutRequest_updatePhonebook(newNeighborIp, newNeighborNewPhonebook, "right");

        sendOutRequest_updatePhonebook(newNeighborNeighborIP,
                new PhoneBook(newNeighborIp, node.IP, node.neighborRight.IP),
                "right");

        sendOutRequest_updatePhonebook(newNeighbor2ndLeftNeighborIP,
                new PhoneBook(newNeighborNeighborIP, newNeighborIp, node.IP),
                "right");

        sendOutRequest_updatePhonebook(node.neighborRight.IP,
                new PhoneBook(node.IP, newNeighborIp, newNeighborNeighborIP),
                "left");

        sendOutRequest_updatePhonebook(node.phoneBookRight.IPs.get(1),
                new PhoneBook(newNeighborIp, node.IP, newNeighborIp),
                "left");

        System.out.println("Node handling dead neighbor" + node.IP);



        /*
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
        * */

    }


}
