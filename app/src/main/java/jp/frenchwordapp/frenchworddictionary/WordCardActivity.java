package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Locale;

import io.realm.Realm;


public class WordCardActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private Realm mRealm;

    Button mConcealButton, mMeaningButton;
    ImageButton mHearingButton, mNextButton, mBackButton;
    TextView mWordText, mCategoryText, mPartOfSpeechText;

    boolean isCorrect, isWrong;


    private TextToSpeech engine;
    private Word mWordData, mNextWordData, mPreviousWordData; //don't edit here
    private EditedData mEditedData;
    private int mWordId;
    private String mWord, mCategoryIntent, mCategory, mPartOfSpeechIntent, mPartOfSpeech, mMeaning;
    SharedPreferences mSetting;
    private Boolean mIsReverse, mIsAutoPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);

        //setting
        mSetting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        mIsReverse = mSetting.getBoolean("reverse", false);
        mIsAutoPlay = mSetting.getBoolean("autoPlay", false);

        //音声読み上げのインスタンス
        engine = new TextToSpeech(this, this);

        //ボタンとテキストのセット
        mConcealButton = findViewById(R.id.concealButton);
        mConcealButton.setOnClickListener(this);
        mMeaningButton = findViewById(R.id.meaningButton);
        mMeaningButton.setVisibility(View.GONE);
        mMeaningButton.setOnClickListener(this);
        mHearingButton = findViewById(R.id.hearingButton);
        mHearingButton.setOnClickListener(this);
        if (mIsReverse) {
            mHearingButton.setVisibility(View.GONE);
        }
        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(this);
        mBackButton = findViewById(R.id.backButton);
        mBackButton.setOnClickListener(this);
        mNextButton.setVisibility(View.VISIBLE);
        mBackButton.setVisibility(View.VISIBLE);
        mWordText = findViewById(R.id.word);
        mCategoryText = findViewById(R.id.category);
        mPartOfSpeechText = findViewById(R.id.partOfSpeech);


        //Intentデータ受け取り
        Intent intent = getIntent();
        mWordId = intent.getIntExtra("EXTRA_WORD", 0); //id of the first word. if 1d = 0, from menu button
        mCategoryIntent = intent.getStringExtra("CATEGORY");
        mPartOfSpeechIntent = intent.getStringExtra("HINSHI");
        if (mCategoryIntent != null) {
            mCategory = mCategoryIntent;
        } else if (mPartOfSpeechIntent != null) {
            mPartOfSpeech = mPartOfSpeechIntent;
        }

        mRealm = Realm.getDefaultInstance();
        //Wordレルム
        mWordData = mRealm.where(Word.class).equalTo("id", mWordId).findFirst();
        //EditedData (to change default data)
        mEditedData = mRealm.where(EditedData.class).equalTo("id", mWordId).findFirst();
        //最初に表示される単語情報
        mWord = mWordData.getWord();
        mWordId = mWordData.getId();
        mMeaning = mWordData.getMeaning();
        if (mIsReverse) { //日→仏
            mWordText.setText(mMeaning);
            mMeaningButton.setText(mWord);
        } else {          //仏→日
            mWordText.setText(mWord);
            mMeaningButton.setText(mMeaning);
        }
        String category = mWordData.getCategory();
        mCategoryText.setText(category);
        String partOfSpeech = mWordData.getPartOfSpeech();
        mPartOfSpeechText.setText(partOfSpeech);


        //check button setting
        if (mEditedData == null) {
            isCorrect = false;
            isWrong = false;
        } else {
            isCorrect = mEditedData.isCorrect();
            isWrong = mEditedData.isWrong();
        }

        if (isCorrect) {
            mWordText.setTextColor(Color.parseColor("#e103c6a9"));
        } else if (isWrong) {
            mWordText.setTextColor(Color.parseColor("#dcf92d2d"));
        } else {
            mWordText.setTextColor(Color.parseColor("#020202"));
        }

        // set next and previous buttons invisible here if the next or previous words don't exist
        if (mCategoryIntent != null) {
            mNextWordData = mRealm.where(Word.class).equalTo("category", mCategory).lessThan("id", mWordId).findFirst(); //前の言葉のデータを取得
            mPreviousWordData = mRealm.where(Word.class).equalTo("category", mCategory).greaterThan("id", mWordId).findFirst();  //次の言葉のデータを取得
            if (mNextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }
            if (mPreviousWordData == null) {
                mBackButton.setVisibility(View.GONE);
            }
        } else if (mPartOfSpeechIntent != null) {
            mPreviousWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).greaterThan("id", mWordId).findFirst(); //次の言葉のデータを取得
            mNextWordData = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).lessThan("id", mWordId).findFirst(); //前の言葉
            if (mNextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }
            if (mPreviousWordData == null) {
                mBackButton.setVisibility(View.GONE);
            }
        } else {
            mPreviousWordData = mRealm.where(Word.class).greaterThan("id", mWordId).findFirst(); //次の言葉のデータを取得
            mNextWordData = mRealm.where(Word.class).lessThan("id", mWordId).findFirst(); //前の言葉
            if (mNextWordData == null) {
                mNextButton.setVisibility(View.GONE);
            }
            if (mPreviousWordData == null) {
                mBackButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.meaningButton:
                mMeaningButton.setVisibility(View.GONE);
                mConcealButton.setVisibility(View.VISIBLE);
                break;

            case R.id.concealButton:
                mMeaningButton.setVisibility(View.VISIBLE);
                mConcealButton.setVisibility(View.GONE);
                mHearingButton.setVisibility(View.VISIBLE);
                if(mIsAutoPlay&&mIsReverse){
                    speech();
                }
                break;

            case R.id.hearingButton:
                speech();
                break;

            case R.id.nextButton:
                setNextWord();
                mMeaningButton.setVisibility(View.GONE);
                mConcealButton.setVisibility(View.VISIBLE);
                if (mNextWordData == null) {
                    mNextButton.setVisibility(View.GONE);
                }
                if (mIsReverse) {
                    mHearingButton.setVisibility(View.GONE);
                }
                mBackButton.setVisibility(View.VISIBLE);

                //boolean data
                mEditedData = mRealm.where(EditedData.class).equalTo("id", mWordId).findFirst();
                if (mEditedData == null) {
                    isCorrect = false;
                    isWrong = false;
                } else {
                    isCorrect = mEditedData.isCorrect();
                    isWrong = mEditedData.isWrong();
                }
                if (isCorrect) {
                    mWordText.setTextColor(Color.parseColor("#e103c6a9"));
                } else if (isWrong) {
                    mWordText.setTextColor(Color.parseColor("#dcf92d2d"));
                } else {
                    mWordText.setTextColor(Color.parseColor("#020202"));
                }

                if(mIsAutoPlay&&!mIsReverse){
                    speech();
                }

                break;

            case R.id.backButton:
                setPreviousWord();
                mMeaningButton.setVisibility(View.GONE);
                mConcealButton.setVisibility(View.VISIBLE);
                mNextButton.setVisibility(View.VISIBLE);
                if (mPreviousWordData == null) {
                    mBackButton.setVisibility(View.GONE);
                }
                if (mIsReverse) {
                    mHearingButton.setVisibility(View.GONE);
                }

                //boolean data
                mEditedData = mRealm.where(EditedData.class).equalTo("id", mWordId).findFirst();
                if (mEditedData == null) {
                    isCorrect = false;
                    isWrong = false;
                } else {
                    isCorrect = mEditedData.isCorrect();
                    isWrong = mEditedData.isWrong();
                }
                if (isCorrect) {
                    mWordText.setTextColor(Color.parseColor("#e103c6a9"));
                } else if (isWrong) {
                    mWordText.setTextColor(Color.parseColor("#dcf92d2d"));
                } else {
                    mWordText.setTextColor(Color.parseColor("#020202"));
                }

                if(mIsAutoPlay&&!mIsReverse){
                    speech();
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search_view);
        item.setVisible(false);
        MenuItem wordCard = menu.findItem(R.id.wordCard);
        wordCard.setVisible(false);
        MenuItem list = menu.findItem(R.id.list);
        MenuItem setting = menu.findItem(R.id.setting);
        list.setVisible(false);
        setting.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onInit(int status) {
        Log.d("Speech", "OnInit - Status [" + status + "]");

        if (status == TextToSpeech.SUCCESS) {
            Log.d("Speech", "Success!");
            engine.setLanguage(Locale.FRENCH);
            if(mIsAutoPlay&&!mIsReverse){
                speech();
            }
        }
    }

    private void speech() {
        engine.speak(mWord, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void setNextWord() {
        //次に表示される単語情報
        if (mCategoryIntent != null) { //カテゴリーボタンを押したときの変遷の場合
            mWordData = mRealm.where(Word.class)
                    .equalTo("category", mCategory)
                    .lessThan("id", mWordId)
                    .findAll()
                    .last();
            mWordId = mWordData.getId();
            mCategory = mWordData.getCategory();
            mNextWordData = mRealm.where(Word.class)
                    .equalTo("category", mCategory)
                    .lessThan("id", mWordId)
                    .findFirst();
        } else if (mPartOfSpeechIntent != null) { //品詞ボタンのときの変遷の場合
            mWordData = mRealm.where(Word.class)
                    .equalTo("partOfSpeech", mPartOfSpeech)
                    .lessThan("id", mWordId)
                    .findAll()
                    .last();
            mWordId = mWordData.getId();
            mPartOfSpeech = mWordData.getPartOfSpeech();
            mNextWordData = mRealm.where(Word.class)
                    .equalTo("partOfSpeech", mPartOfSpeech)
                    .lessThan("id", mWordId)
                    .findFirst();
        } else { //一覧からの場合
            mWordData = mRealm.where(Word.class)
                    .lessThan("id", mWordId)
                    .findAll()
                    .last();
            mWordId = mWordData.getId();
            mNextWordData = mRealm.where(Word.class)
                    .lessThan("id", mWordId)
                    .findFirst();
        }

        mWord = mWordData.getWord();
        mMeaning = mWordData.getMeaning();
        mPartOfSpeech = mWordData.getPartOfSpeech();
        mCategory = mWordData.getCategory();

        //set text
        if (mIsReverse) {
            mWordText.setText(mMeaning);
            mMeaningButton.setText(mWord);
        } else {
            mWordText.setText(mWord);
            mMeaningButton.setText(mMeaning);
        }
        mCategoryText.setText(mCategory);
        mPartOfSpeechText.setText(mPartOfSpeech);
    }

    private void setPreviousWord() {
        //前に表示される単語情報
        if (mCategoryIntent != null) {
            mWordData = mRealm.where(Word.class)
                    .equalTo("category", mCategory)
                    .greaterThan("id", mWordId)
                    .findFirst();  //前の言葉のデータを取得
            mWordId = mWordData.getId();
            mCategory = mWordData.getCategory();
            mPreviousWordData = mRealm.where(Word.class)
                    .equalTo("category", mCategory)
                    .greaterThan("id", mWordId)
                    .findFirst();
            mWord = mWordData.getWord();
            mMeaning = mWordData.getMeaning();
            mPartOfSpeech = mWordData.getPartOfSpeech();
        } else if (mPartOfSpeechIntent != null) {
            mWordData = mRealm.where(Word.class)
                    .equalTo("partOfSpeech", mPartOfSpeech)
                    .greaterThan("id", mWordId)
                    .findFirst();  //前の言葉のデータを取得
            mWordId = mWordData.getId();
            mPartOfSpeech = mWordData.getPartOfSpeech();
            mPreviousWordData = mRealm.where(Word.class)
                    .equalTo("partOfSpeech", mCategory)
                    .greaterThan("id", mWordId)
                    .findFirst();

            mWord = mWordData.getWord();
            mMeaning = mWordData.getMeaning();
            mCategory = mWordData.getCategory();

        } else {

            mWordData = mRealm.where(Word.class)
                    .greaterThan("id", mWordId)
                    .findFirst();
            mWordId = mWordData.getId();
            mPreviousWordData = mRealm.where(Word.class)
                    .greaterThan("id", mWordId)
                    .findFirst();
            mWord = mWordData.getWord();
            mMeaning = mWordData.getMeaning();
            mCategory = mWordData.getCategory();
            mPartOfSpeech = mWordData.getPartOfSpeech();
        }

        //set text
        if (mIsReverse) {
            mWordText.setText(mMeaning);
            mMeaningButton.setText(mWord);
        } else {
            mWordText.setText(mWord);
            mMeaningButton.setText(mMeaning);
        }
        mCategoryText.setText(mCategory);
        mPartOfSpeechText.setText(mPartOfSpeech);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.shutdown();
        mRealm.close();
    }
}