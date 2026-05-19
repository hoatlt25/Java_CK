package model;

public class Language {
    private String LanguageID;
    private String LanguageName;

    public Language(){ }
    public Language(String LanguageID, String LanguageName) {
        this.LanguageID = LanguageID;
        this.LanguageName = LanguageName;
    }
    public String getLanguageID() {return LanguageID;}
    public void setLanguageID(String LanguageID) {this.LanguageID = LanguageID;}

    public String getLanguageName() {return LanguageName;}
    public void setLanguageName(String LanguageName) {this.LanguageName = LanguageName;}

}
