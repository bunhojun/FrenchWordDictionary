package jp.frenchwordapp.frenchworddictionary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import android.support.design.widget.FloatingActionButton;
import android.widget.SearchView;

import java.util.List;


public class WordListActivity extends AppCompatActivity {

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            mWordAdapter.notifyDataSetChanged();
        }
    };
    private ListView mListView;
    private WordListAdapter mWordAdapter;
    private Word mWord;
    private Intent mIntent;
    private String mPartOfSpeech;
    private String mCategory;
    private RealmResults<Word> mWordRealmResults;
    private SearchView mSearchView;
    private View view;
    private List<Word> mWordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        mIntent = getIntent();
        mPartOfSpeech = mIntent.getStringExtra("PART OF SPEECH");
        mCategory = mIntent.getStringExtra("CATEGORY");

        // ListViewの設定
        mListView = (ListView) findViewById(R.id.listView1);

        // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
        RealmResults<Word> wordRealmResults = mRealm.where(Word.class).findAllSorted("id", Sort.ASCENDING);
        //Listを定義
        mWordList = mRealm.copyFromRealm(wordRealmResults);
        mWordAdapter = new WordListAdapter(WordListActivity.this, android.R.layout.simple_list_item_1, mWordList);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 単語カードへ変遷
                mWord = (Word) parent.getAdapter().getItem(position);

                Intent intent = new Intent(WordListActivity.this, WordCardActivity.class);
                intent.putExtra("EXTRA_WORD", mWord.getId());

                //if(mCategory != null){
                   intent.putExtra("CATEGORY",mCategory);
                //}
                //if(mPartOfSpeech){
                   intent.putExtra("HINSHI",mPartOfSpeech);
                // }


                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //todo add warning, never show again 単語を削除する

                return true;
            }
        });


        if(mPartOfSpeech != null || mCategory != null) {
            reloadSortedView();
        }else {
            reloadListView();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //todo 登録
                Intent intent = new Intent(WordListActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void reloadListView() {

        // 上記の結果を、TaskList としてセットする
        mWordAdapter.setWordList(mWordList);
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mWordAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mWordAdapter.notifyDataSetChanged();
    }

    private void reloadSortedView() {

        if(mPartOfSpeech != null) {
            // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
            mWordRealmResults = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).findAll();
        }else if(mCategory != null){
            // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
            mWordRealmResults = mRealm.where(Word.class).equalTo("category", mCategory).findAll();
        }
        mWordList = mRealm.copyFromRealm(mWordRealmResults);
        // 上記の結果を、TaskList としてセットする
        mWordAdapter.setWordList(mWordList);
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mWordAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mWordAdapter.notifyDataSetChanged();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(mPartOfSpeech != null) {
                    // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
                    mWordRealmResults = mRealm.where(Word.class).equalTo("partOfSpeech", mPartOfSpeech).contains("word", s, Case.INSENSITIVE).findAll();
                }else if(mCategory != null){
                    // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
                    mWordRealmResults = mRealm.where(Word.class).equalTo("category", mCategory).contains("word", s, Case.INSENSITIVE).findAll();
                }else{
                    // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
                    mWordRealmResults = mRealm.where(Word.class).contains("word", s, Case.INSENSITIVE).findAll();
                }
                mWordList = mRealm.copyFromRealm(mWordRealmResults);
                // 上記の結果を、TaskList としてセットする
                mWordAdapter.setWordList(mWordList);
                // TaskのListView用のアダプタに渡す
                mListView.setAdapter(mWordAdapter);
                // 表示を更新するために、アダプターにデータが変更されたことを知らせる
                mWordAdapter.notifyDataSetChanged();

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
