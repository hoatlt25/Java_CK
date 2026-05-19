package model;

import java.time.LocalDateTime;

public class LearningProgress {
    private int id;
    private String status;
    private int review_count;
    private int userID;
    private int wordID;
    private LocalDateTime last_review_date;

    public LearningProgress(){ }

    public LearningProgress(int id, String status, int review_count, int userID, int wordID) {
        this.id = id;
        this.status = status;
        this.review_count = review_count;
        this.userID = userID;
        this.wordID = wordID;
        this.last_review_date = LocalDateTime.now();
    }
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getStatus() { return status;}
    public void setStatus(String status) { this.status = status;}
    public int getReview_count() { return review_count;}
    public void setReview_count(int review_count) { this.review_count = review_count;}
    public int getUserID() { return userID;}
    public void setUserID(int userID) { this.userID = userID;}
    public int getWordID() { return wordID;}
    public void setWordID(int wordID) { this.wordID = wordID;}
    public LocalDateTime getLast_review_date() { return last_review_date;}
    public void setLast_review_date(LocalDateTime last_review_date) {this.last_review_date = last_review_date;}
}
