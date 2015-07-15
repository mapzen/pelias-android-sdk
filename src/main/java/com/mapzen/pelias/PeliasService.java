package com.mapzen.pelias;

import com.mapzen.pelias.gson.Result;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface PeliasService {
    @GET("/suggest")
    void getSuggest(@Query("input") String query, @Query("lat") String lat, @Query("lon") String lon, Callback<Result> callback);

    @GET("/search")
    void getSearch(@Query("input") String query, @Query("lat") String lat, @Query("lon") String lon, Callback<Result> callback);

    @GET("/reverse")
    void getReverse(@Query("lat") String lat, @Query("lon") String lon, Callback<Result> callback);

    @GET("/doc")
    void getDoc(@Query("id") String typeAndId, Callback<Result> callback);
}
