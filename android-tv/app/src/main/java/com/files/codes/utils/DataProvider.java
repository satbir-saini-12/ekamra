package com.files.codes.utils;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.files.codes.AppConfig;

import com.files.codes.database.TvSeries.TvSeriesViewModel;
import com.files.codes.database.live_tv.LiveTvList;
import com.files.codes.database.live_tv.LiveTvViewModel;
import com.files.codes.database.movie.MovieViewModel;
import com.files.codes.model.HomeContent;
import com.files.codes.model.HomeContentList;
import com.files.codes.model.LiveTv;
import com.files.codes.model.Movie;
import com.files.codes.model.MovieList;
import com.files.codes.model.api.ApiService;
import com.files.codes.model.config.Configuration;
import com.files.codes.viewmodel.HomeContentViewModel;
import com.files.codes.viewmodel.config.ConfigViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DataProvider {
    private static final String TAG = "DataProvider";
    private Activity context;
    private PublishProcessor<Integer> paginator;
    private PublishProcessor<Integer> mPublishProcessor;
    private CompositeDisposable disposables;
    private int pageCount = 1;
    private int NUMBER_OF_THREADS = 4;
    private ExecutorService executorService;

    public DataProvider(Activity context, CompositeDisposable disposables) {
        this.context = context;
        this.disposables = disposables;
        paginator = PublishProcessor.create();
        mPublishProcessor = PublishProcessor.create();
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }


    public void getAndSaveHomeContentDataFromServer(ViewModelStoreOwner activity) {
        Log.e(TAG, "getAndSaveHomeContentDataFromServer: " );
        HomeContentViewModel homeContentViewModel = new ViewModelProvider(activity).get(HomeContentViewModel.class);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService api = retrofit.create(ApiService.class);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Call<List<HomeContent>> call = api.getHomeContent(AppConfig.API_KEY);
                call.enqueue(new Callback<List<HomeContent>>() {
                    @Override
                    public void onResponse(Call<List<HomeContent>> call, Response<List<HomeContent>> response) {
                        Log.e(TAG, "onResponse: response code: " +response.code());
                        if (response.code() == 200 && response.body() != null) {
                            List<HomeContent> homeContents = response.body();
                            if (homeContents.size() > 0) {
                                HomeContentList list = new HomeContentList();
                                list.setHomeContentId(1);
                                list.setHomeContentList(homeContents);
                                homeContentViewModel.insert(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HomeContent>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getLocalizedMessage() );
                    }
                });

            }
        });
    }

    //config data
    public void loadConfigDataFromServer(ViewModelStoreOwner activity){
        ConfigViewModel viewModel = new ViewModelProvider(activity).get(ConfigViewModel.class);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<Configuration> call = apiService.getConfiguration(AppConfig.API_KEY);
        call.enqueue(new Callback<Configuration>() {
            @Override
            public void onResponse(Call<Configuration> call, Response<Configuration> response) {
                if (response.code() ==200 && response.body() != null){
                    Configuration configuration = response.body();
                    configuration.setId(1);
                    viewModel.insert(configuration);
                    Log.e(TAG, "onResponse: config data updated" );
                }
            }

            @Override
            public void onFailure(Call<Configuration> call, Throwable t) {
                Log.e(TAG, "onFailure: config data failed"  + t.getLocalizedMessage() );
            }
        });
    }

    public void getMoviesFromServer(ViewModelStoreOwner activity) {
        MovieViewModel viewModel = new ViewModelProvider(activity).get(MovieViewModel.class);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService api = retrofit.create(ApiService.class);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Call<List<Movie>> call = api.getMovies(AppConfig.API_KEY, 1);
                call.enqueue(new Callback<List<Movie>>() {
                    @Override
                    public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                        if (response.code() == 200 && response.body() != null) {
                            MovieList movieList = new MovieList();
                            movieList.setId(1);
                            movieList.setMovieList(response.body ());
                            viewModel.insert(movieList);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Movie>> call, Throwable t) {

                    }
                });
            }
        });
    }

    private Retrofit getRetrofit(){
        return RetrofitClient.getRetrofitInstance();
    }

    public void getTvSeriesDataFromServer(ViewModelStoreOwner activity){
        TvSeriesViewModel viewModel = new ViewModelProvider(activity).get(TvSeriesViewModel.class);
        ApiService api = getRetrofit().create(ApiService.class);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Call<List<Movie>> call = api.getTvSeries(AppConfig.API_KEY, pageCount);
                call.enqueue(new Callback<List<Movie>>() {
                    @Override
                    public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                        if (response.code() == 200 && response.body() != null){
                            MovieList movieList = new MovieList();
                            movieList.setId(1);
                            movieList.setMovieList(response.body());
                            viewModel.insert(movieList);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Movie>> call, Throwable t) {
                        //failed to load TV Series data from server
                    }
                });
            }
        });
    }

    public void getLiveTvDataFromServer(ViewModelStoreOwner activity){
        LiveTvViewModel viewModel = new ViewModelProvider(activity).get(LiveTvViewModel.class);
        ApiService api = getRetrofit().create(ApiService.class);
        Call<List<LiveTv>> call = api.getLiveTvCategories(AppConfig.API_KEY);
        call.enqueue(new Callback<List<LiveTv>>() {
            @Override
            public void onResponse(Call<List<LiveTv>> call, Response<List<LiveTv>> response) {
                if (response.code() == 200 && response.body() != null){
                    LiveTvList liveTvList = new LiveTvList();
                    liveTvList.setId(1);
                    liveTvList.setLiveTvList(response.body());
                    viewModel.insert(liveTvList);
                }
            }

            @Override
            public void onFailure(Call<List<LiveTv>> call, Throwable t) {

            }
        });
    }
}
