package ru.search.github.githubsearch.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.search.github.githubsearch.Classes.UserShort;
import ru.search.github.githubsearch.DBHelpers.FavoritesDBHelper;
import ru.search.github.githubsearch.R;

/**
 * Created by Илья on 04.05.2017.
 */

public class UserListAdapter extends BaseAdapter implements View.OnClickListener {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<UserShort> userList = new ArrayList<>();
    ArrayList<UserShort> favoritesList = new ArrayList<>();
    boolean isFavorites;

    TextView tvLogin;
    ImageView ivAvatar;
    ImageButton btnInFavorites;

    public UserListAdapter(Context context, ArrayList<UserShort> _userList, ArrayList<UserShort> _favoritesList, boolean _isFavorites) {
        ctx = context;
        userList = _userList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        favoritesList = _favoritesList;
        isFavorites = _isFavorites; //Избранные выводятся или нет
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return userList.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.usershort_item, parent, false);
        }

        tvLogin = (TextView) view.findViewById(R.id.tvLogin);
        ivAvatar = (ImageView) view.findViewById(R.id.ivAvatar);
        btnInFavorites = (ImageButton) view.findViewById(R.id.btnInFavorites);
        btnInFavorites.setOnClickListener(this);

        Picasso.with(ctx) //Загружаем фото
                .load(userList.get(position).avatarUrl)
                .placeholder(R.drawable.nophoto) //!!!Различить placeholder и error
                .error(R.drawable.nophoto)
                .into(ivAvatar);

        tvLogin.setText(userList.get(position).login);
        Pair<Boolean, UserShort> tag = new Pair(false, userList.get(position)); //Тэг - в избранных ли и логин

        if (!isFavorites) {
            btnInFavorites.setImageResource(R.drawable.favorite);
            for (int i = 0; i < favoritesList.size(); i++) {
                if (favoritesList.get(i).login.equals(userList.get(position).login)) {
                    btnInFavorites.setImageResource(R.drawable.favorite_selected);
                    tag = new Pair(true, userList.get(position));
                    break;
                }
            }
        } else {
            btnInFavorites.setImageResource(R.drawable.favorite_selected);
            tag = new Pair(true, userList.get(position));
        }
        /*if (favoritesList.contains(userList.get(position))) { //Если элемент в избранных
            btnInFavorites.setImageResource(R.drawable.favorite_selected);
            tag = new Pair(true, userList.get(position));
        }
        else {
            btnInFavorites.setImageResource(R.drawable.greystar);
            tag = new Pair(false, userList.get(position));
        }*/
        btnInFavorites.setTag(tag);

        btnInFavorites.setFocusable(false);

        return view;
    }

    @Override
    public void onClick(View v) {
        FavoritesDBHelper dbHelper = new FavoritesDBHelper(ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ImageButton clicked = (ImageButton) v;
        Pair<Boolean, UserShort> tag = (Pair<Boolean, UserShort>)v.getTag(); //В избранных ли находится и информация
        UserShort selected = tag.second;

        if (tag.first) { //Если есть в избранных - удаляем
            clicked.setImageResource(R.drawable.favorite);
            if (!isFavorites) { //Чтобы, если мы в избранных не удалялось из ListView сразу, а удалилось потом
                for (int i=0; i<favoritesList.size(); i++)
                    if (favoritesList.get(i).login.equals(selected.login)) {
                        favoritesList.remove(i);
                        break;
                    }
            }

            db.delete("favorites", "login = '"+selected.login+"'", null); //Удаляем из БД выбранный логин, если он там уже есть
        } else { //Если нет в избранных - добавляем
            clicked.setImageResource(R.drawable.favorite_selected);
            clicked.setTag(true);
            if (!isFavorites)
                favoritesList.add(0, selected);

            ContentValues cv = new ContentValues(); //Заносим в БД выбранный логин
            cv.put("login", selected.login);
            cv.put("avatarUrl", selected.avatarUrl);
            db.insert("favorites", null, cv);
        }
        db.close();

        tag = new Pair(!tag.first, selected); //Обновляем тэг
        clicked.setTag(tag);
    }
}
