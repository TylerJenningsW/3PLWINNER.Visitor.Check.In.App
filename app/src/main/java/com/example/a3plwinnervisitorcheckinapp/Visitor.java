package com.example.a3plwinnervisitorcheckinapp;

public class Visitor {
    private String firstName, lastName, company, whoAreYouVisiting, reason, checkInTime, checkOutTime, documentId, emergencyContact, emergencyPhone;
    private boolean isCheckedIn;

    public Visitor() {

    }
    public Visitor(String firstName, String lastName,
                   String company, String whoAreYouVisiting,
                   String reason, String checkInTime, String checkOutTime, boolean isCheckedIn,
                   String emergencyContact, String emergencyPhone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.whoAreYouVisiting = whoAreYouVisiting;
        this.reason = reason;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.isCheckedIn = isCheckedIn;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone = emergencyPhone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getWhoAreYouVisiting() {
        return whoAreYouVisiting;
    }

    public void setWhoAreYouVisiting(String whoAreYouVisiting) {
        this.whoAreYouVisiting = whoAreYouVisiting;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public boolean getCheckedIn() {
        return isCheckedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        isCheckedIn = checkedIn;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

}
