package com.example.ziyi.sqlite;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WordsDBHelper wordsDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView)findViewById(R.id.listview);
        registerForContextMenu(listView);

        wordsDBHelper = new WordsDBHelper(this);

        ArrayList<Map<String, String>> items=getAll();
        setWordsListView(items);
    }

    protected void onDestroy(){
        super.onDestroy();
        wordsDBHelper.close();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();

        switch (id){
            case R.id.action_search:
                SearchDialog();
                return true;
            case R.id.action_add:
                InsertDialog();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo infor){
        super.onCreateOptionsMenu(contextMenu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_concent,contextMenu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        TextView textID = null;
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSample = null;
        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;

        switch (item.getItemId()){
            case R.id.menu_update:
                info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView = info.targetView;
                textID = (TextView)itemView.findViewById(R.id.textID);
                textWord = (TextView)itemView.findViewById(R.id.textViewWord);
                textMeaning = (TextView)itemView.findViewById(R.id.textViewMeaning);
                textSample = (TextView)itemView.findViewById(R.id.textViewSample);
                if(textID != null && textWord != null && textMeaning != null && textSample != null){
                    String strID = textID.getText().toString();
                    String strWord = textWord.getText().toString();
                    String strMeaning = textMeaning.getText().toString();
                    String strSample = textSample.getText().toString();
                    UpdateDialog(strID,strWord,strMeaning,strSample);
                }
                break;
            case R.id.menu_delete:
                info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView = info.targetView;
                textID = (TextView)itemView.findViewById(R.id.textID);
                if(textID != null){
                    String strID = textID.getText().toString();
                    DeleteDialog(strID);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void setWordsListView(ArrayList<Map<String,String>> items){
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,items,R.layout.item_layout,
                new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD,Words.Word.COLUMN_NAME_MEANING,Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textID,R.id.textViewWord,R.id.textViewMeaning,R.id.textViewSample});

        ListView listView = (ListView)findViewById(R.id.listview);
        listView.setAdapter(simpleAdapter);
    }

    private ArrayList<Map<String,String>> getAll(){
        SQLiteDatabase db = wordsDBHelper.getWritableDatabase();

        String[] projection = {
                Words.Word._ID,
                Words.Word.COLUMN_NAME_WORD,
                Words.Word.COLUMN_NAME_MEANING,
                Words.Word.COLUMN_NAME_SAMPLE
        };

        String sortOrder = Words.Word.COLUMN_NAME_WORD + " DESC";

        Cursor cursor = db.query(
                Words.Word.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return ConvertCursor2List(cursor);
    }

    private ArrayList<Map<String,String>> ConvertCursor2List(Cursor cursor){
        ArrayList<Map<String,String>> result = new ArrayList<>();
        while (cursor.moveToNext()){
            Map<String,String> map = new HashMap<>();
            map.put(Words.Word._ID,String.valueOf(cursor.getInt(0)));
            map.put(Words.Word.COLUMN_NAME_WORD,cursor.getString(1));
            map.put(Words.Word.COLUMN_NAME_MEANING,cursor.getString(2));
            map.put(Words.Word.COLUMN_NAME_SAMPLE,cursor.getString(3));
            result.add(map);
        }
        return result;
    }

    private void Insert(String strWord,String strMeaning,String strSample){
        SQLiteDatabase db = wordsDBHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Words.Word.COLUMN_NAME_WORD,strWord);
        contentValues.put(Words.Word.COLUMN_NAME_MEANING,strMeaning);
        contentValues.put(Words.Word.COLUMN_NAME_SAMPLE,strSample);

        long newRowID;
        newRowID = db.insert(
                Words.Word.TABLE_NAME,
                null,
                contentValues
        );
    }

    private void InsertDialog(){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.insert,null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")
                .setView(tableLayout)
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String strWord = ((EditText)tableLayout.findViewById(R.id.txtWord)).getText().toString();
                                String strMeaning = ((EditText)tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                                String strSample = ((EditText)tableLayout.findViewById(R.id.txtSample)).getText().toString();

                                Insert(strWord,strMeaning,strSample);

                                setWordsListView(getAll());
                            }
                        }
                )
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private void DeleteUseSQL(String strID){
        String sql = "delete from words where _id = ' " + strID + " ' ";

        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        db.execSQL(sql);
    }

    private void DeleteDialog(final String strID){
        new AlertDialog.Builder(this).setTitle("删除单词")
                .setMessage("确定删除单词？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeleteUseSQL(strID);

                        setWordsListView(getAll());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private  void UpdateUseSQL(String strID,String strWord,String strMeaning,String strSample){
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        String sql = "update words set word = ?,meaning = ?,sample = ? where _id = ?";
        db.execSQL(sql,new String[]{strWord,strMeaning,strSample,strID});
    }

    private void UpdateDialog(final String strID,final String strWord,final String strMeaning,final String strSample){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.insert,null);
        ((EditText)tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText)tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText)tableLayout.findViewById(R.id.txtSample)).setText(strSample);

        new AlertDialog.Builder(this)
                .setTitle("修改单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newWord = ((EditText)tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String newMeaning = ((EditText)tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String newSample = ((EditText)tableLayout.findViewById(R.id.txtSample)).getText().toString();

                        UpdateUseSQL(strID,newWord,newMeaning,newSample);

                        setWordsListView(getAll());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }

    private ArrayList<Map<String,String>> SearchUseSQL(String searchWord){
        SQLiteDatabase db = wordsDBHelper.getReadableDatabase();
        String sql = "select * from words where word like ? order by word desc";
        Cursor cursor = db.rawQuery(sql,new String[]{"%" + searchWord + "%"});
        return ConvertCursor2List(cursor);
    }

    private void SearchDialog(){
        final TableLayout tableLayout = (TableLayout)getLayoutInflater().inflate(R.layout.search,null);

        new AlertDialog.Builder(this)
                .setTitle("查找单词")
                .setView(tableLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String searchWord = ((EditText)tableLayout.findViewById(R.id.search_word)).getText().toString();

                        ArrayList<Map<String,String>> items = SearchUseSQL(searchWord);

                        if(items.size() > 0){
                            setWordsListView(items);
                        }
                        else Toast.makeText(MainActivity.this,"没有找到",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create()
                .show();
    }
}
