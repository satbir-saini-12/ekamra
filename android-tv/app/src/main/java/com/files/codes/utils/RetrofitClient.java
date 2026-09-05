package com.files.codes.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.files.codes.AppConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String API_EXTENSION = "/v130/"; //v130
    private static final String API_USER_NAME = "admin";
    private static final String API_PASSWORD = "1234";

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(API_USER_NAME, API_PASSWORD)).build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.API_SERVER_URL + API_EXTENSION)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
