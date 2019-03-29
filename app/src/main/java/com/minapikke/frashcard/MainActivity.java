package com.minapikke.frashcard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;

import android.util.Log;


public class MainActivity extends AppCompatActivity {
    private final static String DB_NAME="cardTable2.db";
    private final static String DB_TABLE="CardTable";
    private final static int DB_VERSION=2;
    private SQLiteDatabase databaseObject;
    private SQLiteDatabase preDatabaseObject;
    private EditText editText;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private EditText editText5;
    private String subjectStr;
    private String tenseStr;

    private final static String DB_TABLE_PRE="preCardTable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // pre-made
        preMadeDatabase();

        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        editText5 = (EditText) findViewById(R.id.editText5);

        DatabaseHelper dbHelperObject =
                new DatabaseHelper(MainActivity.this);
        databaseObject =
                dbHelperObject.getWritableDatabase();

        databaseObject.execSQL(
                "CREATE TABLE IF NOT EXISTS " +
                        DB_TABLE +
                        "(id integer primary key autoincrement, word text, subject text, tense text, eng_example text, jap_example text)"
        );

        findViewById(R.id.button1)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    String word = editText.getText().toString();
                                    String eng_example = editText2.getText().toString();
                                    String jap_example = editText3.getText().toString();
                                    writeToDB(word,eng_example,jap_example);
                                } catch (Exception e) {
                                    showDialog(getApplicationContext(), "ERROR", "データの書き込みに失敗しました");
                                }
                            }
                        });

        findViewById(R.id.button2)
                .setOnClickListener(
                        new View.OnClickListener() {
                            // ListViewに表示するためのArrayAdapter
                            ArrayAdapter<String> ad;

                            @Override
                            public void onClick(View v) {
                                try {
                                    String id = editText4.getText().toString();
                                    String[] readString = readToDB(id);
                                    editText4.setText(readString[0]);
                                    editText5.setText(readString[1]+ readString[2]);
                                } catch (Exception e) {
                                    showDialog(getApplicationContext(), "ERROR", "データの読み込みに失敗しました");

                                }

                                try {
                                    String selectTable = "SELECT " + DB_TABLE + ".id, " + DB_TABLE + ".word, " + DB_TABLE + ".subject, " + DB_TABLE + ".tense, " + DB_TABLE + ".eng_example, " + DB_TABLE + ".jap_example FROM " + DB_TABLE +
                                            " UNION ALL " + "SELECT " + DB_TABLE_PRE + ".id, " + DB_TABLE_PRE + ".word, " + DB_TABLE_PRE + ".subject, " + DB_TABLE_PRE + ".tense, " + DB_TABLE_PRE + ".eng_example, " + DB_TABLE_PRE + ".jap_example FROM " + DB_TABLE_PRE;
                                    // クエリ実行
                                    Cursor cursor = preDatabaseObject.rawQuery(selectTable, null);
                                    // ListViewに表示するためのArrayAdapterのインスタンスを生成
                                    ad = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
                                    // '1','study', 'I', '現在・肯定', 'I study English every day.', '私は毎日英語を勉強する'  <- cursor
                                    // '2','work', 'I', '現在・肯定', 'I study English every day.', '私は毎日英語を勉強する'
                                    // '3','jump', 'I', '現在・肯定', 'I study English every day.', '私は毎日英語を勉強する'
                                    // '4','walk', 'I', '現在・肯定', 'I study English every day.', '私は毎日英語を勉強する'
                                    while(cursor.moveToNext()){
                                        // 各カラムの識別子（カラムID）を取得
                                        int idId = cursor.getColumnIndex("id");
                                        int idWord = cursor.getColumnIndex("word");
                                        int idSubject = cursor.getColumnIndex("subject");
                                        int idTense = cursor.getColumnIndex("tense");
                                        int idEngExample = cursor.getColumnIndex("eng_example");
                                        int idJapExample = cursor.getColumnIndex("jap_example");

                                        int id = cursor.getInt(idId);   // 1
                                        String word = cursor.getString(idWord);  // study
                                        String subject = cursor.getString(idSubject); // I
                                        String tense = cursor.getString(idTense); // 現在・肯定
                                        String eng_example = cursor.getString(idEngExample); // I study English every day.
                                        String jap_example = cursor.getString(idJapExample); // 私は毎日英語を勉強する

                                        String row = id + ":" + word + ":" + subject + ":" + tense + ":" + eng_example + ":" + jap_example;
                                        ad.add(row);
                                    }
                                }catch(Exception e){
                                    // データベースオブジェクトをクローズ
                                    preDatabaseObject.close();
                                }

                                ((ListView) findViewById(R.id.listView)).setAdapter(ad);
                            }
                        });

        Spinner spinner = findViewById(R.id.spinner1);

        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);

        // リスナーを登録
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                subjectStr = (String)spinner.getSelectedItem();
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }

    private void writeToDB(String word,String eng_example,String jap_example) throws Exception {
        ContentValues contentValObject = new ContentValues();
        //contentValObject.put("id","5");
        contentValObject.put("word",word);
        contentValObject.put("subject","I");
        contentValObject.put("tense","現在・肯定・基本");
        contentValObject.put("eng_example",eng_example);
        contentValObject.put("jap_example",jap_example);
        /*int numberOfColumns =
                databaseObject.update(
                        DB_TABLE,
                        contentValObject,
                        null,
                        null
               );*/
        // if( numberOfColumns == 0) {
        databaseObject.insert(
                DB_TABLE,
                null,
                contentValObject
        );
        // }
    }
    private String[] readToDB(String id) throws Exception {
        String[] valueCursor = new String[3];
        Cursor cursor = databaseObject.query(
                DB_TABLE,
                new String[] {"id","word","subject","tense","eng_example","jap_example"},
                "id='" + id + "'",
                //"id='5'",
                null,
                null,
                null,
                null
        );
        if(cursor.getCount() == 0) {
            throw new Exception();
        }
        cursor.moveToFirst();
        valueCursor[0] = cursor.getString(1);
        valueCursor[1] = cursor.getString(4);
        valueCursor[2] = cursor.getString(5);
        cursor.close();
        return valueCursor;
    }

    private static void showDialog( Context context, String title, String text) {
        AlertDialog.Builder varAlertDialog =
                new AlertDialog.Builder(context);
        varAlertDialog.setTitle(title);
        varAlertDialog.setMessage(text);
        varAlertDialog.setPositiveButton("OK", null);
        varAlertDialog.show();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(
                    context,DB_NAME,null,DB_VERSION
            );
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                            DB_TABLE +
                            "(id integer primary key autoincrement, word text, subject text, tense text, eng_example text, jap_example text)"
            );
            Log.d("Database","Create Table");
        }
        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion
        ) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
            onCreate(db);
        }
    }

    private void preMadeDatabase(){

        // pre-made
        PreDatabaseHelper preDbHelperObject = new PreDatabaseHelper(MainActivity.this);
        preDatabaseObject =
                preDbHelperObject.getWritableDatabase();

        // 古いテーブルを破棄するSQL
        String dropTable = "DROP TABLE IF EXISTS " + DB_TABLE_PRE;
        // テーブルを作成するSQL
        String createTable = "CREATE TABLE " + DB_TABLE_PRE +
                "(id integer primary key autoincrement, word text, subject text, tense text, eng_example text, jap_example text)";

        String[] insertData = {
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・肯定', 'I study English every day.', '私は毎日英語を勉強する')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・肯定', 'Do I study English every day?', '私は毎日英語を勉強する？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・肯定', 'What do I study every day?', '私は毎日何を勉強する？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・否定', 'I don’t study it at school.', '私はそれを学校で勉強しない')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・否定', 'Don’t I study it at school?', '私はそれを学校で勉強しない？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '現在・否定', 'Why don’t I study it at school?', '私はなぜそれを学校で勉強しない？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・肯定', 'I studied math last night.', '私は昨夜数学を勉強した')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・肯定', 'Did I study math last night?', '私は昨夜数学を勉強した？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・肯定', 'What did I study last night?', '私は昨夜何を勉強した？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・否定', 'I didn’t study it this morning.', '私は今朝それを勉強しなかった')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・否定', 'Didn’t I study it this morning?', '私は今朝それを勉強しなかった？')",
                "INSERT INTO " + DB_TABLE_PRE + "(word, subject, tense, eng_example, jap_example) VALUES ('study', 'I', '過去・否定', 'Why didn’t I study it this morning?', '私はなぜ今朝それを勉強しなかった？')"
        };

        // 古いテーブルを破棄
        preDatabaseObject.execSQL(dropTable);
        // テーブルを作成
        preDatabaseObject.execSQL(createTable);
        // データ登録
        for(int i = 0; i < insertData.length; i++){
            preDatabaseObject.execSQL(insertData[i]);
        }

        // データベースオブジェクトをクローズ
//        preDatabaseObject.close();
    }

    private static class PreDatabaseHelper extends SQLiteOpenHelper {
        public PreDatabaseHelper(Context context) {
            super(
                    context,DB_NAME,null,DB_VERSION
            );
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " +
                            DB_TABLE_PRE +
                            "(id integer primary key autoincrement, word text, subject text, tense text, eng_example text, jap_example text)"
            );
        }
        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion
        ) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_PRE);
            onCreate(db);
        }
    }

}