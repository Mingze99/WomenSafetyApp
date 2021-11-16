package com.example.womensafetyapp;

public class GuardianDetails {
    private String guardianID, initial, name, relationship, email, contact;

    public GuardianDetails(){
        this.guardianID = "";
        this.initial = "";
        this.name = "";
        this.relationship = "";
        this.email = "";
        this.contact = "";
    }

    public GuardianDetails(String guardianID, String initial, String name, String relationship, String email, String contact){
        this.guardianID = guardianID;
        this.initial = initial;
        this.name = name;
        this.relationship = relationship;
        this.email = email;
        this.contact = contact;
    }

    public String getGuardianID() {
        return guardianID;
    }

    public void setGuardianID(String guardianID) {
        this.guardianID = guardianID;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial){
        this.initial = initial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }


}
