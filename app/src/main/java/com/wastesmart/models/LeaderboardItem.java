package com.wastesmart.models;

public class LeaderboardItem {
    private String id;
    private String name;
    private int rank;
    private int score;
    private String zone;
    
    // Default constructor for Firestore
    public LeaderboardItem() {
    }
    
    public LeaderboardItem(String id, String name, int rank, int score, String zone) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.score = score;
        this.zone = zone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
