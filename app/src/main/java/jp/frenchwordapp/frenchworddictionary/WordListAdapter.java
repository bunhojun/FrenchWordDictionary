package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by ponnp on 17/04/2018.
 */

public class WordListAdapter extends ArrayAdapter<Word> {
    private List<Word> mWordList;
    private LayoutInflater mLayoutInflater;
    private Filter filter;

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
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView1 = convertView.findViewById(android.R.id.text1);

        textView1.setText(mWordList.get(position).getWord());


        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SampleFilter();
        }

        // 自前なFilterオブジェクトを返すようにする

        return filter;
    }

    private class SampleFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            //List<String> filterItems = new ArrayList<String>();
            SortedSet<Word> filterItems = new TreeSet<Word>();

            // ここらでクエリーからフィルターして(filterItemsに)データ突っ込む

            results.values = filterItems;
            results.count = filterItems.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            @SuppressWarnings("unchecked")
            SortedSet<Word> filters = (SortedSet<Word>) results.values;

            notifyDataSetChanged();
            clear(); // 一度データ削除する

            // フィルターしたデータを突っ込む

            for (Word filter : filters) {
                add(filter);
            }
        }
    }

}
