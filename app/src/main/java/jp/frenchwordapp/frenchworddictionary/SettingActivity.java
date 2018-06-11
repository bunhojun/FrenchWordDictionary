package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;


public class SettingActivity extends AppCompatActivity {

    SharedPreferences mSetting;
    SharedPreferences.Editor mEditor;
    Boolean mIsRandom, mIsReverse, mIsOnlyWrong, mIsAutoPlay;
    Switch mRandomSwitch, mReverseSwitch, mSortWrongSwitch, mAutoPlaySwitch;
    Button mResetButton;
    Realm mRealm = Realm.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mSetting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        mEditor = mSetting.edit();
        mIsRandom = mSetting.getBoolean("random", false);
        mIsReverse = mSetting.getBoolean("reverse", false);
        mIsOnlyWrong = mSetting.getBoolean("onlyWrong", false);
        mIsAutoPlay = mSetting.getBoolean("autoPlay", false);

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

        mAutoPlaySwitch = findViewById(R.id.autoPlaySwitch);
        mAutoPlaySwitch.setChecked(mSetting.getBoolean("autoPlay", false));
        mAutoPlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    setAutoPlay();
                } else {
                    setUnAutoPlay();
                }
            }
        });

        mResetButton = findViewById(R.id.resetButton);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("データリセット")
                        .setMessage("正解・不正解のデータをリセットしますか？")
                        .setPositiveButton("許可", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RealmResults<EditedData> editedResults = mRealm.where(EditedData.class).findAll();
                                if(editedResults!=null){
                                    mRealm.beginTransaction();
                                    editedResults.deleteAllFromRealm();
                                    mRealm.copyToRealmOrUpdate(editedResults);
                                    mRealm.commitTransaction();
                                }
                                Toast.makeText(SettingActivity.this,
                                        "正解・不正解のデータをリセットしました",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();
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

    private void setAutoPlay() {
        mEditor.putBoolean("autoPlay", true);
        mEditor.apply();
    }

    private void setUnAutoPlay() {
        mEditor.putBoolean("autoPlay", false);
        mEditor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

}
