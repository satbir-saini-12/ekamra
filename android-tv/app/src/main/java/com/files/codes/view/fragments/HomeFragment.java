package com.files.codes.view.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.files.codes.AppConfig;
import com.files.codes.CardPresenter;
import com.ekamra.tv.R;
import com.files.codes.database.DatabaseHelper;
import com.files.codes.model.Channel;
import com.files.codes.model.HomeContent;
import com.files.codes.model.HomeContentList;
import com.files.codes.model.PlaybackModel;
import com.files.codes.model.VideoContent;
import com.files.codes.model.api.ApiService;
import com.files.codes.utils.Constants;
import com.files.codes.utils.LoginAlertDialog;
import com.files.codes.utils.PaidDialog;
import com.files.codes.utils.PreferenceUtils;
import com.files.codes.utils.RetrofitClient;
import com.files.codes.view.ErrorActivity;
import com.files.codes.view.HomeActivity;
import com.files.codes.view.fragments.testFolder.HomeNewActivity;
import com.files.codes.view.PlayerActivity;
import com.files.codes.view.VideoDetailsActivity;
import com.files.codes.view.VideoPlaybackActivity;
import com.files.codes.view.presenter.SliderCardPresenter;
import com.files.codes.view.presenter.TvPresenter;
import com.files.codes.viewmodel.HomeContentViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends RowsSupportFragment {
    private final String TAG = HomeActivity.class.getSimpleName();
    //private BackgroundHelper bgHelper;
    private ArrayObjectAdapter rowsAdapter;
    private CardPresenter cardPresenter;
    private View v;
    private HomeNewActivity activity;
    private HomeContentViewModel homeContentViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: " );
        super.onCreate(savedInstanceState);
        //bgHelper = new BackgroundHelper(getActivity());
        activity = (HomeNewActivity) getActivity();
        //activity.showLogo();
        setOnItemViewClickedListener(getDefaultItemViewClickedListener());
        setOnItemViewSelectedListener(getDefaultItemSelectedListener());

        //home content live data
        homeContentViewModel = new ViewModelProvider(getActivity()).get(HomeContentViewModel.class);
        homeContentViewModel.getHomeContentLiveData().observe(getActivity(), new Observer<HomeContentList>() {
            @Override
            public void onChanged(HomeContentList homeContentList) {
                if (homeContentList != null){
                    loadRows(homeContentList.getHomeContentList());
                     }else {
                    loadHomeContentDataFromServer();
                }
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    private void loadHomeContentDataFromServer() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService api = retrofit.create(ApiService.class);
        Call<List<HomeContent>> call = api.getHomeContent(AppConfig.API_KEY);
        call.enqueue(new Callback<List<HomeContent>>() {
            @Override
            public void onResponse(Call<List<HomeContent>> call, Response<List<HomeContent>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<HomeContent> homeContents = response.body();

                    if (homeContents.size() > 0) {
                        HomeContentList list = new HomeContentList();
                        list.setHomeContentId(1);
                        list.setHomeContentList(homeContents);
                        homeContentViewModel.insert(list);

                        //save latest movies in constant file for temporary
                        // to add/update channel
                        if (homeContents.get(2).getContent() != null) {
                            Constants.movieList.clear();
                            Constants.movieList = homeContents.get(2).getContent();
                        }
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<HomeContent>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRows(List<HomeContent> list) {
        if (list != null) {
            rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
            cardPresenter = new CardPresenter();
            SliderCardPresenter sliderCardPresenter = new SliderCardPresenter();
            TvPresenter tvPresenter = new TvPresenter();


            for (int i = 0; i < list.size(); i++) {
                HomeContent homeContent = list.get(i);
                ArrayObjectAdapter listRowAdapter;
                HeaderItem header;

                if (homeContent.getType().equalsIgnoreCase("slider")) {
                    // load slider
                    listRowAdapter = new ArrayObjectAdapter(sliderCardPresenter);
                    header = new HeaderItem(0, "");
                } else if (homeContent.getType().equalsIgnoreCase("tv")) {
                    //load tv layout
                    listRowAdapter = new ArrayObjectAdapter(tvPresenter);
                    header = new HeaderItem(1, homeContent.getTitle());
                }else if (homeContent.getId().equalsIgnoreCase("2")){
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    header = new HeaderItem(2, homeContent.getTitle());
                }else if (homeContent.getType().equalsIgnoreCase("tvseries")){
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    header = new HeaderItem(3, homeContent.getTitle());
                } else {
                    listRowAdapter = new ArrayObjectAdapter(cardPresenter);
                    header = new HeaderItem(i, list.get(i).getTitle());
                }
                //for (int j = 0; j < NUM_COLS; j++) {
                for (int j = 0; j < list.get(i).getContent().size(); j++) {
                    VideoContent videoContent = list.get(i).getContent().get(j);
                    if (list.get(i).getType().equalsIgnoreCase("tv")) {
                        videoContent.setType("tv");
                    } else if (list.get(i).getType().equalsIgnoreCase("movie")) {
                        videoContent.setType("movie");
                    } else if (list.get(i).getType().equalsIgnoreCase("tvseries")) {
                        videoContent.setType("tvseries");
                    } else if (list.get(i).getType().equalsIgnoreCase("slider")) {
                        if (videoContent.getIsTvseries().equals("1")) {
                            videoContent.setType("tvseries");
                        } else if (videoContent.getIsTvseries().equals("0")) {
                            videoContent.setType("movie");
                        }
                    }

                    listRowAdapter.add(videoContent);
                }
                rowsAdapter.add(new ListRow(header, listRowAdapter));
            }

            setAdapter(rowsAdapter);
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());

            setCustomPadding();
        } else {
            Intent intent = new Intent(activity, ErrorActivity.class);
            startActivity(intent);
            activity.finish();
        }

    }

    private void setCustomPadding() {
        //getView().setPadding(Utils.dpToPx(-24, getContext()), Utils.dpToPx(70, getContext()), 0, 0);
    }

    // click listener
    private OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o,
                                      RowPresenter.ViewHolder viewHolder2, Row row) {

                VideoContent videoContent = (VideoContent) o;
                DatabaseHelper db = new DatabaseHelper(getContext());
                String status =db.getActiveStatusData() != null ?db.getActiveStatusData().getStatus() : "inactive";

                if (videoContent.getType().equals("tv")) {
                    if (videoContent.getIsPaid().equals("1")) {
                        if (PreferenceUtils.isLoggedIn(getActivity())) {
                            if ( status.equals("active")) {
                                PlaybackModel model = new PlaybackModel();
                                model.setId(Long.parseLong(videoContent.getId()));
                                model.setTitle(videoContent.getTitle());
                                model.setDescription(videoContent.getDescription());
                                model.setVideoType(videoContent.getStreamFrom());
                                model.setCategory("tv");
                                model.setVideoUrl(videoContent.getStreamUrl());
                                model.setCardImageUrl(videoContent.getPosterUrl());
                                model.setBgImageUrl(videoContent.getThumbnailUrl());
                                model.setIsPaid(videoContent.getIsPaid());

                                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                                intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, model);
                                startActivity(intent);
                            } else {
                                //saved data is not valid, because it was saved more than 2 hours ago
                                PreferenceUtils.updateSubscriptionStatus(getActivity());
                                PaidDialog dialog = new PaidDialog(getContext());
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                                dialog.show();
                            }
                        }else {
                            // user is not logged in
                            // show an alert dialog
                            LoginAlertDialog dialog = new LoginAlertDialog(getActivity());
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            dialog.show();
                        }
                    } else {
                        PlaybackModel model = new PlaybackModel();
                        model.setId(Long.parseLong(videoContent.getId()));
                        model.setTitle(videoContent.getTitle());
                        model.setDescription(videoContent.getDescription());
                        model.setVideoType(videoContent.getStreamFrom());
                        model.setCategory("tv");
                        model.setVideoUrl(videoContent.getStreamUrl());
                        model.setCardImageUrl(videoContent.getPosterUrl());
                        model.setBgImageUrl(videoContent.getThumbnailUrl());
                        model.setIsPaid(videoContent.getIsPaid());

                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, model);
                        startActivity(intent);
                    }

                } else {
                    String type = videoContent.getType();
                    if (videoContent.getIsTvseries().equalsIgnoreCase("1")){
                        type = "tvseries";
                    }
                    Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                    intent.putExtra("id", videoContent.getId());
                    intent.putExtra("type", type);
                    intent.putExtra("thumbImage", videoContent.getThumbnailUrl());

                    //poster transition
                    ImageView imageView = ((ImageCardView) viewHolder.view).getMainImageView();
                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                            imageView, VideoDetailsFragment.TRANSITION_NAME).toBundle();

                    startActivity(intent, bundle);
                }
            }
        };
    }

    //listener for setting blur background each time when the item will select.
    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, final Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {

                if (item instanceof VideoContent) {
                    //bgHelper = new BackgroundHelper(getActivity());
                    //bgHelper.prepareBackgroundManager();
                    //bgHelper.startBackgroundTimer(((VideoContent) item).getPosterUrl());
                } else if (item instanceof Channel) {
                    //bgHelper = new BackgroundHelper(getActivity());
                    // bgHelper.prepareBackgroundManager();
                    //bgHelper.startBackgroundTimer(((Channel) item).getPosterUrl());

                }

            }
        };
    }
}
