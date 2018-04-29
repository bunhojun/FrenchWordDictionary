package jp.frenchwordapp.frenchworddictionary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.realm.Realm;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    public static Boolean mIsRandom;
    Button mRandomButton;
    Button mUnRandomButton;
    Realm mRealm;
    Word mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mRandomButton = findViewById(R.id.randomButton);
        mRandomButton.setOnClickListener(this);
        mUnRandomButton = findViewById(R.id.unRandomButton);
        mUnRandomButton.setOnClickListener(this);
        mRealm.getDefaultInstance();
        mSetting = new Word();
        if(mIsRandom == null){ //未設定時
            mUnRandomButton.setVisibility(View.GONE);
            mRandomButton.setVisibility(View.VISIBLE);
        }else {
            mRandomButton.setVisibility(View.GONE);
            mUnRandomButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.randomButton:
                mIsRandom = true;
                mSetting.setIsRandom(mIsRandom);
                mRandomButton.setVisibility(View.GONE);
                mUnRandomButton.setVisibility(View.VISIBLE);
                break;
            case R.id.unRandomButton:
                mIsRandom = false;
                mSetting.setIsRandom(mIsRandom);
                mRandomButton.setVisibility(View.VISIBLE);
                mUnRandomButton.setVisibility(View.GONE);
        }
    }
}
