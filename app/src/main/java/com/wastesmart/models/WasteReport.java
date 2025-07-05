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
    private String status; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED
    private Date reportDate;
    private Long timestamp; // Added for compatibility with adapters
    private String submittedBy; // User ID who submitted the report
    private String submitterName; // Name of the user who submitted the report

    private String assignedCollectorId;
    private String assignedCollectorName;
    private Long assignedTimestamp;

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
        this.timestamp = reportDate != null ? reportDate.getTime() : System.currentTimeMillis();
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
        return status != null ? status.toUpperCase() : "PENDING";
    }

    public void setStatus(String status) {
        if (status != null) {
            this.status = status.toUpperCase();
        } else {
            this.status = "PENDING";
        }
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
        this.timestamp = reportDate != null ? reportDate.getTime() : System.currentTimeMillis();
    }

    // Additional getter methods for adapter compatibility
    public String getSize() {
        return wasteSize;
    }

    public void setSize(String size) {
        this.wasteSize = size;
    }

    public String getImageUrl() {
        return photoUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.photoUrl = imageUrl;
    }

    public Long getTimestamp() {
        if (timestamp == null && reportDate != null) {
            timestamp = reportDate.getTime();
        }
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        if (timestamp != null) {
            this.reportDate = new Date(timestamp);
        }
    }

    public String getAssignedCollectorId() {
        return assignedCollectorId;
    }

    public void setAssignedCollectorId(String assignedCollectorId) {
        this.assignedCollectorId = assignedCollectorId;
    }

    public String getAssignedCollectorName() {
        return assignedCollectorName;
    }

    public void setAssignedCollectorName(String assignedCollectorName) {
        this.assignedCollectorName = assignedCollectorName;
    }

    public Long getAssignedTimestamp() {
        return assignedTimestamp;
    }

    public void setAssignedTimestamp(Long assignedTimestamp) {
        this.assignedTimestamp = assignedTimestamp;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }
}
