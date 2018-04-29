package jp.frenchwordapp.frenchworddictionary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mWordEdit;
    EditText mMeaningEdit;
    Button mVerbButton;
    Button mNounButton;
    Button mLifeButton;
    Button mRegisterButton;
    Realm mRealm;
    Word mWord;
    Boolean mIsVerb = false;
    Boolean mIsNoun = false;
    Boolean mIsLife = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRealm = Realm.getDefaultInstance();
        mWordEdit = findViewById(R.id.wordEdit);
        mMeaningEdit = findViewById(R.id.meaningEdit);
        mWord = new Word();

        mVerbButton = findViewById(R.id.verbButton);
        mVerbButton.setOnClickListener(this);
        mNounButton = findViewById(R.id.nounButton);
        mNounButton.setOnClickListener(this);
        mLifeButton = findViewById(R.id.lifeButton);
        mLifeButton.setOnClickListener(this);
        mRegisterButton = findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.verbButton:
                mIsVerb = true;
                break;
            case R.id.nounButton:
                mIsNoun = true;
                break;
            case R.id.lifeButton:
                mIsLife = true;
                break;
            case R.id.registerButton:
                addWord();
      //          Intent intent = new Intent(RegisterActivity.this, WordListActivity.class);
      //          startActivity(intent);
                break;
        }
    }

    private void addWord() {
       RealmResults<Word> realmResults = mRealm.where(Word.class).findAll();

       int identifier;
       identifier = realmResults.max("id").intValue() + 1;
       String sIdentifier = String.valueOf(identifier);
       mWord.setId(sIdentifier);
       String word = mWordEdit.getText().toString();
       String meaning = mMeaningEdit.getText().toString();
       mWord.setWord(word);
       mWord.setMeaning(meaning);

       if (mIsVerb) {
           mWord.setPartOfSpeech("動詞");
       }else if(mIsNoun){
           mWord.setPartOfSpeech("名詞");
       }

       if(mIsLife){
           mWord.setCategory("生活");
       }

        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(mWord);
        mRealm.commitTransaction();

        mRealm.close();
    }
}
