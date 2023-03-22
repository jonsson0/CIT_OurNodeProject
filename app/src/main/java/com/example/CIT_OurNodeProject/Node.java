package com.example.CIT_OurNodeProject;

import org.json.JSONObject;

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

    }

    public void addDataFromString(String inputString, Boolean isParent) {
        Data data = new Data(inputString, isParent);
        listOfData.add(data);
    }


    public void contactNeighbor(String IP, String path) {

    }
    public boolean deleteDataLocally(String Value) {
        for (int i = 0; i < listOfData.size(); i++) {
            //if our node has the data
            if (listOfData.get(i).equals(Value)) {
                listOfData.remove(listOfData.get(i));
                return true;
            }
        }
        return false;
    }


}
