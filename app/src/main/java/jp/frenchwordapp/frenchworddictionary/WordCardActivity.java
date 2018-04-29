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

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static jp.frenchwordapp.frenchworddictionary.SettingActivity.mIsRandom;


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


    private TextToSpeech engine;
    private Word mWordData;
    private int mWordId;
    private String mWord;
    private String mCategoryIntent;
    private String mCategory;
    private String mPartOfSpeechIntent;
    private String mPartOfSpeech;
    private String mMeaning;

    private String mNextWord;
    private String mNextMeaning;
    private String mNextCategory;
    private String mNextPartOfSpeech;
    private int mNextId;
    private Word mSecondNextWordData;
    private String mPreviousWord;
    private String mPreviousMeaning;
    private String mPreviousCategory;
    private String mPreviousPartOfSpeech;
    private int mPreviousId;
    private Word mSecondPreviousWordData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);
        //音声読み上げのインスタンス
        engine = new TextToSpeech(this, this);

        //ボタンとテキストのセット
        mButton1 = findViewById(R.id.consealButton);
        mMeaningButton = findViewById(R.id.meaningButton);
        mMeaningButton.setVisibility(View.GONE);
        mHearingButton = findViewById(R.id.hearingButton);
        mHearingButton.setOnClickListener(this);
        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(this);
        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener(this);
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

        if(mCategoryIntent != null){

            Word nextWordData = mRealm.where(Word.class).equalTo("category", mCategory).greaterThan("id", mWordId).findFirst();  //次の言葉のデータを取得
            Word previousWordData = mRealm.where(Word.class).equalTo("category", mCategory).lessThan("id", mWordId).findFirst(); //前の言葉のデータを取得
            if(nextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }else if(previousWordData == null){
                mBackButton.setVisibility(View.GONE);
            }

        }else if(mPartOfSpeechIntent != null){

            Word nextWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).greaterThan("id", mWordId).findFirst(); //次の言葉のデータを取得
            Word previousWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).lessThan("id", mWordId).findFirst(); //前の言葉
            if(nextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }else if(previousWordData == null){
                mBackButton.setVisibility(View.GONE);
            }
        }else {
            Word nextWordData = mRealm.where(Word.class).greaterThan("id", mWordId).findFirst(); //次の言葉のデータを取得
            Word previousWordData = mRealm.where(Word.class).lessThan("id", mWordId).findFirst(); //前の言葉
            if(nextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }else if(previousWordData == null){
                mBackButton.setVisibility(View.GONE);
            }
        }






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
                if(mSecondNextWordData == null) {
                    mNextButton.setVisibility(View.GONE);
                }
                mBackButton.setVisibility(View.VISIBLE);
                break;

            case R.id.backButton:
                setPreviousWord();
                mMeaningButton.setVisibility(View.GONE);
                mButton1.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.VISIBLE);
                if(mSecondPreviousWordData == null) {
                    mBackButton.setVisibility(View.GONE);
                }
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

        //次に表示される単語情報
        if (mCategoryIntent != null) { //カテゴリーボタンを押したときの変遷の場合
            Word nextWordData = mRealm.where(Word.class).equalTo("category", mCategory).greaterThan("id", mWordId).findFirst();  //次の言葉のデータを取得
            mNextWord = nextWordData.getWord();
            mNextMeaning = nextWordData.getMeaning();
            mNextCategory = nextWordData.getCategory();
            mNextPartOfSpeech = nextWordData.getPartOfSpeech();
            mNextId = nextWordData.getId();
            mSecondNextWordData = mRealm.where(Word.class).equalTo("category", mNextCategory).greaterThan("id", mNextId).findFirst();
        } else if (mPartOfSpeechIntent != null) { //品詞ボタンのときの変遷の場合
            Word nextWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).greaterThan("id", mWordId).findFirst(); //次の言葉のデータを取得
            mNextWord = nextWordData.getWord();
            mNextMeaning = nextWordData.getMeaning();
            mNextCategory = nextWordData.getCategory();
            mNextPartOfSpeech = nextWordData.getPartOfSpeech();
            mNextId = nextWordData.getId();
            mSecondNextWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mNextPartOfSpeech).greaterThan("id", mNextId).findFirst();
        } else { //一覧からの場合

                  Word nextWordData = mRealm.where(Word.class).greaterThan("id", mWordId).findFirst();

                mNextWord = nextWordData.getWord();
                mNextMeaning = nextWordData.getMeaning();
                mNextCategory = nextWordData.getCategory();
                mNextPartOfSpeech = nextWordData.getPartOfSpeech();
                mNextId = nextWordData.getId();
                mSecondNextWordData = mRealm.where(Word.class).greaterThan("id", mNextId).findFirst();
            //次の言葉のデータを取得

        }
            //set text
            mWordText.setText(mNextWord);
            mMeaningButton.setText(mNextMeaning);
            mCategoryText.setText(mNextCategory);
            mPartOfSpeechText.setText(mNextPartOfSpeech);
            //renew the word
            mWord = mNextWord;
            mWordId = mNextId;
            mMeaning = mNextMeaning;
            mCategory = mNextCategory;
            mPartOfSpeech = mNextPartOfSpeech;

    }

    private void setPreviousWord() {

        //前に表示される単語情報
        if(mCategoryIntent != null) {

            Word previousWordData = mRealm.where(Word.class).equalTo("category", mCategory).lessThan("id", mWordId).findAll().last();
            mPreviousWord = previousWordData.getWord();
            mPreviousMeaning = previousWordData.getMeaning();
            mPreviousCategory = previousWordData.getCategory();
            mPreviousPartOfSpeech = previousWordData.getPartOfSpeech();
            mPreviousId = previousWordData.getId();
            mSecondPreviousWordData = mRealm.where(Word.class).equalTo("category", mPreviousCategory).lessThan("id", mPreviousId).findFirst();
        } else if(mPartOfSpeechIntent != null) {

            Word previousWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).lessThan("id", mWordId).findAll().last();
            mPreviousWord = previousWordData.getWord();
            mPreviousMeaning = previousWordData.getMeaning();
            mPreviousCategory = previousWordData.getCategory();
            mPreviousPartOfSpeech = previousWordData.getPartOfSpeech();
            mPreviousId = previousWordData.getId();
            mSecondPreviousWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPreviousPartOfSpeech).lessThan("id", mPreviousId).findFirst();

        } else {
            Word previousWordData = mRealm.where(Word.class).lessThan("id", mWordId).findAll().last();
            mPreviousWord = previousWordData.getWord();
            mPreviousMeaning = previousWordData.getMeaning();
            mPreviousCategory = previousWordData.getCategory();
            mPreviousPartOfSpeech = previousWordData.getPartOfSpeech();
            mPreviousId = previousWordData.getId();
            mSecondPreviousWordData  = mRealm.where(Word.class).lessThan("id", mPreviousId).findFirst();
        }

            //set text
            mWordText.setText(mPreviousWord);
            mMeaningButton.setText(mPreviousMeaning);
            mCategoryText.setText(mPreviousCategory);
            mPartOfSpeechText.setText(mPreviousPartOfSpeech);
            //renew the word
            mWord = mPreviousWord;
            mWordId = mPreviousId;
            mMeaning = mPreviousMeaning;
            mCategory = mPreviousCategory;
            mPartOfSpeech = mPreviousPartOfSpeech;
    }


}
