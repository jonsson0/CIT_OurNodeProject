package com.example.CIT_OurNodeProject;

import java.util.ArrayList;

public class Node {

    String IP;

    ArrayList<Data> listOfData = new ArrayList<Data>();

    Neighbor neighborLeft = new Neighbor();

    Neighbor neighborRight = new Neighbor();

    PhoneBook phoneBookLeft = new PhoneBook();

    PhoneBook phoneBookRight = new PhoneBook();

    public Node(String IP){
        this.IP = IP;
    }

    public void init(String IP, ApiHandler apiHandler) {
        apiHandler.createHttpRequestAsString("get", "getPhoneBook", "");
    }

    public void addDataFromString(String inputString) {
        Data data = new Data(inputString);
        listOfData.add(data);
    }

    public void addData(Data data){
        listOfData.add(data);
    }

    public void contactNeighbor(String IP, String path) {

    }

    public Data getData(String id){
        for (Data data:
             listOfData) {
            if (data.id.equals(id)){
                return data;
            }
        }
        return null;

    }

    public Data getData(Data dataPar){
        for (Data data:
                listOfData) {
            if (data.id.equals(dataPar.id)){
                return data;
            }
        }
        return null;

    }



}
