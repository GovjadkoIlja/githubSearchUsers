package ru.search.github.githubsearch.Classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import ru.search.github.githubsearch.DBHelpers.FavoritesDBHelper;

/**
 * Created by Илья on 04.05.2017.
 */

public class FullUserInfo { //Добавить блог
    public String avatarUrl;
    public String login;
    public String name;
    public String mail;
    public String location;
    public String company;
    public String blog;
    public String bio;
    public int reposCount;
    public int folowers;
    public String type;
    public Date createDate;
    public Date updateDate;
    public boolean inFavorites;
    private Context context;

    public FullUserInfo(Map<String, String> infoMap, Context _context) { //Инициализируем информацию по map информации
        context = _context;
        avatarUrl = infoMap.get("avatar_url");
        login = infoMap.get("login");
        name = infoMap.get("name");
        mail = infoMap.get("email");
        location = infoMap.get("location");
        company = infoMap.get("company");
        blog = infoMap.get("blog");
        bio = infoMap.get("bio");
        reposCount = (int)Double.parseDouble(String.valueOf(infoMap.get("public_repos")));
        folowers = (int)Double.parseDouble(String.valueOf(infoMap.get("followers")));
        type = infoMap.get("type");

        createDate = makeDateFromString(infoMap.get("created_at"));

        updateDate = makeDateFromString(infoMap.get("updated_at"));

        FavoritesDBHelper dbHelper = new FavoritesDBHelper(context); //Проверяем есть ли в избранных
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c;

        c = db.query("favorites", null, "login = ?", new String[] {login}, null, null, null);

        inFavorites = c.moveToFirst();

        c.close();
        db.close();
    }

    public static Date makeDateFromString(String s) {
        s = s.substring(0, s.indexOf('T')); //Обрезаем время
        SimpleDateFormat st = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return st.parse(s);
        }catch (Exception e) {
            return null;
        }

    }
}
