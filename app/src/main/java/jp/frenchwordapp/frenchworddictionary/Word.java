package jp.frenchwordapp.frenchworddictionary;

import android.content.Intent;
import android.util.Log;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ponnp on 17/04/2018.
 */

public class Word  extends RealmObject implements Serializable {
    private String word;
    private String meaning;
    private String partOfSpeech;
    private String category;
    private boolean checked, wrong, correct;


    @PrimaryKey
    private int id;

    public void setId(String sid) {
        id = Integer.parseInt(sid);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }
}
