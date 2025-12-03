package com.example.taskflow;
public class Task {
    // Attributes of the task
    private String title;
    private String dateTimeRange; // Text showing the date and time range

    // Constructor
    public Task(String title, String dateTimeRange) {
        this.title = title;
        this.dateTimeRange = dateTimeRange;
    }

    // Getters to access the data
    public String getTitle() {
        return title;
    }

    public String getDateTimeRange() {
        return dateTimeRange;
    }
}
