package jp.frenchwordapp.frenchworddictionary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.HashMap;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;


public class WordCardActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {

        }
    };

    Button mButton1;
    Button mMeaningButton;
    ImageButton mHearingButton;
    ImageButton mNextButton;
    ImageButton mBackButton;
    TextView mWordText;
    TextView mCategoryText;
    TextView mPartOfSpeechText;

    Cursor cursor;

    private TextToSpeech engine;
    private Word mWordData;
    private int mWordId;
    private String mWord;
    private String mCategoryIntent;
    private String mCategory;
    private String mPartOfSpeechIntent;
    private String mPartOfSpeech;
    private String mMeaning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);
        engine = new TextToSpeech(this, this);

        mButton1 = findViewById(R.id.consealButton);
        mMeaningButton = findViewById(R.id.meaningButton);
        mHearingButton = findViewById(R.id.hearingButton);
        mHearingButton.setOnClickListener(this);
        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(this);
        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener(this);
        mMeaningButton.setVisibility(View.GONE);
        mWordText = findViewById(R.id.word);
        mCategoryText = findViewById(R.id.category);
        mPartOfSpeechText = findViewById(R.id.partOfSpeech);

        //データ受け取り
        Intent intent = getIntent();
        mWordId = intent.getIntExtra("EXTRA_WORD", 0); //id of the first word
        mCategoryIntent = intent.getStringExtra("CATEGORY");
        mPartOfSpeechIntent = intent.getStringExtra("HINSHI");
        if(mCategoryIntent != null){
           mCategory = mCategoryIntent;
        }else if(mPartOfSpeechIntent != null){
            mPartOfSpeech = mPartOfSpeechIntent;
        }

        //レルム
        mRealm = Realm.getDefaultInstance();
        mWordData = mRealm.where(Word.class).equalTo("id", mWordId).findFirst();
        mRealm.close();

        //最初に表示される単語情報
        mWord = mWordData.getWord();
        mWordText.setText(mWord);
        String category = mWordData.getCategory();
        mCategoryText.setText(category);
        String partOfSpeech = mWordData.getPartOfSpeech();
        mPartOfSpeechText.setText(partOfSpeech);
        mMeaning = mWordData.getMeaning();
        mMeaningButton.setText(mMeaning);


        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMeaningButton.setVisibility(View.VISIBLE);
                mButton1.setVisibility(View.GONE);
            }
        });

        mMeaningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMeaningButton.setVisibility(View.GONE);
                mButton1.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hearingButton:
                speech();
                Log.d("Speech", "speech is on");
                break;

            case R.id.nextButton:
                setNextWord();
                mMeaningButton.setVisibility(View.GONE);
                mButton1.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onInit(int status) {
        Log.d("Speech", "OnInit - Status [" + status + "]");

        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            engine.setLanguage(Locale.FRENCH);
        }
    }

    private void speech() {
        engine.speak(mWord, TextToSpeech.QUEUE_FLUSH, null, null);
        Log.d("Speech", "speak is on");
    }

    private void setNextWord() {
        if (mCategoryIntent != null) {
            Word nextWordData = mRealm.where(Word.class).equalTo("category", mCategoryIntent).greaterThan("id", mWordId).findFirst();
            String nextWord = nextWordData.getWord();
            String nextMeaning = nextWordData.getMeaning();
            String nextCategory = nextWordData.getCategory();
            String nextPartOfSpeech = nextWordData.getPartOfSpeech();
            int nextId = nextWordData.getId();

            //set text
            mWordText.setText(nextWord);
            mMeaningButton.setText(nextMeaning);
            mCategoryText.setText(nextCategory);
            mPartOfSpeechText.setText(nextPartOfSpeech);
            //renew the word
            mWord = nextWord;
            mWordId = nextId;
            mMeaning = nextMeaning;
            mPartOfSpeech = nextPartOfSpeech;
        } else if (mPartOfSpeech != null) {
            Word nextWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).greaterThan("id", mWordId).findFirst();
            String nextWord = nextWordData.getWord();
            String nextMeaning = nextWordData.getMeaning();
            String nextCategory = nextWordData.getCategory();
            String nextPartOfSpeech = nextWordData.getPartOfSpeech();
            int nextId = nextWordData.getId();

            //set text
            mWordText.setText(nextWord);
            mMeaningButton.setText(nextMeaning);
            mCategoryText.setText(nextCategory);
            mPartOfSpeechText.setText(nextPartOfSpeech);
            //renew the word
            mWord = nextWord;
            mWordId = nextId;
            mMeaning = nextMeaning;
            mCategory = nextCategory;
        } else {

            Word nextWordData = mRealm.where(Word.class).greaterThan("id", mWordId).findFirst();
            String nextWord = nextWordData.getWord();
            String nextMeaning = nextWordData.getMeaning();
            String nextCategory = nextWordData.getCategory();
            String nextPartOfSpeech = nextWordData.getPartOfSpeech();
            int nextId = nextWordData.getId();

            //set text
            mWordText.setText(nextWord);
            mMeaningButton.setText(nextMeaning);
            mCategoryText.setText(nextCategory);
            mPartOfSpeechText.setText(nextPartOfSpeech);
            //renew the word
            mWord = nextWord;
            mWordId = nextId;
            mMeaning = nextMeaning;
            mCategory = nextCategory;
            mPartOfSpeech = nextPartOfSpeech;
        }
    }

    private void setPreviousWord() {

    }


}
