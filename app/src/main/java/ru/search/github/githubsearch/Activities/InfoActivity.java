package ru.search.github.githubsearch.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.search.github.githubsearch.Adapters.ReposListAdapter;
import ru.search.github.githubsearch.Classes.FullUserInfo;
import ru.search.github.githubsearch.Classes.Repository;
import ru.search.github.githubsearch.Const;
import ru.search.github.githubsearch.DBHelpers.FavoritesDBHelper;
import ru.search.github.githubsearch.Interfaces.RecieveRepos;
import ru.search.github.githubsearch.Interfaces.RecieveUserInfo;
import ru.search.github.githubsearch.R;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvLogin;
    TextView tvName;
    TextView tvLocation;
    TextView tvCompany;
    TextView tvBlog;
    TextView tvBio;
    TextView tvFolowers;
    TextView tvCreateDate;
    TextView tvUpdateDate;
    TextView tvRepos;
    ListView lvRepos;
    ImageView ivPhoto;
    ImageView ivType;
    ImageButton btnInFavoritesInfo;
    ImageView ivLoadingInfo;
    ImageView ivLoadingRepos;
    ImageView ivDropDown;

    LinearLayout layoutBlog;
    LinearLayout layoutLocation;
    LinearLayout layoutJob;
    RelativeLayout layoutLoadingInfo;
    RelativeLayout layoutLoadingRepos;
    LinearLayout layoutRepos;

    FullUserInfo userInfo;
    public ArrayList<Repository> reposList;
    public ReposListAdapter reposListAdapter;
    int searchPage = 1;
    boolean isRecieving = false; //Получаем ли в данный момент данные
    boolean reposOpen = false; //Открыт ли список репозиториев
    boolean inFavoritesBegin;
    boolean internetConnection = true;

    Gson gson = new GsonBuilder().create();

    Retrofit searchRetrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Const.githubAddress)
            .build();

    RecieveUserInfo userInfoIntf = searchRetrofit.create(RecieveUserInfo.class);
    RecieveRepos reposIntf = searchRetrofit.create(RecieveRepos.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvName = (TextView) findViewById(R.id.tvName);
       // tvMail = (TextView) findViewById(R.id.tvMail);
       // tvType = (TextView) findViewById(R.id.tvType);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvCompany = (TextView) findViewById(R.id.tvCompany);
        tvBlog = (TextView) findViewById(R.id.tvBlog);
        tvBio = (TextView) findViewById(R.id.tvBio);
        tvFolowers = (TextView) findViewById(R.id.tvFolowers);
        tvCreateDate = (TextView) findViewById(R.id.tvCreateDate);
        tvUpdateDate = (TextView) findViewById(R.id.tvUpdateDate);
        tvRepos = (TextView) findViewById(R.id.tvRepos);
        lvRepos = (ListView) findViewById(R.id.lvRepos);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        ivType = (ImageView) findViewById(R.id.ivType);
        btnInFavoritesInfo = (ImageButton) findViewById(R.id.btnInFavoritesInfo);

        ivLoadingInfo = (ImageView) findViewById(R.id.ivLoadingInfo);
        ivLoadingRepos = (ImageView) findViewById(R.id.ivLoadingRepos);

        layoutBlog = (LinearLayout) findViewById(R.id.layoutBlog);
        layoutLocation = (LinearLayout) findViewById(R.id.layoutLocation);
        layoutJob = (LinearLayout) findViewById(R.id.layoutJob);
        layoutLoadingInfo = (RelativeLayout) findViewById(R.id.layoutLoadingInfo);
        layoutLoadingRepos = (RelativeLayout) findViewById(R.id.layoutLoadingRepos);
        layoutRepos = (LinearLayout) findViewById(R.id.layoutRepos);
        ivDropDown = (ImageView) findViewById(R.id.ivDropDown);

        //tvRepos.setOnClickListener(this);
        btnInFavoritesInfo.setOnClickListener(this);
        layoutBlog.setOnClickListener(this);
        layoutRepos.setOnClickListener(this);

        reposList = new ArrayList<>();

        reposListAdapter = new ReposListAdapter(this, reposList);
        lvRepos.setAdapter(reposListAdapter);

        lvRepos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(reposList.get(position).repoUrl));
                startActivity(browserIntent);
            }
        });

        lvRepos.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if ((totalItemCount == visibleItemCount) || (isRecieving)) //Если все вывели или в данный момент уже получаем данные
                    return;
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
                    ReposRecieving reposRecieving = new ReposRecieving();
                    reposRecieving.execute(userInfo.login); //Получаем список репозиториев пользователя
                }
            }
        });

        Animation loading = AnimationUtils.loadAnimation(this, R.anim.loading);
        ivLoadingInfo.setAnimation(loading);

        UserInfoRecieving userInfoRecieving = new UserInfoRecieving();
        userInfoRecieving.execute(getIntent().getStringExtra("login")); //Получаем логин, информацию по которому хотим вывести
    }

    @Override
    public void onBackPressed() { //При нажатии назад возвращаем измении избранные или нет
        Intent intent = new Intent();
        if (userInfo == null) {
            intent.putExtra("login", getIntent().getStringExtra("login"));
            intent.putExtra("hasChanged", false);
        } else {
            intent.putExtra("hasChanged", userInfo.inFavorites != inFavoritesBegin);
            intent.putExtra("login", userInfo.login);
            intent.putExtra("inFavorites", userInfo.inFavorites); //В избранных ли теперь
        }
        setResult(RESULT_OK, intent);
        finish();
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.layoutRepos):
                internetConnection = true; //Чтобы потом заново выводить, если интернета нет
                if (reposOpen) {  //Если список список репозиториев открыт - закрываем его
                    lvRepos.setVisibility(View.GONE);
                    reposOpen = false;
                    ivDropDown.setRotation(ivDropDown.getRotation() + 180);
                } else {
                    if (reposList.size() == 0) { //Если список репозиториев пуст - получаем их
                        layoutLoadingRepos.setVisibility(View.VISIBLE); //Устанавливаем анимацию
                        Animation loading = AnimationUtils.loadAnimation(this, R.anim.loading);
                        ivLoadingRepos.setAnimation(loading);

                        ReposRecieving reposRecieving = new ReposRecieving(); //Получаем список репозиториев пользователя
                        reposRecieving.execute(userInfo.login);

                        int res;
                        try {
                            res = reposRecieving.get();
                        } catch (Exception e) {
                            res = -2;
                        }
                        if (res != -2) { //Если мы смогли получить список
                            lvRepos.setVisibility(View.VISIBLE);
                            reposOpen = true;
                            ivDropDown.setRotation(ivDropDown.getRotation() + 180);
                        }
                    } else {
                        lvRepos.setVisibility(View.VISIBLE);
                        reposOpen = true;
                        ivDropDown.setRotation(ivDropDown.getRotation() + 180);
                    }
                }
                break;

            case (R.id.btnInFavoritesInfo):
                FavoritesDBHelper dbHelper = new FavoritesDBHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                if (userInfo.inFavorites) {
                    btnInFavoritesInfo.setImageResource(R.drawable.favorite);

                    db.delete("favorites", "login = '"+userInfo.login+"'", null); //Удаляем из БД выбранный логин, если он там уже есть
                }
                else { //Если нет в избранных - добавляем
                    btnInFavoritesInfo.setImageResource(R.drawable.favorite_selected);

                    ContentValues cv = new ContentValues(); //Заносим в БД выбранный логин
                    cv.put("login", userInfo.login);
                    cv.put("avatarUrl", userInfo.avatarUrl);
                    db.insert("favorites", null, cv);
                }
                db.close();
                userInfo.inFavorites = !userInfo.inFavorites;
                break;

            case (R.id.layoutBlog):
                Intent browserIntent = new
                        Intent(Intent.ACTION_VIEW, Uri.parse(userInfo.blog));
                startActivity(browserIntent);

        }
    }

    public void setLayout() { //Выставляем текст на экране и убираем его, если он не нужен
        Picasso.with(this) //Загружаем аватар
                .load(userInfo.avatarUrl)
                .placeholder(R.drawable.nophoto)
                .error(R.drawable.nophoto)
                .into(ivPhoto);

        if (userInfo.inFavorites)
            btnInFavoritesInfo.setImageResource(R.drawable.favorite_selected);
        else
            btnInFavoritesInfo.setImageResource(R.drawable.favorite);

        if (userInfo.login == null)
            tvLogin.setVisibility(View.GONE);
        else
            tvLogin.setText(userInfo.login);

        if (userInfo.name == null) {
            tvName.setVisibility(View.GONE);
            ivType.setVisibility(View.GONE);
        }
        else
            tvName.setText(userInfo.name);

       /* if (userInfo.mail == null)
            tvMail.setVisibility(View.GONE);
        else
            tvMail.setText("mail: " +  userInfo.mail);*/

        if (userInfo.type == null)
            ivType.setVisibility(View.GONE);
        else if (userInfo.type.equals("Organization"))
            ivType.setImageResource(R.drawable.organisation);

        if (userInfo.location == null)
            layoutLocation.setVisibility(View.GONE);
        else
            tvLocation.setText(userInfo.location);

        if (userInfo.company == null)
            layoutJob.setVisibility(View.GONE);
        else
            tvCompany.setText(userInfo.company);

        if (userInfo.blog.equals(""))
            layoutBlog.setVisibility(View.GONE);
        else {
            tvBlog.setText(Html.fromHtml("<u>" + userInfo.blog + "</u>"));
        }

        if (userInfo.bio == null)
            tvBio.setVisibility(View.GONE);
        else
            tvBio.setText(userInfo.bio);

        tvFolowers.setText("followers: " + userInfo.folowers);

        SimpleDateFormat st = new SimpleDateFormat("dd.MM.yyyy");
        if (userInfo.createDate == null)
            tvCreateDate.setVisibility(View.GONE);
        else
            tvCreateDate.setText("created: " + st.format(userInfo.createDate));

        if (userInfo.updateDate == null)
            tvUpdateDate.setVisibility(View.GONE);
        else
            tvUpdateDate.setText("updated: " + st.format(userInfo.updateDate));

        tvRepos.setText(Html.fromHtml("<u>Репозитории: " + userInfo.reposCount + "</u>"));

        /*SpannableString ss = new SpannableString(userInfo.bio);
        // Выставляем отступ для первых трех строк абазца
        ss.setSpan(new MyLeadingMarginSpan2(3, 110), 0, ss.length(), 0);*/
    }

    class UserInfoRecieving extends AsyncTask<String, Void, Integer> { //Получаем информацию о пользователе

        protected Integer doInBackground(String... name) {
            Call<Object> call = userInfoIntf.recieve(name[0]);

            try {
                Response<Object> response = call.execute(); //Обращаемся к ГитХабу за информацией

                Map<String, String> infoMap = gson.fromJson(gson.toJson(response.body()), Map.class);

                userInfo = new FullUserInfo(infoMap, InfoActivity.this); //Создаем userInfo по Map

            } catch (IOException e) {
                return -2;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code == -2) {
                Toast.makeText(InfoActivity.this, "Internet connection required", Toast.LENGTH_LONG).show(); //Уведомляем пользователя о необходимости интернета
                onBackPressed();
                return;
            }

            ivLoadingInfo.clearAnimation(); //Убираем загрузку
            layoutLoadingInfo.setVisibility(View.GONE);

            setLayout();
            inFavoritesBegin = userInfo.inFavorites;
        }
    }

    class ReposRecieving extends AsyncTask<String, Void, Integer> { //Получаем репозитории пользвателя

        protected Integer doInBackground(String... name) {
            isRecieving = true;
            Call<Object> call = reposIntf.recieve(name[0], "pushed", searchPage, Const.perPageRepos); //Сортируем по последнему выложенному репозиторию этого пользователя

            try {
                Response<Object> response = call.execute(); //Получаем список репозиториев пользователя

                ArrayList<Map<String, String>> reposMapList = gson.fromJson(gson.toJson(response.body()), ArrayList.class);

                for (int i=0; i<reposMapList.size(); i++)
                    reposList.add(new Repository(reposMapList.get(i)));
            } catch (IOException e) {
                return -2;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);

            if (code == -2) {
                isRecieving = false;
                if (!internetConnection) //Если подключения не было - ничего не надо менять
                    return;
                Toast.makeText(InfoActivity.this, "Internet connection required", Toast.LENGTH_LONG).show(); //Уведомляем пользователя о необходимости интернета
                internetConnection = false;
                layoutLoadingRepos.setVisibility(View.GONE); //Убираем анимацию

                return;
            }

            internetConnection = true;
            searchPage++; //Номер искомой страницы
            ivLoadingInfo.clearAnimation(); //Убираем анимацию и прячем загрузку
            layoutLoadingRepos.setVisibility(View.GONE);

            reposListAdapter.notifyDataSetChanged();

            isRecieving = false;
        }
    }
}
