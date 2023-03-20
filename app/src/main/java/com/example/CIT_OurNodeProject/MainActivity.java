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

import org.json.JSONArray;
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
import java.sql.SQLOutput;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI-elements
    private Button startClientButton, submitIP;
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


    int clientNumber = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClientButton = findViewById(R.id.button);
        serverInfoTv = findViewById(R.id.serveroutput);
        clientInfoTv = findViewById(R.id.clientoutput);
        submitIP = findViewById(R.id.sendclient);
        ipInputField = findViewById(R.id.clientmessagefield);

        //Setting click-listeners on buttons
        startClientButton.setOnClickListener(this);
        submitIP.setOnClickListener(this);

        //Setting some UI state
        ipInputField.setHint("Submit IP-address");
        startClientButton.setEnabled(false); //deactivates the button

        //Getting the IP address of the device
        THIS_IP_ADDRESS = getLocalIpAddress();
        sUpdate("This IP is " + THIS_IP_ADDRESS);

        node = new Node(THIS_IP_ADDRESS);

        Data data = new Data("123");
        Data data1 = new Data("1234");


        node.listOfData.add(data);
        node.listOfData.add(data1);
        //  System.out.println(data.id);

        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);

        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);



        apiHandler = new ApiHandler(node);
        //Starting the server thread
        serverThread.start();
        serverinfo += "- - - SERVER STARTED - - -\n";

    }

    @Override
    public void onClick(View view) {

        if (view == startClientButton) {
            if (!clientStarted) {
                clientStarted = true;
                clientThread.start();
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
//                while (carryOn) {
                    String requestString = (String) instream.readUTF();

                    System.out.println("Recived requestString:");
                    System.out.println(requestString);

                    Request requestFromClient = new Request(requestString);

                    sUpdate("Client " + number + " says: " + requestFromClient.toString());

                    String answer = "";
                    Response response = apiHandler.requestHandler(requestFromClient, REMOTE_IP_ADDRESS);

                    answer = response.toString();

                    sUpdate("Reply to client " + number + ": " + answer);
                    outstream.writeUTF(answer);
                    outstream.flush();
//                    waitABit();
//                }
                //Closing everything down
                sUpdate("SERVER: Remote client " + number + " socket closed");
                sUpdate("SERVER: Remote client " + number + " inputstream closed");
                instream.close();
                outstream.close();
                client.close();
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
        @Override
        public void run() {

            try {
                cUpdate("CLIENT: starting client socket ");
                cUpdate("CLIENT: client connected ");
                Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);


                DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());

//                while (carryOn) {



                    // building json body for the test request:
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("Id", "3" );
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Request request = new Request("get", "getData", jsonBody);

                    System.out.println(request.header);

                    String message = request.toString();

                    out.writeUTF(message);
                    cUpdate("I said:      " + message);
                    String messageFromServer = instream.readUTF();
                System.out.println("I said:      " + messageFromServer);

                    Response response = new Response(messageFromServer);
                    instream.close();
                    out.close();
                    connectionToServer.close();
/*

* */

                apiHandler.respondHandler(request, response);

                    cUpdate("Server says: " + messageFromServer);
//                    waitABit();
//                }
                cUpdate("CLIENT: closed inputstream");
                cUpdate("CLIENT: closed outputstream");
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

    //Server update TexView
    private void sUpdate(String message) {
        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serverinfo = message + "\n" + serverinfo;
                serverInfoTv.setText(serverinfo);
            }
        });

    }

    //Client update TextView
    private void cUpdate(String message) {
        System.out.println(message);

        //Run this code on UI-thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                clientinfo = message + "\n" + clientinfo;
                clientInfoTv.setText(clientinfo);
            }
        });
    }
}