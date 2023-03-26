package com.example.CIT_OurNodeProject;

import java.util.ArrayList;

public class Neighbor  {

    String IP;

    ArrayList<Data> listOfData = new ArrayList<Data>();

    public Neighbor(String IP) {
        this.IP = IP;
    }

    public Neighbor() {
    }
    public boolean hasData(Data data){
        for ( Data dat:
                this.listOfData) {
            if (data.value.equals(dat.value))
                return true;
        }
        return false;
    }

    public void addData(ArrayList<Data> dataToAdd){
        for (Data data:dataToAdd) {
            this.listOfData.add(data);
        }
    }

    public void removeAllData() {
        listOfData.clear();
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


