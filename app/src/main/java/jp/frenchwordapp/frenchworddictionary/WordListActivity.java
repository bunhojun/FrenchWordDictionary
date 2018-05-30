package jp.frenchwordapp.frenchworddictionary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import android.support.design.widget.FloatingActionButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

public class WordListActivity extends AppCompatActivity {

    private Realm mRealm = Realm.getDefaultInstance();
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            mWordAdapter.notifyDataSetChanged();
            reloadListView();
        }
    };
    int allSize = mRealm.where(Word.class).findAll().size();
    private ListView mListView;
    private WordListAdapter mWordAdapter;
    private Word mWord, wordData;
    private EditedData editedData;
    private String mPartOfSpeech, mCategory;
    private RealmResults<Word> mWordRealmResults;
    private List<Word> mWordList;
    private AlertDialog.Builder mBuilder;
    private Boolean isRandom, isSortWrong;
    Button mRegisterButton, mEditButton, mCancelButton, mDeleteButton;
    SharedPreferences mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        //setting
        mSetting = getSharedPreferences("setting", Context.MODE_PRIVATE);
        isRandom = mSetting.getBoolean("random", false);
        isSortWrong = mSetting.getBoolean("onlyWrong", false);
        //realm
        mRealm.addChangeListener(mRealmListener);

        //intent
        Intent intent = getIntent();
        mPartOfSpeech = intent.getStringExtra("PART OF SPEECH");
        mCategory = intent.getStringExtra("CATEGORY");

        //TextView setting
        TextView textView = findViewById(R.id.textView);
        if (mCategory != null) {
            textView.setText("カテゴリー:" + mCategory);
        } else if (mPartOfSpeech != null) {
            textView.setText("品詞:" + mPartOfSpeech);
        } else {
            textView.setText("すべての単語・表現");
        }
        Typeface typeface = Typeface.createFromAsset(WordListActivity.this.getAssets(), "NagomiGokubosoGothic-ExtraLight.otf");
        textView.setTypeface(typeface);

        // ListViewの設定
        mListView = findViewById(R.id.listView1);
        // Realmデータベースから、「全てのデータを取得して登録順に並べた結果」を取得
        RealmResults<Word> wordRealmResults = mRealm.where(Word.class).findAllSorted("id", Sort.ASCENDING);
        //Listを定義しアダプターにいれる
        mWordList = mRealm.copyFromRealm(wordRealmResults);
        mWordAdapter = new WordListAdapter(WordListActivity.this, android.R.layout.simple_list_item_1, mWordList);

        //show list
        reloadListView();

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 単語カードへ変遷
                mWord = (Word) parent.getAdapter().getItem(position);
                Intent intent = new Intent(WordListActivity.this, WordCardActivity.class);
                intent.putExtra("EXTRA_WORD", mWord.getId());
                intent.putExtra("CATEGORY", mCategory);
                intent.putExtra("HINSHI", mPartOfSpeech);
                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Word word = (Word) parent.getAdapter().getItem(position);
                showEditDialog(word);
                return true;
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });
    }

    private void reloadListView() {

        if (mPartOfSpeech != null) {   //品詞ボタンからの変遷
            mWordRealmResults = mRealm.where(Word.class)
                    .equalTo("partOfSpeech", mPartOfSpeech)
                    .findAllSorted("id", Sort.DESCENDING);

        } else if (mCategory != null) { //カテゴリーボタンからの変遷
            mWordRealmResults = mRealm.where(Word.class)
                    .equalTo("category", mCategory)
                    .findAllSorted("id", Sort.DESCENDING);

        } else {                        //一覧ボタンからの変遷
            mWordRealmResults = mRealm.where(Word.class)
                    .findAllSorted("id", Sort.DESCENDING);
        }
        mWordList = mRealm.copyFromRealm(mWordRealmResults);
        mWordAdapter.setWordList(mWordList);
        mListView.setAdapter(mWordAdapter);
        mWordAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search_view);
        final MenuItem wordCard = menu.findItem(R.id.wordCard);
        SearchView searchView = (SearchView) item.getActionView();
        MenuItem list = menu.findItem(R.id.list);
        list.setVisible(false);
        MenuItem setting = menu.findItem(R.id.setting);
        setting.setVisible(false);

        wordCard.setOnMenuItemClickListener((new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int wordId = sendWordData();
                Log.d("TAG", "word ID is " + String.valueOf(wordId));
                if (wordId == -1) {
                    // ダイアログを表示する
                    AlertDialog.Builder builder = new AlertDialog.Builder(WordListActivity.this);
                    builder.setTitle("エラー")
                            .setMessage("指定された条件では言葉が見つからずテストが行えません。設定画面で条件を変更してください。")
                            .setNegativeButton("CLOSE", null);
                    AlertDialog confirmDialog = builder.create();
                    confirmDialog.show();
                } else {
                    Intent intent = new Intent(WordListActivity.this, TestActivity.class);
                    intent.putExtra("WORD ID", wordId);
                    intent.putExtra("CATEGORY", mCategory);
                    intent.putExtra("HINSHI", mPartOfSpeech);
                    startActivity(intent);
                }
                return true;
            }
        }));

        // Detect SearchView icon clicks
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordCard.setVisible(false);
            }
        });
        // Detect SearchView close
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                wordCard.setVisible(true);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (mPartOfSpeech != null) {
                    mWordRealmResults = mRealm.where(Word.class)
                            .contains("word", s, Case.INSENSITIVE)
                            .equalTo("partOfSpeech", mPartOfSpeech)
                            .or()
                            .contains("meaning", s)
                            .equalTo("partOfSpeech", mPartOfSpeech)
                            .findAllSorted("id", Sort.DESCENDING);
                } else if (mCategory != null) {
                    mWordRealmResults = mRealm.where(Word.class)
                            .contains("word", s, Case.INSENSITIVE)
                            .equalTo("category", mCategory)
                            .or()
                            .contains("meaning", s)
                            .equalTo("category", mCategory)
                            .findAllSorted("id", Sort.DESCENDING);
                } else {
                    mWordRealmResults = mRealm.where(Word.class)
                            .contains("word", s, Case.INSENSITIVE)
                            .or()
                            .contains("meaning", s)
                            .findAllSorted("id", Sort.DESCENDING);
                }
                mWordList = mRealm.copyFromRealm(mWordRealmResults);
                mWordAdapter.setWordList(mWordList);
                mListView.setAdapter(mWordAdapter);
                mWordAdapter.notifyDataSetChanged();

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void showRegisterDialog() {
        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(
                R.layout.alert_dialog_custom,
                (ViewGroup) findViewById(R.id.dialogCustom));
        final Spinner categorySpinner = layout.findViewById(R.id.categorySpinner);
        final Spinner partOfSpeechSpinner = layout.findViewById(R.id.hinshiSpinner);

        if (mCategory != null) {  //todo add categories
            switch (mCategory) {
                case "生活":
                    categorySpinner.setSelection(1);
                    break;
                case "あいさつ":
                    categorySpinner.setSelection(2);
            }

        } else if (mPartOfSpeech != null) {
            switch (mPartOfSpeech) {
                case "名詞":
                    partOfSpeechSpinner.setSelection(1);
                    break;
                case "動詞":
                    partOfSpeechSpinner.setSelection(2);
                    break;
                case "形容詞":
                    partOfSpeechSpinner.setSelection(3);
                    break;
                case "副詞":
                    partOfSpeechSpinner.setSelection(4);
                    break;
                case "前置詞":
                    partOfSpeechSpinner.setSelection(5);
                    break;
                case "その他":
                    partOfSpeechSpinner.setSelection(6);
                    break;
            }
        }

        // アラートダイアログ を生成
        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setView(layout);

        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        //Button
        mRegisterButton = layout.findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when 登録 button pressed
                Word word = null;
                EditText wordEdit = layout.findViewById(R.id.wordEdit);
                String sentWord = wordEdit.getText().toString();
                EditText meaningEdit = layout.findViewById(R.id.meaningEdit);
                String sentMeaning = meaningEdit.getText().toString();
                String chosenCategory = categorySpinner.getSelectedItem().toString();
                String chosenPartOfSpeech = partOfSpeechSpinner.getSelectedItem().toString();
                if (sentWord.length() == 0 || sentMeaning.length() == 0 || chosenCategory.equals("--カテゴリ--") || chosenPartOfSpeech.equals("--品詞--")) {
                    Toast.makeText(WordListActivity.this, "単語名、意味、品詞、カテゴリをすべて入力してください", Toast.LENGTH_LONG).show();
                } else {
                    addOrEditWord(word, sentWord, sentMeaning, chosenCategory, chosenPartOfSpeech);
                    dialog.dismiss();
                }
            }
        });
        mCancelButton = layout.findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showEditDialog(final Word word) {
        // カスタムビューを設定
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(
                R.layout.alert_dialog_edit_custom,
                (ViewGroup) findViewById(R.id.dialogEditCustom));

        final EditText wordEdit = layout.findViewById(R.id.wordEdit);
        wordEdit.setText(word.getWord());       //長押しされた単語取得
        final EditText meaningEdit = layout.findViewById(R.id.meaningEdit);
        meaningEdit.setText(word.getMeaning()); //意味取得

        final Spinner categorySpinner = layout.findViewById(R.id.categorySpinner);
        final Spinner partOfSpeechSpinner = layout.findViewById(R.id.hinshiSpinner);
        if (mCategory != null) {
            switch (mCategory) {
                case "生活":
                    categorySpinner.setSelection(1);
                    break;
                case "あいさつ":
                    categorySpinner.setSelection(2);
                    break;
                case "基本単語":
                    categorySpinner.setSelection(3);
                    break;
                case "数量":
                    categorySpinner.setSelection(4);
                    break;
                case "その他":
                    categorySpinner.setSelection(5);
                    break;
            }
            String partOfSpeech = word.getPartOfSpeech();
            switch (partOfSpeech) {
                case "名詞":
                    partOfSpeechSpinner.setSelection(1);
                    break;
                case "動詞":
                    partOfSpeechSpinner.setSelection(2);
                    break;
                case "形容詞":
                    partOfSpeechSpinner.setSelection(3);
                    break;
                case "副詞":
                    partOfSpeechSpinner.setSelection(4);
                    break;
                case "前置詞":
                    partOfSpeechSpinner.setSelection(5);
                    break;
                case "その他":
                    partOfSpeechSpinner.setSelection(6);
                    break;
            }

        } else if (mPartOfSpeech != null) {
            switch (mPartOfSpeech) {
                case "名詞":
                    partOfSpeechSpinner.setSelection(1);
                    break;
                case "動詞":
                    partOfSpeechSpinner.setSelection(2);
                    break;
                case "形容詞":
                    partOfSpeechSpinner.setSelection(3);
                    break;
                case "副詞":
                    partOfSpeechSpinner.setSelection(4);
                    break;
                case "前置詞":
                    partOfSpeechSpinner.setSelection(5);
                    break;
                case "その他":
                    partOfSpeechSpinner.setSelection(6);
                    break;
            }
            String category = word.getCategory();
            switch (category) {
                case "生活":
                    categorySpinner.setSelection(1);
                    break;
                case "あいさつ":
                    categorySpinner.setSelection(2);
            }
        } else {
            //カテゴリースピナーのセット
            String category = word.getCategory();
            switch (category) {
                case "生活":
                    categorySpinner.setSelection(1);
                    break;
                case "あいさつ":
                    categorySpinner.setSelection(2);
            }
            //品詞スピナーのセット
            String partOfSpeech = word.getPartOfSpeech();
            switch (partOfSpeech) {
                case "名詞":
                    partOfSpeechSpinner.setSelection(1);
                    break;
                case "動詞":
                    partOfSpeechSpinner.setSelection(2);
                    break;
                case "形容詞":
                    partOfSpeechSpinner.setSelection(3);
                    break;
                case "副詞":
                    partOfSpeechSpinner.setSelection(4);
                    break;
                case "前置詞":
                    partOfSpeechSpinner.setSelection(5);
                    break;
                case "その他":
                    partOfSpeechSpinner.setSelection(6);
                    break;
            }
        }

        // アラートダイアログ を生成
        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setView(layout);

        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        //Button
        mEditButton = layout.findViewById(R.id.editButton);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 編集 button pressed
                String sentWord = wordEdit.getText().toString();
                String sentMeaning = meaningEdit.getText().toString();
                String chosenCategory = categorySpinner.getSelectedItem().toString();
                String chosenPartOfSpeech = partOfSpeechSpinner.getSelectedItem().toString();
                if (sentWord.isEmpty() || sentMeaning.isEmpty() || chosenCategory.equals("--カテゴリ--") || chosenPartOfSpeech.equals("--品詞--")) {
                    Toast.makeText(WordListActivity.this, "単語名、意味、品詞、カテゴリをすべて入力してください", Toast.LENGTH_LONG).show();
                } else {
                    addOrEditWord(word, sentWord, sentMeaning, chosenCategory, chosenPartOfSpeech);
                    dialog.dismiss();
                }
            }
        });
        mDeleteButton = layout.findViewById(R.id.deleteButton);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(WordListActivity.this);

                builder.setTitle("削除")
                        .setMessage(word.getWord() + "を削除しますか")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface deleteDialog, int which) {
                                        int wordId = word.getId();
                                        RealmResults<Word> results = mRealm.where(Word.class)
                                                .equalTo("id", wordId)
                                                .findAll();
                                        EditedData editedData = mRealm.where(EditedData.class).equalTo("id", wordId).findFirst();
                                        mRealm.beginTransaction();
                                        results.deleteAllFromRealm();
                                        editedData.deleteFromRealm();
                                        mRealm.commitTransaction();
                                        dialog.dismiss();
                                    }
                                }
                        ).setNegativeButton("CANCEL", null);
                AlertDialog confirmDialog = builder.create();
                confirmDialog.show();
            }
        });
    }

    private void showExistingWordAlertDialog(final Word newWord) {

        // アラートダイアログ を生成
        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setMessage("登録しようとしている言葉と同じ言葉が既に登録されています。登録しますか？")
                .setPositiveButton("登録", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRealm.beginTransaction();
                        mRealm.copyToRealmOrUpdate(newWord);
                        mRealm.commitTransaction();
                    }
                })
                .setNegativeButton("キャンセル", null);
        mBuilder.create();
        mBuilder.show();
    }

    private void addOrEditWord(Word word,
                               String sentWord,
                               String sentMeaning,
                               String chosenCategory,
                               String chosenPartOfSpeech
    ) {
        if (word == null) { //新規作成のとき
            Word newWord = new Word();
            RealmResults wordRealmResults = mRealm.where(Word.class).findAll();
            int intIdentifier = wordRealmResults.max("id").intValue() + 1;
            String identifier = String.valueOf(intIdentifier); //id
            //登録の準備
            newWord.setId(identifier);
            newWord.setWord(sentWord);
            newWord.setMeaning(sentMeaning);
            newWord.setCategory(chosenCategory);
            newWord.setPartOfSpeech(chosenPartOfSpeech);
            //既存の単語の判定をする
            final RealmResults<Word> existingWords = mRealm.where(Word.class)
                    .equalTo("word", sentWord, Case.INSENSITIVE)
                    .findAll();
            if (existingWords.size() >= 1) { //既存の単語かもしれないとき

                showExistingWordAlertDialog(newWord); //アラート表示

            } else { //既存の単語が存在しないとき
                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(newWord);
                mRealm.commitTransaction();
                Toast.makeText(WordListActivity.this, "新しい単語が追加されました", Toast.LENGTH_LONG).show();
            }
        } else { //編集の時
            word.setWord(sentWord);
            word.setMeaning(sentMeaning);
            word.setCategory(chosenCategory);
            word.setPartOfSpeech(chosenPartOfSpeech);
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(word);
            mRealm.commitTransaction();
            Toast.makeText(WordListActivity.this, "単語が編集されました", Toast.LENGTH_LONG).show();
        }
    }

    private int sendWordData() {
        int wordId;
        int allSize = mRealm.where(Word.class).findAll().size();
        boolean num[] = new boolean[allSize]; //重複判定用
        // すべての重複判定用配列をfalseにしておく
        for (int i = 0; i < allSize; i++) {
            num[i] = false;
        }

        // initial word data to send
        if (mCategory != null) {       //category
            if (isRandom) {  //when random checked
                for (int i = 0; i < allSize; ) {
                    Random rand = new Random(); //ランダムな数値の初期値
                    int randomNumber = rand.nextInt(allSize);
                    if (!num[randomNumber]) {
                        if (isSortWrong) {
                            editedData = mRealm.where(EditedData.class)
                                    .notEqualTo("correct", true)
                                    .equalTo("category", mCategory)
                                    .equalTo("id", randomNumber)
                                    .findFirst();
                            if (editedData != null) {
                                i = allSize;  //stop the loop
                            }
                        } else { // when wrong words not sorted
                            wordData = mRealm.where(Word.class)
                                    .equalTo("category", mCategory)
                                    .equalTo("id", randomNumber)
                                    .findFirst();
                            if (wordData != null) { //when word data exists
                                i = allSize;
                            }
                        }
                    }
                }
            } else {         //when random not checked
                if (isSortWrong) {
                    editedData = mRealm.where(EditedData.class)
                            .equalTo("category", mCategory)
                            .notEqualTo("correct", true)
                            .findFirst();
                } else {
                    wordData = mRealm.where(Word.class)
                            .equalTo("category", mCategory)
                            .findAll()
                            .last();
                }
            }
        } else if (mPartOfSpeech != null) {//part of speech
            if (isRandom) {  //when random checked
                for (int i = 0; i < allSize; ) {
                    Random rand = new Random(); //ランダムな数値の初期値
                    int randomNumber = rand.nextInt(allSize);
                    if (!num[randomNumber]) {
                        if (isSortWrong) {
                            editedData = mRealm.where(EditedData.class)
                                    .notEqualTo("correct", true)
                                    .equalTo("partOfSpeech", mPartOfSpeech)
                                    .equalTo("id", randomNumber)
                                    .findFirst();
                            if (editedData != null) {
                                i = allSize;
                            } else {
                                i++;
                            }
                        } else { // when wrong words not sorted
                            wordData = mRealm.where(Word.class)
                                    .equalTo("partOfSpeech", mPartOfSpeech)
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
                    editedData = mRealm.where(EditedData.class)
                            .equalTo("partOfSpeech", mPartOfSpeech)
                            .notEqualTo("correct", true)
                            .findFirst();
                } else {
                    wordData = mRealm.where(Word.class)
                            .equalTo("partOfSpeech", mPartOfSpeech)
                            .findAll()
                            .last();
                }
            }
        } else {     //all the words
            if (isRandom) {//when random checked
                for (int i = 0; i < allSize; ) {
                    Random rand = new Random(); //ランダムな数値の初期値
                    int randomNumber = rand.nextInt(allSize);
                    if (!num[randomNumber]) {
                        if (isSortWrong) {
                            editedData = mRealm.where(EditedData.class)
                                    .notEqualTo("correct", true)
                                    .equalTo("id", randomNumber)
                                    .findFirst();
                            if (editedData != null) {
                                i = allSize;
                            }
                        } else { // when wrong words not sorted
                            wordData = mRealm.where(Word.class)
                                    .equalTo("id", randomNumber)
                                    .findFirst();
                            if (wordData != null) { //when word data exists
                                i = allSize;
                            }
                        }
                    }
                }
            } else {      //when random not checked
                if (isSortWrong) {
                    editedData = mRealm.where(EditedData.class)
                            .notEqualTo("correct", true)
                            .findFirst(); // set findFirst here
                } else { //unconditioned
                    wordData = mRealm
                            .where(Word.class)
                            .findFirst(); //set findFirst to get null where necessary
                    if (wordData != null) {
                        wordData = mRealm
                                .where(Word.class)
                                .findAll()
                                .last();
                    }
                }
            }
        }

        if (wordData != null) {
            wordId = wordData.getId();
        } else if (editedData != null) {
            wordId = editedData.getId();
        } else {
            wordId = -1;
        }
        return wordId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

}