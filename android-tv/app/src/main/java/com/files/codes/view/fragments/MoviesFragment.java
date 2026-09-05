package com.files.codes.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.files.codes.AppConfig;
import com.ekamra.tv.R;
import com.files.codes.database.movie.MovieViewModel;
import com.files.codes.model.Movie;
import com.files.codes.model.MovieList;
import com.files.codes.model.api.ApiService;
import com.files.codes.utils.NetworkInst;
import com.files.codes.utils.RetrofitClient;
import com.files.codes.view.ErrorActivity;
import com.files.codes.view.fragments.testFolder.HomeNewActivity;
import com.files.codes.view.VideoDetailsActivity;
import com.files.codes.view.fragments.testFolder.GridFragment;
import com.files.codes.view.presenter.VerticalCardPresenter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MoviesFragment extends GridFragment {
    public static final String MOVIE = "movie";
    private static final String TAG = MoviesFragment.class.getSimpleName();
    private static final int NUM_COLUMNS = 8;
    private int pageCount = 2;
    private boolean dataAvailable = true;
    //private BackgroundHelper bgHelper;
    private List<Movie> movies = new ArrayList<>();
    private ArrayObjectAdapter mAdapter;
    private HomeNewActivity activity;
    private MovieViewModel movieViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: " );
        super.onCreate(savedInstanceState);
        activity = (HomeNewActivity) getActivity();
        //activity.hideLogo();
        //setTitle(getResources().getString(R.string.movie));
        //bgHelper = new BackgroundHelper(getActivity());
        setOnItemViewClickedListener(getDefaultItemViewClickedListener());
        setOnItemViewSelectedListener(getDefaultItemSelectedListener());

        // setup
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new ArrayObjectAdapter(new VerticalCardPresenter(MOVIE));
        setAdapter(mAdapter);

        //get data from local database
        movieViewModel = new ViewModelProvider(getActivity()).get(MovieViewModel.class);
        movieViewModel.getMovieLiveData().observe(getActivity(), new Observer<MovieList>() {
            @Override
            public void onChanged(MovieList movieList) {
                if (movieList != null){
                    populateView(movieList.getMovieList());
                }
            }
        });

    }

    // click listener
    private OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o,
                                      RowPresenter.ViewHolder viewHolder2, Row row) {
                Movie movie = (Movie) o;
                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra("id", movie.getVideosId());
                intent.putExtra("type", "movie");
                intent.putExtra("thumbImage", movie.getThumbnailUrl());

                ImageView imageView = ((ImageCardView) viewHolder.view).getMainImageView();
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        imageView, VideoDetailsFragment.TRANSITION_NAME).toBundle();
                startActivity(intent, bundle);

            }
        };
    }

    // selected listener for setting blur background each time when the item will select.
    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, final Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                // pagination
                if (dataAvailable) {
                    int itemPos = mAdapter.indexOf(item);
                    if (itemPos == movies.size() - 1) {
                        pageCount++;
                        fetchMovieData(pageCount);
                    }
                }
                if (item instanceof Movie) {
                   /* bgHelper = new BackgroundHelper(getActivity());
                    bgHelper.prepareBackgroundManager();
                    bgHelper.startBackgroundTimer(((Movie) item).getThumbnailUrl());*/
                }
            }
        };
    }

    public void fetchMovieData(int pageCount) {

        if (!new NetworkInst(activity).isNetworkAvailable()) {
            Intent intent = new Intent(activity, ErrorActivity.class);
            startActivity(intent);
            activity.finish();
            return;
        }

//        final SpinnerFragment mSpinnerFragment = new SpinnerFragment();
//        final FragmentManager fm = getFragmentManager();
//        fm.beginTransaction().add(R.id.custom_frame_layout, mSpinnerFragment).commit();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService api = retrofit.create(ApiService.class);
        Call<List<Movie>> call = api.getMovies(AppConfig.API_KEY, pageCount);
        call.enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.code() == 200) {
                    Log.e(TAG, "onResponse: " + response.code() );
                    List<Movie> movieList = response.body();
                    if (movieList.size() <= 0) {
                        dataAvailable = false;
                        if (pageCount != 2) {
                            Toast.makeText(activity, getResources().getString(R.string.no_more_data_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                    populateView(movieList);
                    // hide the spinner
                   // fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                t.printStackTrace();
                // hide the spinner
                Toast.makeText(activity, t.getMessage(), Toast.LENGTH_SHORT).show();
               // fm.beginTransaction().remove(mSpinnerFragment).commitAllowingStateLoss();
            }
        });
    }

    private void populateView(List<Movie> movieList){
        if (movieList  != null && movieList.size() > 0){
            for (Movie movie : movieList) {
                mAdapter.add(movie);
            }
            mAdapter.notifyArrayItemRangeChanged(movieList.size() - 1, movieList.size() + movies.size());
            movies.addAll(movieList);
            setAdapter(mAdapter);
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        movies = new ArrayList<>();
        pageCount = 1;
        dataAvailable = true;

    }

}
