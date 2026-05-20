package model;

public class Vocabulary {
    private int wordID;
    private String word;
    private String meaning;
    private String pronunciation;
    private String example;

    private int languageID;
    private int topicID;
    private String type;
    private String status;
    private String imagePath;

    public Vocabulary() {}
    public Vocabulary(int wordID,String word,String meaning,String pronunciation,String example,int languageID,int topicID,String type,String status,String imagePath) {
        this.wordID = wordID;
        this.word = word;
        this.meaning = meaning;
        this.pronunciation = pronunciation;
        this.example = example;

        this.languageID = languageID;
        this.topicID = topicID;
        this.type = type;
        this.status = status;
        this.imagePath = imagePath;

    }
    public int getWordID() {return wordID;}
    public void setWordID(int wordID) {this.wordID = wordID;}
    public String getWord() {return word;}
    public void setWord(String word) {this.word = word;}
    public String getMeaning() {return meaning;}
    public void setMeaning(String meaning) {this.meaning = meaning;}
    public String getPronunciation() {return pronunciation;}
    public void setPronunciation(String pronunciation) {this.pronunciation = pronunciation;}
    public String getExample() {return example;}
    public void setExample(String example) {this.example = example;}

    public int getLanguageID() {return languageID;}
    public void setLanguageID(int languageID) {this.languageID = languageID;}
    public int getTopicID() {return topicID;}
    public void setTopicID(int topicID) {this.topicID = topicID;}
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public String getImagePath() {return imagePath;}
    public void setImagePath(String imagePath) {this.imagePath = imagePath;}

}
