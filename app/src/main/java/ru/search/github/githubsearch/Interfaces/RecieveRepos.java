package ru.search.github.githubsearch.Interfaces;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Илья on 05.05.2017.
 */

public interface RecieveRepos {
    @GET("/users/{username}/repos")
    Call<Object> recieve(@Path("username") String username, @Query("sort") String sort, @Query("page") int page, @Query("per_page") int perPage);
}
