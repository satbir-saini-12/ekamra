package com.files.codes.view.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.app.SearchSupportFragment;
import androidx.leanback.widget.ObjectAdapter;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.SpeechRecognitionCallback;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.files.codes.AppConfig;
import com.ekamra.tv.R;
import com.files.codes.model.Movie;
import com.files.codes.model.PlaybackModel;
import com.files.codes.model.SearchContent;
import com.files.codes.model.SearchModel;
import com.files.codes.model.TvModel;
import com.files.codes.model.api.ApiService;
import com.files.codes.utils.RetrofitClient;
import com.files.codes.utils.Utils;
import com.files.codes.view.PlayerActivity;
import com.files.codes.view.VideoDetailsActivity;
import com.files.codes.view.VideoPlaybackActivity;
import com.files.codes.view.presenter.SearchCardPresenter;
import com.files.codes.view.presenter.TvSearchPresenter;

//import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class SearchFragment extends SearchSupportFragment implements SearchSupportFragment.SearchResultProvider, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "SearchFragment";

    private static final int REQUEST_SPEECH = 0x00000010;
    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final long SEARCH_DELAY_MS = 1000L;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private int page_number = 1;
    private String mQuery;
    private List<SearchContent> mItems = new ArrayList<>();
    private List<SearchModel> searchList = new ArrayList<>();
    private String tvHeader = "";
    private String tvSeriesHeader = "";
    private String movieHeader = "";


    private final Runnable mDelayedLoad = new Runnable() {
        @Override
        public void run() {
            //loadRows();
            getQueryData();

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemViewClickedListener(getDefaultItemViewClickedListener());

        if (!Utils.hasPermission(getActivity(), Manifest.permission.RECORD_AUDIO)) {
            // SpeechRecognitionCallback is not required and if not provided recognition will be handled
            // using internal speech recognizer, in which case you must have RECORD_AUDIO permission
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    Log.e(TAG, "recognizeSpeech");
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot find activity for speech recognizer", e);
                    }
                }
            });
        }
    }

    public void focusOnSearch() {
        getView().findViewById(androidx.leanback.R.id.lb_search_bar).requestFocus();
    }

    // click listener
    private OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o,
                                      RowPresenter.ViewHolder viewHolder2, Row row) {

                SearchContent searchContent = (SearchContent) o;
                switch (searchContent.getType()) {
                    case "tv": {
                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        PlaybackModel video = new PlaybackModel();
                        video.setId(Long.parseLong(searchContent.getId()));
                        video.setTitle(searchContent.getTitle());
                        video.setDescription(searchContent.getDescription());
                        video.setCategory("tv");
                        video.setVideoUrl(searchContent.getStreamUrl());
                        video.setVideoType(searchContent.getStreamFrom());
                        video.setBgImageUrl(searchContent.getThumbnailUrl());
                        video.setCardImageUrl(searchContent.getThumbnailUrl());

                        intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, video);
                        startActivity(intent);
                        break;
                    }
                    case "tvseries": {
                        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                        intent.putExtra("id", searchContent.getId());
                        intent.putExtra("type", "tvseries");
                        intent.putExtra("thumbImage", searchContent.getThumbnailUrl());
                        startActivity(intent);
                        break;
                    }
                    case "movie": {
                        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                        intent.putExtra("id", searchContent.getId());
                        intent.putExtra("type", "movie");
                        intent.putExtra("thumbImage", searchContent.getThumbnailUrl());
                        startActivity(intent);
                        break;
                    }
                }
            }
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult requestCode=" + requestCode +
                " resultCode=" + resultCode +
                " data=" + data);

        switch (requestCode) {
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setSearchQuery(data, true);
                        break;
                    case RecognizerIntent.RESULT_CLIENT_ERROR:
                        Log.e(TAG, Integer.toString(requestCode));
                    default:
                        // If recognizer is canceled or failed, keep focus on the search orb
                        if (FINISH_ON_RECOGNIZER_CANCELED) {
                            if (!hasResults()) {
                                Log.e(TAG, "Voice search canceled");
                                getView().findViewById(androidx.leanback.R.id.lb_search_bar_speech_orb).requestFocus();
                            }
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0;
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        Log.e(TAG, "getResultsAdapter");
        // mRowsAdapter (Search result) has prepared in loadRows method
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.e(TAG, String.format("Search Query Text Change %s", newQuery));
        Log.e(TAG, "onQueryTextChange: " + newQuery );
        loadQueryWithDelay(newQuery, SEARCH_DELAY_MS);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.e(TAG, String.format("Search Query Text Submit %s", query));
        // No need to delay(wait) loadQuery, since the query typing has completed.
        loadQueryWithDelay(query, 0);
        return true;
    }

    private void loadQueryWithDelay(String query, long delay) {
        mHandler.removeCallbacks(mDelayedLoad);
        if (!TextUtils.isEmpty(query) && !query.equals("")) {
            mQuery = query;
            mHandler.postDelayed(mDelayedLoad, delay);
            Log.e(TAG,  "Handler started");
        }
    }

    private void getQueryData() {
        final String query = mQuery;
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService searchApi = retrofit.create(ApiService.class);
        Call<SearchModel> call = searchApi.getSearchData(AppConfig.API_KEY, query, page_number, "movieserieslive");
        call.enqueue(new Callback<SearchModel>() {
            @Override
            public void onResponse(Call<SearchModel> call,  Response<SearchModel> response) {
                Log.e(TAG,  "response: " + response.code());
                mItems = new ArrayList<>();

                List<Movie> movieResult = new ArrayList<>();
                List<Movie> tvSeriesResult = new ArrayList<>();
                List<TvModel> tvResult = new ArrayList<>();

                if (response.code() == 200) {
                    if (response.body().getTvChannels().size() != 0) {
                        tvHeader = "Live TV";
                        tvResult.clear();
                        tvResult = response.body().getTvChannels();
                        for (TvModel video : tvResult) {
                            String id = video.getLiveTvId();
                            String title = video.getTvName();
                            String description = video.getDescription();
                            String type = "tv";
                            String streamUrl = video.getStreamUrl();
                            String streamFrom = video.getStreamFrom();
                            String thumbnailUrl = video.getPosterUrl();
                            SearchContent searchContent = new SearchContent(id, title, description, type, streamUrl, streamFrom, thumbnailUrl);
                            mItems.add(searchContent);
                        }
                    }

                    if (response.body().getMovie().size() != 0) {
                        movieHeader = "Movies";
                        movieResult.clear();
                        movieResult = response.body().getMovie();
                        for (Movie video : movieResult) {
                            String id = video.getVideosId();
                            String title = video.getTitle();
                            String description = video.getDescription();
                            String type = "movie";
                            String streamUrl = "";
                            String streamFrom = "";
                            String thumbnailUrl = video.getThumbnailUrl();
                            SearchContent searchContent = new SearchContent(id, title, description, type, streamUrl, streamFrom, thumbnailUrl);
                            mItems.add(searchContent);
                        }
                    }

                    if (response.body().getTvseries().size() != 0) {
                        tvSeriesHeader = "TV Series";
                        tvSeriesResult.clear();
                        tvSeriesResult = response.body().getTvseries();
                        for (Movie video : tvSeriesResult) {
                            String id = video.getVideosId();
                            String title = video.getTitle();
                            String description = video.getDescription();
                            String type = "tvseries";
                            String streamUrl = "";
                            String streamFrom = "";
                            String thumbnailUrl = video.getPosterUrl();
                            SearchContent searchContent = new SearchContent(id, title, description, type, streamUrl, streamFrom, thumbnailUrl);
                            mItems.add(searchContent);
                        }
                    }

                    loadRows(movieResult, tvSeriesResult, tvResult);
                }

            }

            @Override
            public void onFailure(Call<SearchModel> call, Throwable t) {
                Log.e(TAG, "response : " + t.getLocalizedMessage());
            }
        });

    }


    @SuppressLint("StaticFieldLeak")
    private void loadRows(final List<Movie> movieResult, final List<Movie> tvSeriesResult, final List<TvModel> tvResult) {
        // offload processing from the UI thread
        new AsyncTask<String, Void, List<ListRow>>() {
            private final String query = mQuery;

            @Override
            protected void onPreExecute() {
                mRowsAdapter.clear();
            }

            @Override
            protected List<ListRow> doInBackground(String... params) {
                final List<SearchContent> result = new ArrayList<>();

                for (SearchContent video : mItems) {
                    // Main logic of search is here.
                    // Just check that "query" is contained in Title or Description or not. (NOTE: excluded studio information here)
                    if (video.getTitle().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))
                            || video.getDescription().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
                        result.add(video);
                    }
                }

                List<ListRow> listRows = new ArrayList<>();

                ArrayObjectAdapter movieAdapter = new ArrayObjectAdapter(new SearchCardPresenter());
                ArrayObjectAdapter tvSeriesAdapter = new ArrayObjectAdapter(new SearchCardPresenter());
                ArrayObjectAdapter tvAdapter = new ArrayObjectAdapter(new TvSearchPresenter());

                for (SearchContent video : mItems) {
                    if (video.getType().equalsIgnoreCase("movie")) {
                        movieAdapter.add(video);
                    } else if (video.getType().equalsIgnoreCase("tvseries")) {
                        tvSeriesAdapter.add(video);
                    } else if (video.getType().equalsIgnoreCase("tv")) {
                        tvAdapter.add(video);
                    }
                }

                listRows.add(new ListRow(new HeaderItem(movieHeader), movieAdapter));
                listRows.add(new ListRow(new HeaderItem(tvSeriesHeader), tvSeriesAdapter));
                listRows.add(new ListRow(new HeaderItem(tvHeader), tvAdapter));

                return listRows;
            }

            @Override
            protected void onPostExecute(List<ListRow> listRow) {
                for (ListRow listRow1 : listRow) {
                    mRowsAdapter.add(listRow1);
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void loadChannelRows(List<Movie> movieResult, List<Movie> tvSeriesResult, List<TvModel> tvResult) {

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        SearchCardPresenter searchCardPresenter = new SearchCardPresenter();
        HeaderItem header;

        if (movieResult.size() != 0) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(searchCardPresenter);

            for (Movie movie : movieResult) {
                listRowAdapter.add(movie);
            }

            rowsAdapter.add(new ListRow(new HeaderItem(0, "Movies"), listRowAdapter));

        }

        if (tvSeriesResult.size() != 0) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(searchCardPresenter);

            for (Movie movie : tvSeriesResult) {
                listRowAdapter.add(movie);
            }
            rowsAdapter.add(new ListRow(new HeaderItem(0, "Tv Series"), listRowAdapter));
        }

        if (tvResult.size() != 0) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(searchCardPresenter);

            for (TvModel tvModel : tvResult) {
                listRowAdapter.add(tvModel);
            }
            rowsAdapter.add(new ListRow(new HeaderItem(0, "Live TV"), listRowAdapter));
        }

        mRowsAdapter.add(rowsAdapter);
    }

    // The name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "com.mytv.tvbox.tvleanback";
    // Base of all URIs that will be used to contact the content provider.

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
         Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("video").build();
         return new CursorLoader(getActivity(), CONTENT_URI, null, "", new String[]{}, null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}