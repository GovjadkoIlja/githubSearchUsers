package ru.search.github.githubsearch.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ru.search.github.githubsearch.Classes.Repository;
import ru.search.github.githubsearch.R;

/**
 * Created by Илья on 07.05.2017.
 */

public class ReposListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<Repository> reposList;

    TextView tvName;
    TextView tvDescription;
    TextView tvLanguage;
    TextView tvStars;
    TextView tvForks;
    TextView tvUpdated;

    public ReposListAdapter(Context context, ArrayList<Repository> _reposList) {
        ctx = context;
        reposList = _reposList;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return reposList.size();
    }

    @Override
    public Object getItem(int position) {
        return reposList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.repo_item, parent, false);
        }

        tvName = (TextView) view.findViewById(R.id.tvName);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvLanguage = (TextView) view.findViewById(R.id.tvLanguage);
        tvStars = (TextView) view.findViewById(R.id.tvStars);
        tvForks = (TextView) view.findViewById(R.id.tvForks);
        tvUpdated = (TextView) view.findViewById(R.id.tvUpdated);

        tvName.setText(reposList.get(position).name);

        if (reposList.get(position).description == null)
            tvDescription.setVisibility(View.GONE);
        else
            tvDescription.setText(reposList.get(position).description);

        tvLanguage.setVisibility(View.VISIBLE); //Устанавливаем данные по умолчанию
        tvLanguage.setTextColor(Color.BLACK);
        if (reposList.get(position).language == null)
            tvLanguage.setVisibility(View.GONE);
        else {
            setLanguageColor(reposList.get(position).language);
            tvLanguage.setText(reposList.get(position).language);
        }

        tvStars.setText(Integer.toString(reposList.get(position).stars));
        tvForks.setText(Integer.toString(reposList.get(position).forks));

        SimpleDateFormat st = new SimpleDateFormat("dd.MM.yyyy");
        tvUpdated.setText("updated: " + st.format(reposList.get(position).updated));
        //tvUpdated.setText(reposList.get(position).updated);

        return view;
    }

    public void setLanguageColor(String language) { //Отображаем язык соответствующим цветом
        if (language.equals("Ruby"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorRuby));
        else if (language.equals("JavaScript"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorJavaScript));
        else if (language.equals("Go"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorGo));
        else if (language.equals("C"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorC));
        else if (language.equals("Objective-C"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorObjectiveC));
        else if (language.equals("Shell"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorShell));
        else if (language.equals("Python"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorPython));
        else if (language.equals("CoffeeScript"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorCoffeeScript));
        else if (language.equals("Java"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorJava));
        else if (language.equals("C#"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorCSharp));
        else if (language.equals("CSS"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorCSS));
        else if (language.equals("HTML"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorHTML));
        else if (language.equals("C++"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorCPlusPlus));
        else if (language.equals("Erlang"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorErlang));
        else if (language.equals("PHP"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorPHP));
        else if (language.equals("Perl"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorPerl));
        else if (language.equals("OCaml"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorOCaml));
        else if (language.equals("Scala"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorScala));
        else if (language.equals("Kotlin"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorKotlin));
        else if (language.equals("TypeScript"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorTypeScript));
        else if (language.equals("Swift"))
            tvLanguage.setTextColor(ctx.getResources().getColor(R.color.colorSwift));
    }
}
