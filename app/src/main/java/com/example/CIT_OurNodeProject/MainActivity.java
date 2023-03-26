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
    private Button startClientButton, submitIP;
    private Button getIdButton, getPhonebookButton, updatePhonebookButton;
    private Button getDataButton, addDataButton, deleteDataButton;
    private Button showDataButton, showPhonebookButton;

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

    //    ApiHandler apiHandler;
    ServerManager serverManager;
    ClientManager clientManager;

    int clientNumber = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI boilerplate
        startClientButton = findViewById(R.id.startClient);
        serverInfoTv      = findViewById(R.id.serveroutput);
        clientInfoTv      = findViewById(R.id.clientoutput);
        submitIP          = findViewById(R.id.sendclient);
        ipInputField      = findViewById(R.id.clientmessagefield);


        getIdButton = findViewById(R.id.GetID);
        getPhonebookButton = findViewById(R.id.GetPhonebook);
        updatePhonebookButton = findViewById(R.id.UpdatePhonebook);
        getDataButton = findViewById(R.id.getData);
        addDataButton = findViewById(R.id.addData);
        deleteDataButton = findViewById(R.id.deleteData);

        showDataButton = findViewById(R.id.showData);
        showPhonebookButton = findViewById(R.id.showPhonebook);


        //Setting click-listeners on buttons
        startClientButton.setOnClickListener(this);
        submitIP.setOnClickListener(this);
        getIdButton.setOnClickListener(this);
        getPhonebookButton.setOnClickListener(this);
        updatePhonebookButton.setOnClickListener(this);
        getDataButton.setOnClickListener(this);
        addDataButton.setOnClickListener(this);
        deleteDataButton.setOnClickListener(this);
        getDataButton.setOnClickListener(this);
        showDataButton.setOnClickListener(this);
        showPhonebookButton.setOnClickListener(this);

        //Setting some UI state
        ipInputField.setHint("Submit IP-address");
        startClientButton.setEnabled(false); //deactivates the button

        //Getting the IP address of the device
        THIS_IP_ADDRESS = getLocalIpAddress();
        sUpdate("This IP is " + THIS_IP_ADDRESS);
        System.out.println("This IP is " + THIS_IP_ADDRESS);

        node = new Node(THIS_IP_ADDRESS);
        String[] dataToAddList = THIS_IP_ADDRESS.split("\\.");
        Data data = new Data("data" + dataToAddList[dataToAddList.length-1], true);
        node.listOfData.add(data);
        node.isInNetwork = false;
//        Data data1 = new Data("1234",true);


//        node.listOfData.add(data1);
        //  System.out.println(data.id);

        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookLeft.IPs.add(THIS_IP_ADDRESS);

        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);
        node.phoneBookRight.IPs.add(THIS_IP_ADDRESS);

//        node.neighborLeft.listOfData.add(data);
//        node.neighborLeft.listOfData.add(data1);


        serverManager = new ServerManager(node);
        clientManager = new ClientManager(node);
//        apiHandler = new ApiHandler(node);
        //Starting the server thread
        serverThread.start();
        serverinfo += "- - - SERVER STARTED - - -\n";

    }

    @Override
    public void onClick(View view) {

        // Start initial connection
        if (view == startClientButton) {
            if (!clientStarted) {
                clientStarted = true;
                // Thread running initially when joining network
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!node.isInNetwork){
                            clientManager.joinNetwork(REMOTE_IP_ADDRESS);
                            node.isInNetwork = true;
                        }
                    }
                });
                t.start();
                clientinfo += "- - - CLIENT STARTED - - - \n";
                startClientButton.setText("Resend");
            }

        // Save the input IP
        } else if (view == submitIP) {
            String inputIP = ipInputField.getText().toString();
            if (!inputIP.equals("")){
                REMOTE_IP_ADDRESS = inputIP;
            } else {
                REMOTE_IP_ADDRESS = THIS_IP_ADDRESS;
            }
            ipInputField.setText("");
            startClientButton.setEnabled(true);

        // Get server's ID
        } else if (view == getIdButton) {
            if(ipInputField.getText().toString().equals("")){

                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_GetId();
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            }

        // Button to get the server's phonebooks
        } else if (view == getPhonebookButton) {
            if(ipInputField.getText().toString().equals("")){

                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_GetPhonebook();
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            }

        // Update server's phonebook
        } else if (view == updatePhonebookButton) {
            String input = ipInputField.getText().toString();
            String[] newCommandList = input.split(";");
            if (ipInputField.getText().toString().equals("")){
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_UpdatePhoneBook(node.phoneBookLeft.copy(), "left");
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            } else {
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_UpdatePhoneBook(new PhoneBook(newCommandList[0]), newCommandList[1]);
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            }

        }

        // get data button
        else if (view == getDataButton) {
            String input = ipInputField.getText().toString();
            if(ipInputField.getText().toString().equals("")){
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_GetData("data1");
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            } else {
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_GetData(input);
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            }

        // Add data to server
        } else if (view == addDataButton) {
            String input = ipInputField.getText().toString();
            String[] newCommandList = input.split(";");
            if(ipInputField.getText().toString().equals("")){
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_AddData("data1", true);
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            } else {
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_AddData(newCommandList[0],
                        Boolean.parseBoolean(newCommandList[1]));
                Thread clientThread = new Thread(cThread);
                clientThread.start();

            }

        // Delete data from server
        } else if (view == deleteDataButton) {
            String input = ipInputField.getText().toString();
            String[] newCommandList = input.split(";");
            if(ipInputField.getText().toString().equals("")){
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_DeleteData("data1", true);
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            } else {
                MyClientThread cThread = new MyClientThread();
                cThread.request = clientManager.generateRequest_DeleteData(newCommandList[0], Boolean.parseBoolean(newCommandList[1]));
                Thread clientThread = new Thread(cThread);
                clientThread.start();
            }

        // Posts own and neighbor's data in the UI
        } else if (view == showDataButton) {
            String thisNodeData = "This node data: " + node.listOfData.toString() + "\n";
            String leftData = "left neighbor data: " + node.neighborLeft.listOfData.toString()+ "\n";
            String rightData = "right neighbor data: " +node.neighborRight.listOfData.toString();
            sUpdate(thisNodeData + leftData + rightData);
            cUpdate(thisNodeData + leftData + rightData);
        // Posts phonebooks on the UI
        } else if (view == showPhonebookButton) {
            String thisNodeData = "Right phonebook: " + node.phoneBookRight.toString() + "\n";
            String thisNodePhonebookLeft = "Left phonebook: " + node.phoneBookLeft.toString() + "\n";
            sUpdate(thisNodeData + thisNodePhonebookLeft );

        }

    }

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
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
    }

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

                // Receive client outstream
                String requestString = instream.readUTF();

                System.out.println("Recived requestString:");
                System.out.println(requestString);

                Request requestFromClient = new Request(requestString);
                sUpdate("Client " + number + " requests: " + requestFromClient.toString());

                // Handle the request and return a Response to send back
                Response response = serverManager.handleRequestFromClient(requestFromClient, REMOTE_IP_ADDRESS);

                String answer = response.toString();

                // Send response back to client
                sUpdate("Server " + number + " responds : " + answer);
                outstream.writeUTF(answer);
                outstream.flush();

                instream.close();
                sUpdate("SERVER: Remote client " + number + " inputstream closed");
                outstream.close();
                sUpdate("SERVER: Remote client  " + number + "outputstream closed");
                client.close();
                sUpdate("SERVER: Remote client " + number + " socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
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

        Request request;
        @Override
            public void run() {
            try {
//                System.out.println(!node.isInNetwork);
//                if (!node.isInNetwork){
//
//                    sUpdate("WE ARE IN");
//                    clientManager.joinNetwork(REMOTE_IP_ADDRESS);
//                    node.isInNetwork = true;
//                } else {

                    cUpdate("CLIENT: starting client socket ");
                    cUpdate("CLIENT: client connected ");


                    Socket connectionToServer = new Socket(REMOTE_IP_ADDRESS, 4444);

                    DataInputStream instream = new DataInputStream(connectionToServer.getInputStream());
                    DataOutputStream out = new DataOutputStream(connectionToServer.getOutputStream());


                    String message = request.toString();

                    out.writeUTF(message);
                    out.flush();
                    cUpdate("Client said:      " + message)
                    ;
                    String messageFromServer = instream.readUTF();

                    System.out.println("THIS IS THE MESSAGEFROMSERVER:");
                    System.out.println(messageFromServer);

                    Response response = new Response(messageFromServer);
                    instream.close();
                    out.close();
                    connectionToServer.close();
                    clientManager.handleResponseFromServer(request, response);

                    cUpdate("Server says: " + messageFromServer);

                    cUpdate("CLIENT: closed inputstream");
                    cUpdate("CLIENT: closed outputstream");
                    cUpdate("CLIENT: closed socket");

//                }

            }  catch (UnknownHostException uhe) {
                clientManager.findDeadNodeNeighbor(REMOTE_IP_ADDRESS);
                clientManager.generateRequest_FixNeighbor("left");
                System.out.println(uhe);



            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }//run()
    } //class MyClientThread



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