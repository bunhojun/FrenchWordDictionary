package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

import io.realm.Realm;

public class TestActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    String levelIntent, partOfSpeechIntent;
    Word wordData;
    EditedData editedData;
    String word, level, partOfSpeech, meaning;
    int wordId;
    boolean isCorrect, isWrong;
    Button circleButton, crossButton;
    ImageButton hearingButton;
    TextView wordText;

    private TextView timerText, countText;
    private TextToSpeech engine;

    // 5秒= 5x1000 = 5000 msec
    long countNumber = 5000;
    // インターバル msec
    long interval = 10;
    CountDown countDown = new CountDown(countNumber, interval);

    SharedPreferences mSetting;
    Boolean isRandom, isReverse, isSortWrong, isAutoPlay;

    Realm realm = Realm.getDefaultInstance();
    int allSize = realm.where(Word.class).findAll().size();
    boolean num[] = new boolean[allSize];
    int i, count = 1, numberOfWrong, levelSize, partOfSpeechSize, allWordSize;

    private SimpleDateFormat dataFormat =
            new SimpleDateFormat("ss.SS", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getSupportActionBar().hide(); // no action bar

        //setting
        mSetting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        isRandom = mSetting.getBoolean("random", false);
        isReverse = mSetting.getBoolean("reverse", false);
        isSortWrong = mSetting.getBoolean("onlyWrong", false);
        isAutoPlay = mSetting.getBoolean("autoPlay", false);

        //intent
        Intent intent = getIntent();
        levelIntent = intent.getStringExtra("LEVEL");
        partOfSpeechIntent = intent.getStringExtra("HINSHI");
        wordId = intent.getIntExtra("WORD ID", -1);

        //音声読み上げのインスタンス
        engine = new TextToSpeech(this, this);

        //the numbers of words
        if (isSortWrong) {
            levelSize = realm.where(Word.class)
                    .equalTo("level", levelIntent)
                    .findAll()
                    .size()
                    - realm.where(EditedData.class).equalTo("level", levelIntent)
                    .equalTo("correct", true)
                    .findAll()
                    .size();
            partOfSpeechSize = realm.where(Word.class)
                    .equalTo("partOfSpeech", partOfSpeechIntent)
                    .findAll()
                    .size()
                    - realm.where(EditedData.class)
                    .equalTo("partOfSpeech", partOfSpeechIntent)
                    .equalTo("correct", true)
                    .findAll()
                    .size();
            allWordSize = realm.where(Word.class)
                    .findAll()
                    .size()
                    - realm.where(EditedData.class)
                    .equalTo("correct", true)
                    .findAll()
                    .size();
        } else {
            levelSize = realm.where(Word.class)
                    .equalTo("level", levelIntent)
                    .findAll()
                    .size();
            partOfSpeechSize = realm.where(Word.class)
                    .equalTo("partOfSpeech", partOfSpeechIntent)
                    .findAll()
                    .size();
            allWordSize = realm.where(Word.class)
                    .findAll()
                    .size();
        }

        // すべての重複判定用配列をfalseにしておく
        for (int i = 0; i < allSize; i++) {
            num[i] = false;
        }
        num[wordId] = true; //initial word id has to be set "used"

        //最初に表示される単語情報
        wordData = realm.where(Word.class).equalTo("id", wordId).findFirst();
        word = wordData.getWord();
        level = wordData.getLevel();
        partOfSpeech = wordData.getPartOfSpeech();
        meaning = wordData.getMeaning();

        //EditedData
        editedData = realm.where(EditedData.class).equalTo("id", wordId).findFirst();
        if (editedData == null) {
            isCorrect = false;
            isWrong = false;
        } else {
            isCorrect = editedData.isCorrect();
            isWrong = editedData.isWrong();
        }

        //text and button setting
        final Button openingButton = findViewById(R.id.openingButton);
        Typeface typeface1 = Typeface.createFromAsset(TestActivity.this.getAssets(), "NagomiGokubosoGothic-ExtraLight.otf");
        openingButton.setTypeface(typeface1);
        if (levelIntent != null) {
            openingButton.setText(levelIntent + " のテストを始める");
        } else if (partOfSpeechIntent != null) {
            openingButton.setText(partOfSpeechIntent + " のテストを始める");
        } else {
            openingButton.setText("すべての単語・表現のテストを始める");
        }
        countText = findViewById(R.id.countText);
        if (levelIntent != null) {
            countText.setText(count + "/" + levelSize);
        } else if (partOfSpeechIntent != null) {
            countText.setText(count + "/" + partOfSpeechSize);
        } else {
            countText.setText(count + "/" + allWordSize);
        }
        timerText = findViewById(R.id.timerText);
        final TextView levelText = findViewById(R.id.levelText);
        levelText.setText(level);
        final TextView partOfSpeechText = findViewById(R.id.partOfSpeechText);
        partOfSpeechText.setText(partOfSpeech);
        wordText = findViewById(R.id.wordText);
        wordText.setVisibility(View.GONE);
        if (isCorrect) {
            wordText.setTextColor(Color.parseColor("#e103c6a9"));
        } else if (isWrong) {
            wordText.setTextColor(Color.parseColor("#dcf92d2d"));
        }
        final TextView meaningText = findViewById(R.id.meaningText);
        if (isReverse) {
            wordText.setText(meaning);
            meaningText.setText(word);
        } else {
            wordText.setText(word);
            meaningText.setText(meaning);
        }
        meaningText.setVisibility(View.GONE);
        final Button concealButton = findViewById(R.id.concealButton);
        concealButton.setVisibility(View.GONE);
        circleButton = findViewById(R.id.circleButton);
        circleButton.setVisibility(View.GONE);
        circleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.beginTransaction();
                if (editedData == null) {
                    editedData = new EditedData();
                    editedData.setId(wordId);
                }
                editedData.setCorrect(true);
                editedData.setWrong(false);
                editedData.setLevel(level);
                editedData.setPartOfSpeech(partOfSpeech);
                realm.copyToRealmOrUpdate(editedData);
                realm.commitTransaction();
                wordText.setTextColor(Color.parseColor("#e103c6a9"));
            }
        });
        crossButton = findViewById(R.id.crossButton);
        crossButton.setVisibility(View.GONE);
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.beginTransaction();
                if (editedData == null) {
                    editedData = new EditedData();
                    editedData.setId(wordId);
                }
                editedData.setCorrect(false);
                editedData.setWrong(true);
                editedData.setLevel(level);
                editedData.setPartOfSpeech(partOfSpeech);
                realm.copyToRealmOrUpdate(editedData);
                realm.commitTransaction();
                wordText.setTextColor(Color.parseColor("#dcf92d2d"));
            }
        });

        hearingButton = findViewById(R.id.hearingButton);
        if (isReverse) {
            hearingButton.setVisibility(View.GONE);
        }
        hearingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech();
            }
        });

        final ImageButton nextButton = findViewById(R.id.nextButton);
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (levelIntent != null) {
                    if (count != levelSize) {
                        if (isRandom) {  //when random checked
                            for (int i = 0; i < allSize; ) {
                                Random rand = new Random(); //ランダムな数値の初期値
                                int randomNumber = rand.nextInt(allSize);
                                if (!num[randomNumber]) {
                                    if (isSortWrong) {
                                        wordData = realm.where(Word.class)
                                                .equalTo("level", level)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) {
                                            editedData = realm.where(EditedData.class)
                                                    .equalTo("id", randomNumber)
                                                    .equalTo("correct", true)
                                                    .findFirst();
                                            if (editedData == null) { //if a wrong or unanswered word is found
                                                num[randomNumber] = true;
                                                i = allSize;
                                            } else {
                                                num[randomNumber] = true;
                                                i++;
                                            }
                                        } else { //wordData == null
                                            num[randomNumber] = true;
                                            i++;
                                        }
                                    } else { // when wrong words not sorted
                                        wordData = realm.where(Word.class)
                                                .equalTo("level", level)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) { //when word data exists
                                            i = allSize;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                            }
                        } else {         //when random not checked
                            if (isSortWrong) {
                                for (int i = wordId - 1; i > -1; ) {
                                    wordData = realm.where(Word.class)
                                            .equalTo("level", level)
                                            .equalTo("id", i)
                                            .findFirst();
                                    if (wordData != null) {
                                        editedData = realm.where(EditedData.class)
                                                .equalTo("id", i)
                                                .equalTo("correct", true)
                                                .findFirst();
                                        if (editedData == null) {
                                            i = -1;
                                        } else { //if the word is a correct word
                                            i--;
                                        }
                                    } else { //if the word doesn't exist
                                        i--;
                                    }
                                }
                            } else { //when not sorted
                                wordData = realm.where(Word.class)
                                        .lessThan("id", wordId)
                                        .equalTo("level", level)
                                        .findAll()
                                        .last();
                            }
                        }
                        count++; //add 1 to count to know the position of a chosen word
                        countText.setText(count + "/" + levelSize);
                    } else { // after the last word
                        if (isSortWrong) {
                            numberOfWrong = realm.where(Word.class)
                                    .equalTo("level", levelIntent)
                                    .findAll()
                                    .size()
                            - realm.where(EditedData.class)
                                    .equalTo("level", levelIntent)
                                    .equalTo("correct", true)
                                    .findAll()
                                    .size();
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + (levelSize - numberOfWrong) + "/" + levelSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        } else {
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + realm.where(EditedData.class)
                                            .equalTo("level", level)
                                            .equalTo("correct", true)
                                            .findAll().size() + "/" + levelSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        }

                        return;
                    }
                } else if (partOfSpeechIntent != null) {
                    if (count != partOfSpeechSize) {
                        if (isRandom) {  //when random checked
                            for (int i = 0; i < allSize; ) {
                                Random rand = new Random(); //ランダムな数値の初期値
                                int randomNumber = rand.nextInt(allSize);
                                if (!num[randomNumber]) {
                                    if (isSortWrong) {
                                        wordData = realm.where(Word.class)
                                                .equalTo("partOfSpeech", partOfSpeech)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) {
                                            editedData = realm.where(EditedData.class)
                                                    .equalTo("id", randomNumber)
                                                    .equalTo("correct", true)
                                                    .findFirst();
                                            if (editedData == null) { //if a wrong or unanswered word is found
                                                num[randomNumber] = true;
                                                i = allSize;
                                            } else {
                                                num[randomNumber] = true;
                                                i++;
                                            }
                                        } else { //wordData == null
                                            num[randomNumber] = true;
                                            i++;
                                        }
                                    } else { // when wrong words not sorted
                                        wordData = realm.where(Word.class)
                                                .equalTo("partOfSpeech", partOfSpeech)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) { //when word data exists
                                            i = allSize;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                            }
                        } else {         //when random not checked
                            if (isSortWrong) {
                                for (int i = wordId - 1; i > -1; ) {
                                    wordData = realm.where(Word.class)
                                            .equalTo("partOfSpeech", partOfSpeech)
                                            .equalTo("id", i)
                                            .findFirst();
                                    if (wordData != null) {
                                        editedData = realm.where(EditedData.class)
                                                .equalTo("id", i)
                                                .equalTo("correct", true)
                                                .findFirst();
                                        if (editedData == null) {
                                            i = -1;
                                        } else { //if the word is a correct word
                                            i--;
                                        }
                                    } else { //if the word doesn't exist
                                        i--;
                                    }
                                }
                            } else { //when not sorted
                                wordData = realm.where(Word.class)
                                        .equalTo("partOfSpeech", partOfSpeech)
                                        .lessThan("id", wordId)
                                        .findAll()
                                        .last();
                            }
                        }
                        count++;
                        countText.setText(count + "/" + partOfSpeechSize);
                    } else { //after the last question
                        if (isSortWrong) {
                            numberOfWrong = realm.where(EditedData.class)
                                    .notEqualTo("correct", true)
                                    .equalTo("partOfSpeech", partOfSpeechIntent)
                                    .findAll()
                                    .size();
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + (partOfSpeechSize - numberOfWrong) + "/" + partOfSpeechSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        } else {
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + realm.where(EditedData.class)
                                            .equalTo("partOfSpeech", partOfSpeech)
                                            .equalTo("correct", true)
                                            .findAll().size() + "/" + partOfSpeechSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        }
                        return;
                    }
                } else {  //all the words
                    if (count != allWordSize) {
                        if (isRandom) {  //when random checked
                            for (int i = 0; i < allSize; ) {
                                Random rand = new Random(); //ランダムな数値の初期値
                                int randomNumber = rand.nextInt(allSize);
                                if (!num[randomNumber]) {
                                    if (isSortWrong) {
                                        wordData = realm.where(Word.class)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) {
                                            editedData = realm.where(EditedData.class)
                                                    .equalTo("id", randomNumber)
                                                    .equalTo("correct", true)
                                                    .findFirst();
                                            if (editedData == null) { //if a wrong or unanswered word is found
                                                num[randomNumber] = true;
                                                i = allSize;
                                            } else {
                                                num[randomNumber] = true;
                                                i++;
                                            }
                                        } else { //wordData == null
                                            num[randomNumber] = true;
                                            i++;
                                        }
                                    } else { // when wrong words not sorted
                                        wordData = realm.where(Word.class)
                                                .equalTo("id", randomNumber)
                                                .findFirst();
                                        if (wordData != null) { //when word data exists
                                            i = allSize;
                                        } else {
                                            i++;
                                        }
                                    }
                                }
                            }
                        } else {         //when random not checked
                            if (isSortWrong) {
                                for (int i = wordId - 1; i > -1; ) {
                                    wordData = realm.where(Word.class)
                                            .equalTo("id", i)
                                            .findFirst();
                                    if (wordData != null) {
                                        editedData = realm.where(EditedData.class)
                                                .equalTo("id", i)
                                                .equalTo("correct", true)
                                                .findFirst();
                                        if (editedData == null) {
                                            i = -1;
                                        } else { //if the word is a correct word
                                            i--;
                                        }
                                    } else { //if the word doesn't exist
                                        i--;
                                    }
                                }
                            } else { //when not sorted
                                wordData = realm.where(Word.class)
                                        .lessThan("id", wordId)
                                        .findAll()
                                        .last();
                            }
                        }
                        count++;
                        countText.setText(count + "/" + String.valueOf(allWordSize));
                    } else { // after the last word
                        if (isSortWrong) {
                            numberOfWrong = realm.where(EditedData.class)
                                    .notEqualTo("correct", true)
                                    .findAll()
                                    .size();
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + (allWordSize - numberOfWrong) + "/" + allWordSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        } else {
                            // ダイアログを表示する
                            AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                            builder.setTitle("全問題の回答が終了")
                                    .setMessage("正解数: " + realm.where(EditedData.class)
                                            .equalTo("correct", true)
                                            .findAll().size() + "/" + allWordSize)
                                    .setNegativeButton("CLOSE", null);
                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();
                        }
                        return;
                    }
                }
                wordId = wordData.getId();
                word = wordData.getWord();
                meaning = wordData.getMeaning();
                level = wordData.getLevel();
                partOfSpeech = wordData.getPartOfSpeech();
                //boolean data
                editedData = realm.where(EditedData.class).equalTo("id", wordId).findFirst();
                if (editedData == null) {
                    isCorrect = false;
                    isWrong = false;
                } else {
                    isCorrect = editedData.isCorrect();
                    isWrong = editedData.isWrong();
                }
                if (isCorrect) {
                    wordText.setTextColor(Color.parseColor("#e103c6a9"));
                } else if (isWrong) {
                    wordText.setTextColor(Color.parseColor("#dcf92d2d"));
                } else {
                    wordText.setTextColor(Color.parseColor("#020202"));
                }
                levelText.setText(level);
                partOfSpeechText.setText(partOfSpeech);
                if (isReverse) {
                    wordText.setText(meaning);
                    meaningText.setText(word);
                    hearingButton.setVisibility(View.GONE);
                } else {
                    wordText.setText(word);
                    meaningText.setText(meaning);
                }
                concealButton.setVisibility(View.VISIBLE);
                meaningText.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                circleButton.setVisibility(View.GONE);
                crossButton.setVisibility(View.GONE);
                timerText.setText(dataFormat.format(0));
                countDown.start();
                if (isAutoPlay && !isReverse) {
                    speech();
                }
            }
        });

        openingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openingButton.setVisibility(View.GONE);
                wordText.setVisibility(View.VISIBLE);
                concealButton.setVisibility(View.VISIBLE);
                if (isAutoPlay && !isReverse) {
                    speech();
                }
                countDown.start();
            }
        });

        concealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meaningText.setVisibility(View.VISIBLE);
                concealButton.setVisibility(View.GONE);
                circleButton.setVisibility(View.VISIBLE);
                crossButton.setVisibility(View.VISIBLE);
                hearingButton.setVisibility(View.VISIBLE);
                countDown.cancel();
                nextButton.setVisibility(View.VISIBLE);
                if (isAutoPlay && isReverse) {
                    speech();
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    class CountDown extends CountDownTimer {

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 完了
            timerText.setText(dataFormat.format(0));
            ImageButton nextButton = findViewById(R.id.nextButton);
            nextButton.setVisibility(View.VISIBLE);
            Button concealButton = findViewById(R.id.concealButton);
            concealButton.setVisibility(View.GONE);
            TextView meaningText = findViewById(R.id.meaningText);
            meaningText.setVisibility(View.VISIBLE);
            circleButton.setVisibility(View.VISIBLE);
            crossButton.setVisibility(View.VISIBLE);

            realm.beginTransaction();
            if (editedData == null) {
                editedData = new EditedData();
                editedData.setId(wordId);
            }
            editedData.setCorrect(false);
            editedData.setWrong(true);
            realm.copyToRealmOrUpdate(wordData);
            realm.commitTransaction();
            wordText.setTextColor(Color.parseColor("#dcf92d2d"));
        }

        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            timerText.setText(dataFormat.format(millisUntilFinished));
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
        engine.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        Log.d("Speech", "speak is on");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.shutdown();
        realm.close();
    }
}