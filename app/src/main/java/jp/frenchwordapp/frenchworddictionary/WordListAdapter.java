package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.realm.Realm;

public class WordListAdapter extends ArrayAdapter<Word> {
    private List<Word> mWordList;
    private LayoutInflater mLayoutInflater;

    public WordListAdapter(Context context, int textViewResourceId, List<Word> mWordList) {
        super(context, textViewResourceId, mWordList);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setWordList(List<Word> wordList) {
        mWordList = wordList;
    }

    @Override
    public int getCount() {
        return mWordList.size();
    }

    @Override
    public Word getItem(int position) {
        return mWordList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SharedPreferences setting = getContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        Boolean isReverse = setting.getBoolean("reverse", false);

        EditedData editedData;
        Realm realm = Realm.getDefaultInstance();

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView1 = convertView.findViewById(android.R.id.text1);
        editedData = realm.where(EditedData.class)
                .equalTo("id", mWordList.get(position).getId())
                .findFirst();
        boolean isCorrect = false;
        boolean isWrong = false;
        if(editedData!=null){
            isCorrect = editedData.isCorrect();
            isWrong = editedData.isWrong();
        }

        if (isCorrect) {
            textView1.setTextColor(Color.parseColor("#e103c6a9"));
        } else if (isWrong) {
            textView1.setTextColor(Color.parseColor("#dcf92d2d"));
        } else{
            textView1.setTextColor(Color.parseColor("#020202"));
        }

        if(isReverse){
            textView1.setText(mWordList.get(position).getMeaning());
        }else {
            textView1.setText(mWordList.get(position).getWord());
        }
        return convertView;
    }
}