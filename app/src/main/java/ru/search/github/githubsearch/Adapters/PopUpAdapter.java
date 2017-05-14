package ru.search.github.githubsearch.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.search.github.githubsearch.Classes.UserShort;
import ru.search.github.githubsearch.R;

/**
 * Created by Илья on 09.05.2017.
 */

public class PopUpAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<UserShort> popUpList;

    TextView tvName;

     public PopUpAdapter(Context context, ArrayList<UserShort> _popUpList) {
        ctx = context;
        popUpList = _popUpList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return popUpList.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return popUpList.get(position);
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
            view = lInflater.inflate(R.layout.popup_item, parent, false);
        }

        tvName = (TextView) view.findViewById(R.id.tvUsername);

        tvName.setText(popUpList.get(position).login);

        return view;
    }
}