package ru.search.github.githubsearch.DBHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Илья on 08.05.2017.
 */

public class FavoritesDBHelper extends SQLiteOpenHelper { //Таблица для хранения избранных

    public FavoritesDBHelper(Context context) {
        super(context, "myDB2", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table favorites (" //создаем таблицу языков
                + "id integer primary key autoincrement,"
                + "login text,"
                + "avatarUrl text"
                +");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
