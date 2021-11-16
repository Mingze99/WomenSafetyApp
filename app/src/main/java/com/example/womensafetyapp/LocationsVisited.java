package com.example.womensafetyapp;

public class LocationsVisited {
    private String location;
    private String timestamp;

    public LocationsVisited(){
        this.location = "";
        this.timestamp = "";
    }

    public LocationsVisited(String location, String timestamp){
        this.location = location;
        this.timestamp = timestamp;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
