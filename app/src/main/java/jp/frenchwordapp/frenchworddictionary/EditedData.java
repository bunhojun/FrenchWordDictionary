package jp.frenchwordapp.frenchworddictionary;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class EditedData extends RealmObject implements Serializable {

    private boolean correct, wrong;
    private String level, partOfSpeech;
    @PrimaryKey
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setWrong(boolean wrong) {
        this.wrong = wrong;
    }

    public boolean isWrong() {
        return wrong;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

}
