package com.example.CIT_OurNodeProject;

import org.json.JSONObject;

import java.util.ArrayList;

public class Node {


    boolean isInNetwork = false;

    String IP;

    ArrayList<Data> listOfData = new ArrayList<Data>();

    Neighbor neighborLeft = new Neighbor();

    Neighbor neighborRight = new Neighbor();

    PhoneBook phoneBookLeft = new PhoneBook();

    PhoneBook phoneBookRight = new PhoneBook();

    public Node(String IP){
        this.IP = IP;
    }

    public Node(){

    }

    public boolean checkForData(String id){
        for (Data data : listOfData) {
            if (data.id.equals(id)) {
                System.out.println(id);
                return true;
            }
        }
        return false;
    }

    public boolean hasData(Data data){
        for ( Data dat:
                this.listOfData) {
            if (data.value.equals(dat.value))
                return true;
        }
        return false;
    }

    public void addDataFromString(String inputString, Boolean isParent) {
        Data data = new Data(inputString, isParent);
        listOfData.add(data);
    }


    public void contactNeighbor(String IP, String path) {

    }
    public void printAllData(){

        for (int i = 0; i < listOfData.size(); i++) {
            System.out.println(listOfData.get(i).value);
        }
    }
    public boolean deleteDataLocally(String id) {
        for (int i = 0; i < listOfData.size(); i++) {
            //if our node has the data
            if (listOfData.get(i).id.equals(id)) {
                listOfData.remove(listOfData.get(i));
                return true;
            }
        }
        return false;
    }

    public void addData(ArrayList<Data> dataToAdd){
        for (Data data:dataToAdd) {
            this.listOfData.add(data);
        }
    }

    public void addData(Data dataToAdd){
        this.listOfData.add(dataToAdd);
    }

}
