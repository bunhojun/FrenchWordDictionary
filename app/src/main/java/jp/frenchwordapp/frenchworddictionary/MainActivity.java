package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Realm mRealm;

    Button mButton1;
    Button mButton2;
    Button mButton3;
    Button mButton4;
    Button mButton5;
    Button mNounButton;
    Button mOtherButton;
    Button mLifeCategoryButton;
    Button mGreetingCategoryButton;

    private Word mCategory;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        reader(MainActivity.this);

        mButton1 = findViewById(R.id.wordCardButton);
        mButton4 = findViewById(R.id.searchListButton);
        mButton5 = findViewById(R.id.settingButton);
        mNounButton = findViewById(R.id.nounButton);
        mNounButton.setOnClickListener(this);
        mOtherButton = findViewById(R.id.otherButton);
        mOtherButton.setOnClickListener(this);
        mLifeCategoryButton = findViewById(R.id.lifeCategory);
        mLifeCategoryButton.setOnClickListener(this);
        mGreetingCategoryButton = findViewById(R.id.greetingCategory);
        mGreetingCategoryButton.setOnClickListener(this);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WordCardActivity.class);
            }
        });


        mButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WordListActivity.class);
                startActivity(intent);
            }
        });

        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {
        // Perform action on click
        switch(v.getId()) {
            case R.id.nounButton:
                mIntent = new Intent(MainActivity.this, WordListActivity.class);
                mIntent.putExtra("PART OF SPEECH", "名詞");
                startActivity(mIntent);
                break;

            case R.id.otherButton:
                mIntent = new Intent(MainActivity.this, WordListActivity.class);
                mIntent.putExtra("PART OF SPEECH", "その他");
                startActivity(mIntent);
                Log.d("debug", "hinshi on");
                break;

            case R.id.lifeCategory:
                mIntent = new Intent(MainActivity.this, WordListActivity.class);
                mIntent.putExtra("CATEGORY", "生活");
                startActivity(mIntent);
                Log.d("debug", "life on");
                break;

            case R.id.greetingCategory:
                mIntent = new Intent(MainActivity.this, WordListActivity.class);
                mIntent.putExtra("CATEGORY", "あいさつ");
                startActivity(mIntent);
                Log.d("debug", "greeting on");
                break;
        }
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
                data.setCategory(RowData[4]);

                mRealm = Realm.getDefaultInstance();
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
