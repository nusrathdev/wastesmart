package com.wastesmart.models;

import java.util.Date;

public class WasteReport {
    private String id;
    private String userId;
    private String wasteType;
    private String wasteSize;
    private String description;
    private double latitude;
    private double longitude;
    private String photoUrl;
    private String status; // PENDING, ASSIGNED, COLLECTED, CANCELED
    private Date reportDate;

    // Required empty constructor for Firestore
    public WasteReport() {
    }

    public WasteReport(String id, String userId, String wasteType, String wasteSize,
                      String description, double latitude, double longitude,
                      String photoUrl, String status, Date reportDate) {
        this.id = id;
        this.userId = userId;
        this.wasteType = wasteType;
        this.wasteSize = wasteSize;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.status = status;
        this.reportDate = reportDate;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public String getWasteSize() {
        return wasteSize;
    }

    public void setWasteSize(String wasteSize) {
        this.wasteSize = wasteSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
}
