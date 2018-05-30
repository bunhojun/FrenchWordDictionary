package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;


public class SettingActivity extends AppCompatActivity {

    SharedPreferences mSetting;
    SharedPreferences.Editor mEditor;
    Boolean mIsRandom, mIsReverse, mIsOnlyWrong;
    Switch mRandomSwitch, mReverseSwitch, mSortWrongSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mSetting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        mEditor = mSetting.edit();
        mIsRandom = mSetting.getBoolean("random", false);
        mIsReverse = mSetting.getBoolean("reverse", false);
        mIsOnlyWrong = mSetting.getBoolean("onlyWrong", false);

        mRandomSwitch = findViewById(R.id.randomSwitch);
        mRandomSwitch.setChecked(mSetting.getBoolean("random", false));
        mRandomSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    setRandom();
                } else {
                    setUnRandom();
                }
            }
        });

        mReverseSwitch = findViewById(R.id.reverseSwitch);
        mReverseSwitch.setChecked(mSetting.getBoolean("reverse", false));
        mReverseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    setReverse();
                } else {
                    setUnReverse();
                }
            }
        });

        mSortWrongSwitch = findViewById(R.id.sortWrongSwitch);
        mSortWrongSwitch.setChecked(mSetting.getBoolean("onlyWrong", false));
        mSortWrongSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    setSortWrong();
                } else {
                    setUnSortWrong();
                }
            }
        });
    }
    private void setRandom() {
        mEditor.putBoolean("random", true);
        mEditor.apply();
    }

    private void setUnRandom() {
        mEditor.putBoolean("random", false);
        mEditor.apply();
    }

    private void setReverse() {
        mEditor.putBoolean("reverse", true);
        mEditor.apply();
    }

    private void setUnReverse() {
        mEditor.putBoolean("reverse", false);
        mEditor.apply();
    }

    private void setSortWrong() {
        mEditor.putBoolean("onlyWrong", true);
        mEditor.apply();
    }

    private void setUnSortWrong() {
        mEditor.putBoolean("onlyWrong", false);
        mEditor.apply();
    }


}
