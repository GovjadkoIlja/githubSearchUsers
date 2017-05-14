package ru.search.github.githubsearch.DBHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Илья on 07.05.2017.
 */

public class HistoryDBHelper extends SQLiteOpenHelper { //Таблица для хранения истории поиска

    public HistoryDBHelper(Context context) {
        super(context, "myDB1", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table history (" //создаем таблицу истории
                + "id integer primary key autoincrement,"
                + "login text,"
                + "avatarUrl text"
                +");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
