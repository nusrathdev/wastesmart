package com.wastesmart.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

/**
 * Model class for waste collection reports.
 */
public class Report {
    private String id;
    private String title;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private String wasteType;
    private String imageUrl;
    private String assignedCollectorId;
    private String userId;
    private long reportDate;

    // Required empty constructor for Firestore
    public Report() {
    }

    // Constructor with parameters
    public Report(String title, String description, String location, double latitude, double longitude,
                 String status, String wasteType, String imageUrl, String userId, String assignedCollectorId, long reportDate) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.wasteType = wasteType;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.assignedCollectorId = assignedCollectorId;
        this.reportDate = reportDate;
    }

    // Getter and setter methods
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAssignedCollectorId() {
        return assignedCollectorId;
    }

    public void setAssignedCollectorId(String assignedCollectorId) {
        this.assignedCollectorId = assignedCollectorId;
    }

    public long getReportDate() {
        return reportDate;
    }

    public void setReportDate(long reportDate) {
        this.reportDate = reportDate;
    }
    
    @Exclude
    public Date getReportDateAsDate() {
        return new Date(reportDate);
    }
}
