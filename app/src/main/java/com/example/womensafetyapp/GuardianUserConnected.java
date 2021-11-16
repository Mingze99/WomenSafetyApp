package com.example.womensafetyapp;

public class GuardianUserConnected {
    private String userID;
    private String guardianID;

    public GuardianUserConnected(){
        this.userID = "";
        this.guardianID = "";
    }

    public GuardianUserConnected(String userID, String guardianID){
        this.userID = userID;
        this.guardianID = guardianID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGuardianID() {
        return guardianID;
    }

    public void setGuardianID(String guardianID) {
        this.guardianID = guardianID;
    }
}
