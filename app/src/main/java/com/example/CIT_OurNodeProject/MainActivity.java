package com.example.CIT_OurNodeProject;


import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI-elements
    private Button startClientButton, submitIP, sendRequest;
    private TextView serverInfoTv, clientInfoTv;
    private EditText ipInputField;

    // Logging/status messages
    private String serverinfo = "SERVER LOG:";
    private String clientinfo = "CLIENT LOG: ";

    // Global data
    private final int PORT = 4444;
    private String THIS_IP_ADDRESS = "";
    private String REMOTE_IP_ADDRESS = "";
    private Thread serverThread = new Thread(new MyServerThread());
    private Thread clientThread = new Thread(new MyClientThread());

    // Some state
    private boolean ip_submitted = false;
    private boolean carryOn = true; //Now only used for client part
    boolean clientStarted = false;

    Node node;

    ApiHandler apiHandler;

    Request latestRequest;
    int clientNumber = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClientButton = findViewById(R.id.button);

        sendRequest = findViewById(R.id.sendRequest);


        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);
        submitIP = findViewById(R.id.sendclient);
        ipInputField = findViewById(R.id.clientmessagefield);

        //Setting click-listeners on buttons
        startClientButton.setOnClickListener(this);
        submitIP.setOnClickListener(this);
        sendRequest.setOnClickListener(this);

        //Setting some UI state
        ipInputField.setHint("Submit IP-address");
        startClientButton.setEnabled(false); //deactivates the button

        //Getting the IP address of the device
        THIS_IP_ADDRESS = getLocalIpAddress();
        sUpdate("This IP is " + THIS_IP_ADDRESS);

        node = new Node(THIS_IP_ADDRESS);

        Data data = new Data("123");

        node.listOfData.add(data);
        //  System.out.println(data.id);

//        node.phoneBookLeft.IPs.add("192.168.0.79");

//        node.phoneBookLeft.IPs.add("192.168.0.104");
//        node.phoneBookLeft.IPs.add("192.168.0.104");


        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
//


        node.phoneBookRight.IPs.add("192.168.0.104");
        node.phoneBookRight.IPs.add("192.168.0.104");
        node.phoneBookRight.IPs.add("192.168.0.104");

        latestRequest = new Request();

        apiHandler = new ApiHandler(node);
        apiHandler.activity = this;
        //Starting the server thread
        serverThread.start();
        serverinfo += "- - - SERVER STARTED - - -\n";

    }

    public void makeRequest(String ip, String requestString){
        System.out.println("--------------------------------------------------makeRequest----------------------------------------" );
//        latestRequest = requestString;
        REMOTE_IP_ADDRESS = ip;
        MyClientThread thread = new MyClientThread();
        thread.message = requestString.toString();
        Thread clientThread = new Thread(thread);
        clientThread.start();

        clientinfo += "- - - CLIENT STARTED - - - \n";
        sendRequest.setText("Resend req");
    }
    public void makeRequest(String ip, Request requestString){
        System.out.println("--------------------------------------------------makeRequest----------------------------------------" );
        latestRequest = requestString;
        REMOTE_IP_ADDRESS = ip;
        MyClientThread thread = new MyClientThread();
        JSONObject jsonReq = requestString.toJson();

        thread.message = jsonReq.toString();
        Thread clientThread = new Thread(thread);
        clientThread.start();

        clientinfo += "- - - CLIENT STARTED - - - \n";
        sendRequest.setText("Resend req");
    }

    @Override
    public void onClick(View view) {

        if (view == startClientButton) {
            if (!clientStarted) {
                clientStarted = true;
//                clientThread.start();
                clientinfo += "- - - CLIENT STARTED - - - \n";
                startClientButton.setText("Resend");

            } else{
                if(!ipInputField.getText().toString().equals(REMOTE_IP_ADDRESS)) {
                    // String newCommand = ipInputField.getText().toString();
                    // String[] newCommandList = newCommand.split(",");
                    // command = HandleApi.createHttpRequest(newCommandList[0], newCommandList[1], newCommandList[2]);

                }else{
                    // command = HandleApi.createHttpRequest("Get", "getId", "empty");
                    //  System.out.println(command);
                }
                Thread clientThread = new Thread(new MyClientThread());
                clientThread.start();
            }
        } else if (view == submitIP) {
            if (!ip_submitted) {
                ip_submitted = true;
                REMOTE_IP_ADDRESS = ipInputField.getText().toString();
                startClientButton.setEnabled(true);
                submitIP.setEnabled(false);
            }
        } else if (view == sendRequest) {
            System.out.println("----------------------------------------------------------------------SEND REQUEST----------------------------------------" );
//            Request reqstring = apiHandler.requestGetData("1234");
            String reqstring = apiHandler.getId();
//            String reqstring = apiHandler.requestAddData(new Data("1233"), true, false);
            makeRequest(REMOTE_IP_ADDRESS, reqstring);

            clientinfo += "- - - CLIENT STARTED - - - \n";
            sendRequest.setText("Resend req");
        }


    }//onclick

    class MyServerThread implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(4444);

                //Always be ready for next client
                while (true) {
                    sUpdate("SERVER: start listening..");
                    Socket clientSocket = serverSocket.accept();
                    sUpdate("SERVER connection accepted");
                    clientNumber++;
                    new RemoteClient(clientSocket, clientNumber).start();
//                    new RemoteClient(clientSocket, clientNumber).start();

                }//while listening for clients



            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }//run
    }//runnable

    class RemoteClient extends Thread {
        private final Socket client;
        private int number;

        public RemoteClient (Socket clientSocket, int number) {
            this.client = clientSocket;
            this.number = number;
        }

        public void run() {

            try {
                DataInputStream instream = new DataInputStream(client.getInputStream());
                DataOutputStream outstream = new DataOutputStream(client.getOutputStream());

                //Run conversation
                String str = (String) instream.readUTF();

                System.out.println("server str" + str);

                Request requestFromClient = apiHandler.readHttpRequest(str);

                System.out.println("Client " + number + " says: " + requestFromClient.toString() + "\n-----------------------------------------------------------");

                String answer = "";
//                answer = apiHandler.requestHandler(str);
                answer = apiHandler.requestHandler(requestFromClient);

//                apiHandler.checkResponse(str);
                sUpdate(answer + "\n-----------------------------------------------------------");

                // open new connection when needed. Like with addData requests


                System.out.println("Reply to client " + number + ": " + answer + "\n-----------------------------------------------------------");
                outstream.writeUTF(answer);
                outstream.flush();
                waitABit();


                //Closing everything down
                client.close();
//                sUpdate("SERVER: Remote client " + number + " socket closed");
//                instream.close();
//                sUpdate("SERVER: Remote client " + number + " inputstream closed");
//                outstream.close();
//                sUpdate("SERVER: Remote client  " + number + "outputstream closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void run2() {

            try {
                DataInputStream instream = new DataInputStream(client.getInputStream());
                DataOutputStream outstream = new DataOutputStream(client.getOutputStream());

                //Run conversation
                while (carryOn) {
                    String str = (String) instream.readUTF();

                    Request requestFromClient = apiHandler.readHttpRequest(str);

                    System.out.println("Client " + number + " says: " + requestFromClient.toString());
                    String answer = "";

                    answer = apiHandler.requestHandler(requestFromClient);
//                    answer = apiHandler.requestHandler(str);

                    // open new connection when needed. Like with addData requests


                    System.out.println("Reply to client " + number + ": " + answer);
                    outstream.writeUTF(answer);
                    outstream.flush();
                    waitABit();
                }


                //Closing everything down
                client.close();
                sUpdate("SERVER: Remote client " + number + " socket closed");
                instream.close();
                sUpdate("SERVER: Remote client " + number + " inputstream closed");
                outstream.close();
                sUpdate("SERVER: Remote client  " + number + "outputstream closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }






    // !!! Returns 0.0.0.0 on emulator
    //Modified from https://www.tutorialspoint.com/sending-and-receiving-data-with-sockets-in-android
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        String address = null;
        try {
            address = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

    class MyClientThread implements Runnable {
        String message;

        DataInputStream instream;

        DataOutputStream outstream;
        Socket connectionToServer;


        @Override
        public void run() {

            try {
                cUpdate("CLIENT: starting client socket ");
//                REMOTE_IP_ADDRESS = node.phoneBookLeft.IPs.get(0);
//                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);
//                cUpdate("CLIENT: client connected ");
//
//                DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
//                DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());

                JSONObject json = new JSONObject();
                JSONObject innerJson = new JSONObject();

                Request requestFromClient = apiHandler.readHttpRequest(message);

                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);
                instream = new DataInputStream(connectionToServer.getInputStream());
                outstream = new DataOutputStream(connectionToServer.getOutputStream());

                String messageFromServer = instream.readUTF();
                Response response = apiHandler.readHttpResponse(messageFromServer);

                if (apiHandler.checkResponse(messageFromServer)){

                }

                if (requestFromClient.method == "getdata"){


                    boolean hasGottenData = false;
                    PhoneBook copyPhonebook = node.phoneBookLeft.copy();
                    String newMessage;

                    while(!hasGottenData) {

                        String IP = copyPhonebook.IPs.get(0);
                        connectionToServer = new Socket(IP, 4444);
                        instream = new DataInputStream(connectionToServer.getInputStream());
                        outstream = new DataOutputStream(connectionToServer.getOutputStream());
                        messageFromServer = instream.readUTF();
                        response = apiHandler.readHttpResponse(messageFromServer);
                        apiHandler.checkResponse(messageFromServer);


                        if(copyPhonebook.IPs.size()==1){
                            newMessage=apiHandler.getPhonebook();
                            outstream.writeUTF(newMessage);
                            outstream.flush();
                            messageFromServer = instream.readUTF();
                            response = apiHandler.readHttpResponse(messageFromServer);
                            String stripped = null;
                            try {
                                stripped = response.body.getString("leftNeighbors").replaceAll("[\\[\\](){} ]","");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            };
                            // if ip == our ip, we need to stop, but first after finishing our phonebook

                            String[] splitList = stripped.split(",");
                            for (String ip:
                                    splitList) {
                                copyPhonebook.IPs.add(ip);
                            }
                            System.out.println("message from server" + copyPhonebook.IPs);

                        }
                        else {
                            outstream.writeUTF(message);
                            outstream.flush();
                            System.out.println();
                            messageFromServer = instream.readUTF();
                            System.out.println("Response from server: " + messageFromServer);

                            if (apiHandler.checkResponse(messageFromServer)) {
                                hasGottenData = true;
                                break;
                            }
                        }
                        }
                        connectionToServer.close();
                        copyPhonebook.IPs.remove(REMOTE_IP_ADDRESS);
                        System.out.println("Phonebook: " + copyPhonebook.IPs);

                }

//                String message = apiHandler.requestAddData(new Data("1233"), false, false);
//                String message = apiHandler.requestGetData("123");

//                String message = apiHandler.getData("123");

                // String message = apiHandler.createHttpRequestAsString("get", "getData", json.toString());

                cUpdate("Request message" + message);
//                String messageFromServer = instream.readUTF();
//                cUpdate("Server says: " + messageFromServer);
                waitABit();



//                cUpdate("CLIENT: closed inputstream");
//                out.close();
//                cUpdate("CLIENT: closed outputstream");
//                connectionToServer.close();
//                cUpdate("CLIENT: closed socket");

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }//run()


        private void gettingData(){

        }
        public void runConnection(){
            //                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);

            try {
                connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);
                instream = new DataInputStream(connectionToServer.getInputStream());
                outstream = new DataOutputStream(connectionToServer.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void endConnection(){

        }



        public void run2() {

            try {
                cUpdate("CLIENT: starting client socket ");
                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);
                cUpdate("CLIENT: client connected ");

                DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());

                while (carryOn) {

                    JSONObject json = new JSONObject();
                    JSONObject innerJson = new JSONObject();



                    try {
                        innerJson.put("Id", "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3" );
                        innerJson.put("Value", "");
                        innerJson.put("IsParent", "false" );
                        innerJson.put("IsGlobal", "false" );
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        json.put("Data", innerJson);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    String message = apiHandler.getData("123");

                    // String message = apiHandler.createHttpRequestAsString("get", "getData", json.toString());
                    out.writeUTF(message);
                    out.flush();
                    cUpdate("I said:      " + message);
                    String messageFromServer = instream.readUTF();
                    cUpdate("Server says: " + messageFromServer);
                    waitABit();
                }
                instream.close();
                cUpdate("CLIENT: closed inputstream");
                out.close();
                cUpdate("CLIENT: closed outputstream");
                connectionToServer.close();
                cUpdate("CLIENT: closed socket");

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }//run()
    } //class MyClientThread

    //Wait by setting the thread to sleep for 1,5 seconds
    private void waitABit() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //The below two methods are for updating UI-elements on the main thread


    private void sendRequest(){

        REMOTE_IP_ADDRESS = node.phoneBookLeft.IPs.get(0);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientinfo = "\n" + serverinfo;
//                serverInfoTv.setText(serverinfo);
            }
        });
    }


    //Server update TexView
    private void sUpdate(String message) {
        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverinfo = message + "\n\n" +  serverinfo;
                serverInfoTv.setText(serverinfo);
            }
        });

    }

    //Client update TextView
    private void cUpdate(String message) {
//        System.out.println(message);

        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientinfo = message + "\n\n" + clientinfo;
                clientInfoTv.setText(clientinfo);
            }
        });
    }
}