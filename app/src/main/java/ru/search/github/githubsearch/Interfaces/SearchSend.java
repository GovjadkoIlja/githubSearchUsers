package ru.search.github.githubsearch.Interfaces;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


/**
 * Created by Илья on 03.05.2017.
 */

public interface SearchSend {
    @GET("/search/users")
    Call<Object> search(@Query("q") String name, @Query("page") int page, @Query("per_page") int perPage);
}
