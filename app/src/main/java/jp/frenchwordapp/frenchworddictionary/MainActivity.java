package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Realm mRealm;

    Button mNounButton, mVerbButton, mAdjectiveButton, mAdverbButton, mPrepositionButton, mNumberButton,
    mOtherButton, mLevel1Button, mLevel2Button, mLevel3Button, mLevel4Button, mLevel5Button, mLevel6Button, mYourWordsButton;

    private Intent mListIntent; //メンバ変数にしておく

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Realm
        mRealm = Realm.getDefaultInstance();

        //read svc file
        reader(MainActivity.this);

        TextView textView1 = findViewById(R.id.titleText4);
        Typeface typeface1 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        textView1.setTypeface(typeface1);

        TextView textView2 = findViewById(R.id.titleText5);
        Typeface typeface2 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        textView2.setTypeface(typeface2);

        TextView textView3 = findViewById(R.id.titleText6);
        Typeface typeface3 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        textView3.setTypeface(typeface3);

        //button setting
        mNounButton = findViewById(R.id.nounButton);
        mNounButton.setOnClickListener(this);
        Typeface typeface4 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mNounButton.setTypeface(typeface4);
        mVerbButton = findViewById(R.id.verbButton);
        mVerbButton.setOnClickListener(this);
        Typeface typeface5 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mVerbButton.setTypeface(typeface5);
        mAdjectiveButton = findViewById(R.id.adjectiveButton);
        mAdjectiveButton.setOnClickListener(this);
        Typeface typeface6 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mAdjectiveButton.setTypeface(typeface6);
        mAdverbButton = findViewById(R.id.adverbButton);
        mAdverbButton.setOnClickListener(this);
        Typeface typeface7 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mAdverbButton.setTypeface(typeface7);
        mPrepositionButton = findViewById(R.id.prepositionButton);
        mPrepositionButton.setOnClickListener(this);
        Typeface typeface8 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mPrepositionButton.setTypeface(typeface8);
        mNumberButton = findViewById(R.id.numberButton);
        mNumberButton.setOnClickListener(this);
        Typeface typeface17 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mNumberButton.setTypeface(typeface17);
        mOtherButton = findViewById(R.id.otherButton);
        mOtherButton.setOnClickListener(this);
        Typeface typeface9 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mOtherButton.setTypeface(typeface9);
        mLevel1Button = findViewById(R.id.level1);
        mLevel1Button.setOnClickListener(this);
        Typeface typeface10 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel1Button.setTypeface(typeface10);
        mLevel2Button = findViewById(R.id.level2);
        mLevel2Button.setOnClickListener(this);
        Typeface typeface11 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel2Button.setTypeface(typeface11);
        mLevel3Button = findViewById(R.id.level3);
        mLevel3Button.setOnClickListener(this);
        Typeface typeface12 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel3Button.setTypeface(typeface12);
        mLevel4Button = findViewById(R.id.level4);
        mLevel4Button.setOnClickListener(this);
        Typeface typeface13 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel4Button.setTypeface(typeface13);
        mLevel5Button = findViewById(R.id.level5);
        mLevel5Button.setOnClickListener(this);
        Typeface typeface14 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel5Button.setTypeface(typeface14);
        mLevel6Button = findViewById(R.id.level6);
        mLevel6Button.setOnClickListener(this);
        Typeface typeface15 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mLevel6Button.setTypeface(typeface15);
        mYourWordsButton = findViewById(R.id.yourWords);
        mYourWordsButton.setOnClickListener(this);
        Typeface typeface16 = Typeface.createFromAsset(MainActivity.this.getAssets(),"NagomiGokubosoGothic-ExtraLight.otf");
        mYourWordsButton.setTypeface(typeface16);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
                //part of speech
            case R.id.nounButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "名詞");
                startActivity(mListIntent);
                break;
            case R.id.verbButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "動詞");
                startActivity(mListIntent);
                break;
            case R.id.adjectiveButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "形容詞");
                startActivity(mListIntent);
                break;
            case R.id.adverbButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "副詞");
                startActivity(mListIntent);
                break;
            case R.id.prepositionButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "前置詞");
                startActivity(mListIntent);
                break;
            case R.id.numberButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class); //must initialise intent here
                mListIntent.putExtra("PART OF SPEECH", "数・時");
                startActivity(mListIntent);
                break;
            case R.id.otherButton:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("PART OF SPEECH", "その他");
                startActivity(mListIntent);
                break;

                //category
            case R.id.level1:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル１");
                startActivity(mListIntent);
                break;
            case R.id.level2:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル２");
                startActivity(mListIntent);
                break;
            case R.id.level3:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル３");
                startActivity(mListIntent);
                break;
            case R.id.level4:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル４");
                startActivity(mListIntent);
                break;
            case R.id.level5:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル５");
                startActivity(mListIntent);
                break;
            case R.id.level6:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "レベル６");
                startActivity(mListIntent);
                break;
            case R.id.yourWords:
                mListIntent = new Intent(MainActivity.this, WordListActivity.class);
                mListIntent.putExtra("LEVEL", "あなたの言葉");
                startActivity(mListIntent);
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

        list.setOnMenuItemClickListener((new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent listIntent = new Intent(MainActivity.this, WordListActivity.class);
                startActivity(listIntent);
                return true;
            }
        }));

        setting.setOnMenuItemClickListener((new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(settingIntent);
                return true;
            }
        }));

        return super.onCreateOptionsMenu(menu);
    }

    public void reader(Context context) {
        AssetManager assetManager = context.getResources().getAssets();

        try {
            // CSVファイルの読み込み
            InputStream inputStream = assetManager.open("words.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferReader = new BufferedReader(inputStreamReader);

            String line;
           while ((line = bufferReader.readLine()) != null) {

                //カンマ区切りで１つづつ配列に入れる
                Word data = new Word();
               String[] RowData = line.split(",");
                //CSVの左([0]番目)から順番にセット
                data.setId(RowData[0]);
                data.setWord(RowData[1]);
                data.setMeaning(RowData[2]);
                data.setPartOfSpeech(RowData[3]);
                data.setLevel(RowData[4]);

                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(data);
                mRealm.commitTransaction();
            }
            bufferReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
