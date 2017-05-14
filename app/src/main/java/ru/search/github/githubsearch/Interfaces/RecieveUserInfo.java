package ru.search.github.githubsearch.Interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Илья on 04.05.2017.
 */

public interface RecieveUserInfo {
    @GET("/users/{username}")
    Call<Object> recieve(@Path("username") String name);
}
