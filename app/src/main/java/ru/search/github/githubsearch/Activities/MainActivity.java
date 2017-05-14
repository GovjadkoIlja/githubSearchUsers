package ru.search.github.githubsearch.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.search.github.githubsearch.Adapters.PopUpAdapter;
import ru.search.github.githubsearch.Adapters.UserListAdapter;
import ru.search.github.githubsearch.Classes.UserShort;
import ru.search.github.githubsearch.Const;
import ru.search.github.githubsearch.DBHelpers.FavoritesDBHelper;
import ru.search.github.githubsearch.DBHelpers.HistoryDBHelper;
import ru.search.github.githubsearch.Interfaces.SearchSend;
import ru.search.github.githubsearch.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher, TextView.OnEditorActionListener{

    Button btnSearch;
    EditText etName;
    ListView lvResults;
    ListPopupWindow mListPopupWindow;
    ImageButton btnDelete;
    RelativeLayout layoutSearch;
    ImageButton btnFavorites;
    ImageButton btnAll;
    ImageButton btnHistory;
    RelativeLayout layoutEdit;
    ImageView ivLoading;
    RelativeLayout layoutLoading;

    ArrayList<UserShort> userList = new ArrayList<>();
    ArrayList<UserShort> favoritesList = new ArrayList<>(); //Список избранных
    ArrayList<UserShort> history = new ArrayList<>(); //Список прошлых поисков
    ArrayList<UserShort> popUp = new ArrayList<>(); //Список, выводящийся в подсказках

    UserListAdapter userListAdapter; //Задаем адаптер для списка значений
    UserListAdapter favoritesListAdapter; //Адаптер для избранных
    UserListAdapter historyListAdapter; //Адаптер для избранных

    int searchPage = 1;
    boolean isRecieving = false; //Получаем ли в данный момент данные
    boolean isFavoritesOpen = false; //Избранные ли открыты. Это подгружать при загрузке приложения
    boolean isHistoryOpen = false; //История ли открыта. Это подгружать при загрузке приложения
    int selectedPosition = -1;
    boolean fromPopUp = false;
    boolean goToUser = false; //Переходим к пользователю или выключаем. Чтобы занть, когда сохранять
    int usersCount = 0;
    boolean internetConnection = true;
    boolean searchDone = false;

    SharedPreferences savedData; //Сохранение данных

    Gson gson = new GsonBuilder().create();

    Retrofit searchRetrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(Const.githubAddress)
            .build();

    SearchSend searchSendIntf = searchRetrofit.create(SearchSend.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText) findViewById(R.id.etName);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        lvResults = (ListView) findViewById(R.id.lvResults);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        layoutSearch = (RelativeLayout) findViewById(R.id.layoutSearch);
        layoutEdit = (RelativeLayout) findViewById(R.id.layoutEdit);

        btnAll = (ImageButton) findViewById(R.id.btnAll);
        btnFavorites = (ImageButton) findViewById(R.id.btnFavorites);
        btnHistory = (ImageButton) findViewById(R.id.btnHistory);
        ivLoading = (ImageView) findViewById(R.id.ivLoading);
        layoutLoading = (RelativeLayout) findViewById(R.id.layoutLoading);

        btnAll.setOnClickListener(this);
        btnFavorites.setOnClickListener(this);
        btnHistory.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        etName.addTextChangedListener(this);
        etName.setOnEditorActionListener(this);
        etName.setOnClickListener(this);

        lvResults.setOnScrollListener(new AbsListView.OnScrollListener() { //Подгрузка при прокрутке
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if ((usersCount == userList.size()) || (isRecieving) || (isFavoritesOpen) || (isHistoryOpen)) //Если все вывели или в данный момент уже получаем данные bили в истории или избранных
                    return;
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
                    UsersRecieveing usersRecieveing = new UsersRecieveing(); //Получаем список пользователей
                    usersRecieveing.execute(etName.getText().toString());
                }
            }
        });

        setFavoritesList();

        userListAdapter = new UserListAdapter(this, userList, favoritesList, isFavoritesOpen);
        lvResults.setAdapter(userListAdapter);

        mListPopupWindow = new ListPopupWindow(this); //Настраиваем список подсказок
        mListPopupWindow.setAnchorView(layoutEdit);

        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() { //Выбор из подсказок
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToUser = true;
                selectedPosition = position;
                fromPopUp = true;
                selectUser(popUp.get(position));
                saveData(popUp.get(position).login, etName.getText().toString(), true);
            }
        });

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToUser = true;
                selectedPosition = position;
                UserShort selected;
                if (isFavoritesOpen)
                    selected = favoritesList.get(position);
                else {
                    if (isHistoryOpen)
                        selected = history.get(position);
                    else
                        selected = userList.get(position);
                }

                saveData(selected.login, etName.getText().toString(), true);
                selectUser(selected);
            }
        });

        setData(); //Устанавливаем данные
    }

    @Override
    protected void onStop() { //Сохраняем данные
        super.onStop();

        if (!goToUser) //Если это не переход на пользователя
            saveData("", etName.getText().toString(), false);
    }

    @Override
    protected void onResume() { //Восстанавливаем данные
        super.onResume();
        goToUser = false; //Не переходим на пользователя в данный момент

        history.clear(); //Очищаем исорию, чтобы получить ее заново

        HistoryDBHelper dbHelper = new HistoryDBHelper(this); //Получаем обновленную историю
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c;

        c = db.query("history", null, null, null, null, null, null);

        UserShort user;
        if (c.moveToFirst()) {
            do {
                user = new UserShort(c.getString(c.getColumnIndex("login")), c.getString(c.getColumnIndex("avatarUrl")));
                history.add(0, user);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        if (etName.getText().length() == 0) //Скрываем кнопку уддаления, если поле пустое
            btnDelete.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.btnSearch):
                internetConnection = true; //Чтобы снова выводить, что интернета нет, если его нет

                userList.clear(); //Очищаем список результатов
                searchPage = 1; //Снова результаты выводим с первой страницы
                if (!isRecieving) { //Если в данный момент не получаем данные
                    if (etName.getText().length() > 0) {
                        layoutLoading.setVisibility(View.VISIBLE); //Показываем анимацию загрузки, вместо старого списка
                        lvResults.setVisibility(View.GONE);
                        Animation loading = AnimationUtils.loadAnimation(this, R.anim.loading);
                        ivLoading.setAnimation(loading);

                        UsersRecieveing usersRecieveing = new UsersRecieveing(); //Получаем список пользователей
                        usersRecieveing.execute(etName.getText().toString());
                    } else //Если поле пустое
                        Toast.makeText(MainActivity.this, "Firstable enter the user's login to find him", Toast.LENGTH_LONG).show();

                }
                break;

            case (R.id.btnDelete):
                etName.setText("");
                break;

            case (R.id.btnFavorites):
                isFavoritesOpen = true;
                isHistoryOpen = false;
                favoritesListAdapter = new UserListAdapter(this, favoritesList, favoritesList, isFavoritesOpen);
                lvResults.setAdapter(favoritesListAdapter);
                layoutSearch.setVisibility(View.GONE);
                lvResults.setVisibility(View.VISIBLE);

                setImages(1);
                break;

            case (R.id.btnAll):
                isFavoritesOpen = false;
                isHistoryOpen = false;
                layoutSearch.setVisibility(View.VISIBLE);
                lvResults.setAdapter(userListAdapter);

                setImages(0);
                setFavoritesList();
                break;

            case (R.id.btnHistory):
                isFavoritesOpen = false;
                isHistoryOpen = true;
                historyListAdapter = new UserListAdapter(this, history, favoritesList, false);
                lvResults.setAdapter(historyListAdapter);
                layoutSearch.setVisibility(View.GONE);
                lvResults.setVisibility(View.VISIBLE);

                setImages(2);
                setFavoritesList();
                break;

            case (R.id.etName):
                afterTextChanged(etName.getText());
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (etName.getWidth() == 0) //Если запускается слишком рано
            return;

        if ((v.getId() == R.id.etName) && (hasFocus)) { //Если фокус на поле логина
            popUp = (ArrayList<UserShort>) history.clone();
            mListPopupWindow.setAdapter(new PopUpAdapter(this, popUp));
            mListPopupWindow.setWidth(layoutEdit.getWidth());
            setPopUpListSize();
            mListPopupWindow.show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (etName.getWidth() == 0) //Если активити еще не загрузилось - выходим
            return;

        if (searchDone) {
            mListPopupWindow.dismiss();
            searchDone = false;
            return;
        }

        popUp.clear();

        for (int i=0; i<history.size(); i++) {
            if (history.get(i).login.toLowerCase().startsWith(s.toString().toLowerCase()))
                popUp.add(history.get(i));
        }

        if (s.length() > 0) {
            btnDelete.setVisibility(View.VISIBLE);
            if (popUp.size() == 0) { //Если нет такого в истории - удаляем подсказки
                mListPopupWindow.dismiss();
                return;
            }
            mListPopupWindow.setAdapter(new PopUpAdapter(this, popUp));
            setPopUpListSize();
        } else { //Если поле логина пустое
            btnDelete.setVisibility(View.INVISIBLE);
            popUp = (ArrayList<UserShort>) history.clone();
            mListPopupWindow.setAdapter(new PopUpAdapter(this, popUp));
            setPopUpListSize();
        }

        mListPopupWindow.setWidth(layoutEdit.getWidth());
        mListPopupWindow.show();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { //Поиск по нажатии на Enter
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchDone = true; //Указали, что ищем, чтобы не выводить подсказки
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //Убираем клавиатуру
            btnSearch.callOnClick(); //Начинаем поиск
            mListPopupWindow.dismiss(); //Скрываем подсказки
        }
        return false;
    }

    public void setFavoritesList() { //Настраиваем список избранных
        favoritesList.clear();

        FavoritesDBHelper dbHelper = new FavoritesDBHelper(this); //Заполняем список избранных
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c;

        c = db.query("favorites", null, null, null, null, null, null);

        UserShort info;
        String login;
        String avatarUrl;
        if (c.moveToFirst()) {
            do {
                login = c.getString(c.getColumnIndex("login"));
                avatarUrl = c.getString(c.getColumnIndex("avatarUrl"));
                info = new UserShort(login, avatarUrl);

                favoritesList.add(0, info);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
    }

    public void setPopUpListSize() { //Устанавливаем высоту подсказок
        if (popUp.size() < 6)
            mListPopupWindow.setHeight((int)(popUp.size() * 35 * getApplicationContext().getResources().getDisplayMetrics().density));
        else
            mListPopupWindow.setHeight((int)(5 * 35 * getApplicationContext().getResources().getDisplayMetrics().density));
    }

    public void selectUser(UserShort selected) { //действия, при выобре пользователя
        HistoryDBHelper dbHelper = new HistoryDBHelper(this); //Заносим выбранный логин в историю
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("history", "login =   '"+selected.login+"'", null); //Удаляем из БД выбранный логин, если он там уже есть

        ContentValues cv = new ContentValues();
        cv.put("login", selected.login);
        cv.put("avatarUrl", selected.avatarUrl);
        db.insert("history", null, cv);
        db.close();

        Intent intent;
        intent = new Intent(this, InfoActivity.class);
        intent.putExtra("login", selected.login);
        startActivityForResult(intent, 0);
    }

    public void setImages(int selected) { //Настройка картинок на кнопках
        //0 - поиск
        //1 - избранные
        //2 - история

        if (selected == 0) {
            btnAll.setImageResource(R.drawable.search_selected);
            btnFavorites.setImageResource(R.drawable.favorite);
            btnHistory.setImageResource(R.drawable.history);
        } else {
            btnAll.setImageResource(R.drawable.search);
            if (selected == 1) {
                btnFavorites.setImageResource(R.drawable.favorite_selected);
                btnHistory.setImageResource(R.drawable.history);
            }
            else {
                btnFavorites.setImageResource(R.drawable.favorite);
                btnHistory.setImageResource(R.drawable.history_selected);
            }
        }
    }

    public void saveData(String login, String search, boolean inInfo) { //Сохранение данных при выходе
        mListPopupWindow.dismiss();
        savedData.edit().clear().commit(); //Очищаем прошлые настройки

        savedData = getPreferences(MODE_PRIVATE); //Сохраняем данные для следующего запуска
        SharedPreferences.Editor ed = savedData.edit();
        ed.putString("login", login);
        ed.putString("search", search);
        ed.putBoolean("inInfo", inInfo);

        ed.commit();
    }

    public void setData() { //Восстанавливаем данные
        setImages(0);

        savedData = getPreferences(MODE_PRIVATE);
        String login = savedData.getString("login", "");
        String search = savedData.getString("search", "");
        boolean inInfo = savedData.getBoolean("inInfo", false);

        etName.setText(search);
        etName.setSelection(search.length()); //Выставляем курсор в конец

        if (search.length() == 0) //Если ничего не сохранено - выходим
            return;

        if (!inInfo) { //Если вышли из поиска
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //Убираем клавиатуру
            btnSearch.callOnClick(); //Начинаем поиск
            mListPopupWindow.dismiss(); //Скрываем подсказки
        } else { //Если мы в информации о пользователе - не надо занонить в историю, так как это и так в истории последней записью
            btnSearch.callOnClick(); //Начинаем поиск

            Intent intent;
            intent = new Intent(this, InfoActivity.class);
            intent.putExtra("login", login);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getBooleanExtra("hasChanged", false)) { //Если значение избранных изменилость - добавляем или удаляем из избранных новое значение
            if (data.getBooleanExtra("inFavorites", false)) {
                if (!isFavoritesOpen) {
                    if (fromPopUp) //Если пользовател был выбран через PopUp
                        favoritesList.add(0, popUp.get(selectedPosition));
                    else if (isHistoryOpen)
                        favoritesList.add(0, history.get(selectedPosition));
                    else
                        favoritesList.add(0, userList.get(selectedPosition));
                }
            } else {
                if (!isFavoritesOpen) {
                    if (fromPopUp) { //Если пользователь был выбран через PopUp
                        for (int i = 0; i < favoritesList.size(); i++) {
                            if (favoritesList.get(i).login.equals(popUp.get(selectedPosition).login)) {
                                favoritesList.remove(i);
                                break;
                            }
                        }
                    } else if (isHistoryOpen) {
                        for (int i = 0; i < history.size(); i++) {
                            if (favoritesList.get(i).login.equals(history.get(selectedPosition).login)) {
                                favoritesList.remove(i);
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < favoritesList.size(); i++) {
                            if (favoritesList.get(i).login.equals(userList.get(selectedPosition).login)) {
                                favoritesList.remove(i);
                                break;
                            }
                        }
                    }
                } else
                    favoritesList.remove(selectedPosition);
                //favoritesList.remove(userList.get(selectedPosition));
            }
        }

        if (isFavoritesOpen)
            favoritesListAdapter.notifyDataSetChanged();
        else if (isHistoryOpen)
            historyListAdapter.notifyDataSetChanged();
        else
            userListAdapter.notifyDataSetChanged();
        fromPopUp = false;
    }

    class UsersRecieveing extends AsyncTask<String, Void, Integer> { //Получаем список пользователей

        protected Integer doInBackground(String... name) {
            isRecieving = true; //Устанавливаем, что получаем данные, чтобы не пытаться получить новые, пока не получим эти
            Call<Object> call = searchSendIntf.search(name[0], searchPage, Const.perPageUsers);

            try {
                Response<Object> response = call.execute(); //Обращаемся к ГитХабу за списком пользователей

                String anwser = response.body().toString();

                String count = anwser.substring(anwser.indexOf("total_count") + 12, anwser.indexOf(".0,")); //Заносим количество пользрвателей всего с этим логином
                usersCount = Integer.parseInt(count);

                anwser = anwser.substring(anwser.indexOf("items=")+6, anwser.length()-1);

                if (anwser.length() < 3) //Если нет таких пользователей
                    return -1;

                anwser = toJsonString(anwser, 2);

                ArrayList<Map<String, String>> userMapList = gson.fromJson(anwser, ArrayList.class); //Преобразовывать через toJson

                String login="";
                String avatarUrl="";

                for (int i=0; i<userMapList.size(); i++) {
                    for (Map.Entry e : userMapList.get(i).entrySet()) {
                        if (e.getKey().equals("login"))
                            login = e.getValue().toString();
                        if (e.getKey().equals("avatar_url"))
                            avatarUrl = e.getValue().toString();
                    }

                    userList.add(new UserShort(login, avatarUrl));
                }

            } catch (IOException e) {
                return -2;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer asyncRes) {
            super.onPostExecute(asyncRes);

            if (asyncRes == -1) { //Если список пуст
                layoutLoading.setVisibility(View.GONE); //Устанавливаем анимацию
                Toast.makeText(MainActivity.this, "Nothing was found", Toast.LENGTH_LONG).show();
                isRecieving = false; //Закончили получать данные
                return;
            }

            if (asyncRes == -2) { //Если нет интернета
                isRecieving = false; //Закончили получать данные
                if (!internetConnection) //Ничего не надо менять, так как все уже становлено было раньше
                    return;
                internetConnection = false;
                layoutLoading.setVisibility(View.GONE); //Скрываем анимацию
                Toast.makeText(MainActivity.this, "Internet connection required", Toast.LENGTH_LONG).show(); //Уведомляем пользователя о необходимости интернета
                internetConnection = false;
                return;
            }

            internetConnection = true;
            searchPage++; //Увеличиваем номер страницы, которую будем искать в следующий раз

            ivLoading.clearAnimation(); //Снимаем анимацию и прячем иконку загрузки
            layoutLoading.setVisibility(View.GONE);
            lvResults.setVisibility(View.VISIBLE);
            userListAdapter.notifyDataSetChanged(); //Данные изменились
            isRecieving = false; //Закончили получать данные
        }
    }

    public static String toJsonString(String anwser, int startBranches) { //Приводим строку ответа к строке JSON
        anwser=anwser.replaceAll(", ", "\", \"");
        anwser=anwser.replaceAll("=", "\"=\"");
        anwser=anwser.replaceAll("v\"=\"3\",", "v=3\","); //Во всех avatar_url есть v=3

        anwser=anwser.substring(0, startBranches) + "\"" + anwser.substring(startBranches); //Заносим в кавычки первый логин и последний результат
        anwser=anwser.substring(0, anwser.length()-startBranches) + "\"" + anwser.substring(anwser.length()-startBranches);
        anwser = anwser.replaceAll("\\}\", \"\\{login\"=", "\"\\}, \\{\"login\"="); //Заносим в кавычки последний элемент одного варианта и первый другого

        return anwser;
    }
}
