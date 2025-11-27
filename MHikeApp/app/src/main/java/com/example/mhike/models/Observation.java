package com.example.mhike.models;

import java.io.Serializable;

public class Observation implements Serializable {
    private int id;
    private int hikeId;
    private String observationText;
    private String time;
    private String comments;

    // Constructors
    public Observation() {
    }

    public Observation(int hikeId, String observationText, String time, String comments) {
        this.hikeId = hikeId;
        this.observationText = observationText;
        this.time = time;
        this.comments = comments;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHikeId() {
        return hikeId;
    }

    public void setHikeId(int hikeId) {
        this.hikeId = hikeId;
    }

    public String getObservationText() {
        return observationText;
    }

    public void setObservationText(String observationText) {
        this.observationText = observationText;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Observation{" +
                "id=" + id +
                ", hikeId=" + hikeId +
                ", observationText='" + observationText + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
