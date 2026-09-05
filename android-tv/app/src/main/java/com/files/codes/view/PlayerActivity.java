package com.files.codes.view;

import static android.view.View.VISIBLE;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.ekamra.tv.R;
import com.files.codes.database.DatabaseHelper;
import com.files.codes.model.PlaybackModel;
import com.files.codes.model.Video;
import com.files.codes.model.movieDetails.Subtitle;
import com.files.codes.utils.ToastMsg;
import com.files.codes.view.adapter.ServerAdapter;
import com.files.codes.view.adapter.SubtitleListAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends Activity {
    private static final String TAG = "PlayerActivity";
    private static final String CLASS_NAME = "com.oxoo.spagreen.ui.activity.PlayerActivity";
    private PlayerView exoPlayerView;
    private SimpleExoPlayer player;
    private RelativeLayout rootLayout;
    private MediaSource mediaSource;
    private boolean isPlaying;
    private List<Video> videos = new ArrayList<>();
    private Video video = null;
    private String url = "";
    private String videoType = "";
    private String category = "";
    private int visible;
    private ImageButton serverButton, fastForwardButton, subtitleButton;
    private TextView movieTitleTV, movieDescriptionTV;
    private ImageView posterImageView, posterImageViewForTV;
    private RelativeLayout seekBarLayout;
    private TextView liveTvTextInController;
    private ProgressBar progressBar;
    private PowerManager.WakeLock wakeLock;
    private MediaSession session;

    private long mChannelId;
    private long mStartingPosition;
    private PlaybackModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mChannelId = getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_CHANNEL_ID, -1L);
        mStartingPosition = getIntent().getLongExtra(VideoPlaybackActivity.EXTRA_POSITION, -1L);

        model = (PlaybackModel) getIntent().getSerializableExtra(VideoPlaybackActivity.EXTRA_VIDEO);


        assert model != null;
        url = model.getVideoUrl();
        videoType = model.getVideoType();
        category = model.getCategory();
        if (model.getVideo() != null)
            video = model.getVideo();
        if (model.getCategory().equals("movie") && mChannelId > -1L && model.getIsPaid().equals("1")) {
            //Paid Content from Channel
            //check user has subscription or not
            //if not, send user to VideoDetailsActivity
            DatabaseHelper db = new DatabaseHelper(PlayerActivity.this);
            final String status = db.getActiveStatusData() != null ? db.getActiveStatusData().getStatus() : "inactive";
            if (!status.equals("active")) {
                Intent intent = new Intent(PlayerActivity.this, VideoDetailsActivity.class);
                intent.putExtra("type", model.getCategory());
                intent.putExtra("id", model.getMovieId());
                intent.putExtra("thumbImage", model.getCardImageUrl());
                startActivity(intent, null);
                finish();
            }
        }

        intiViews();
        initVideoPlayer(url, videoType);
    }

    private void intiViews() {
        progressBar = findViewById(R.id.progress_bar);
        exoPlayerView = findViewById(R.id.player_view);
        rootLayout = findViewById(R.id.root_layout);
        movieTitleTV = findViewById(R.id.movie_title);
        movieDescriptionTV = findViewById(R.id.movie_description);
        posterImageView = findViewById(R.id.poster_image_view);
        posterImageViewForTV = findViewById(R.id.poster_image_view_for_tv);
        serverButton = findViewById(R.id.img_server);
        subtitleButton = findViewById(R.id.img_subtitle);
        fastForwardButton = findViewById(R.id.exo_ffwd);
        liveTvTextInController = findViewById(R.id.live_tv);
        seekBarLayout = findViewById(R.id.seekbar_layout);
        if (category.equalsIgnoreCase("tv")) {
            serverButton.setVisibility(View.GONE);
            subtitleButton.setVisibility(View.GONE);
            //seekBarLayout.setVisibility(View.GONE);
            fastForwardButton.setVisibility(View.GONE);
            liveTvTextInController.setVisibility(View.VISIBLE);
            posterImageView.setVisibility(View.GONE);
            posterImageViewForTV.setVisibility(VISIBLE);
            seekBarLayout.setVisibility(View.GONE);
        }

        if (category.equalsIgnoreCase("tvseries")) {
            serverButton.setVisibility(View.GONE);
            //hide subtitle button if there is no subtitle
            if (video != null) {
                if (video.getSubtitle().isEmpty()) {
                    subtitleButton.setVisibility(View.GONE);
                }
            } else {
                subtitleButton.setVisibility(View.GONE);
            }
        }

        if (category.equalsIgnoreCase("movie")) {
            if (model.getVideoList() != null)
                videos.clear();
            videos = model.getVideoList();
            //hide subtitle button if there is no subtitle
            if (video != null) {
                if (video.getSubtitle().isEmpty()) {
                    subtitleButton.setVisibility(View.GONE);
                }
            } else {
                subtitleButton.setVisibility(View.GONE);
            }
            if (videos != null) {
                if (videos.size() < 1)
                    serverButton.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag:");

        subtitleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open subtitle dialog
                openSubtitleDialog();
            }
        });

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open server dialog
                openServerDialog(videos);
            }
        });


        //set title, description and poster in controller layout
        movieTitleTV.setText(model.getTitle());
        movieDescriptionTV.setText(model.getDescription());
        if (category.equalsIgnoreCase("tv")) {
            Picasso.get()
                    .load(model.getCardImageUrl())
                    .placeholder(R.drawable.poster_placeholder)
                    .centerCrop()
                    .resize(200, 120)
                    .error(R.drawable.poster_placeholder)
                    .into(posterImageViewForTV);
        }else {
            Picasso.get()
                    .load(model.getCardImageUrl())
                    .placeholder(R.drawable.poster_placeholder)
                    .centerCrop()
                    .resize(120, 200)
                    .error(R.drawable.poster_placeholder)
                    .into(posterImageView);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        Log.e("RemoteKey", "DPAD_HOME");
        /** Use pressed home button **/
        //time to set media session active
        super.onUserLeaveHint();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_MOVE_HOME:

                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.e("RemoteKey", "DPAD_DOWN");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.e("RemoteKey", "DPAD_RIGHT");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.e("RemoteKey", "DPAD_LEFT");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.e("RemoteKey", "DPAD_CENTER");
                if (!exoPlayerView.isControllerVisible()) {
                    exoPlayerView.showController();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                Log.e("RemoteKey", "DPAD_BACK");
                if (exoPlayerView.isControllerVisible()) {
                    exoPlayerView.hideController();
                    finish();
                }else{
                    exoPlayerView.showController();
                }
//                else {
//                    if (doubleBackToExitPressedOnce) {
//                        releasePlayer();
//                        //mediaSessionHelper.stopMediaSession();
//                        finish();
//                    } else {
//                        handleBackPress();
//                    }
//                }

                break;
            case KeyEvent.KEYCODE_ESCAPE:
                Log.e("RemoteKey", "DPAD_ESCAPE");
               /* if (!exoPlayerView.isControllerVisible()){
                    exoPlayerView.showController();
                }else {
                    releasePlayer();
                    finish();
                }*/
                break;
        }
        return false;
    }


    private void handleBackPress() {
        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();
        new ToastMsg(PlayerActivity.this).toastIconSuccess("Please click BACK again to exit.");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    private void openServerDialog(List<Video> videos) {
        if (videos != null) {
            List<Video> videoList = new ArrayList<>();
            videoList.clear();

            for (Video video : videos) {
                if (!video.getFileType().equalsIgnoreCase("embed")) {
                    videoList.add(video);
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            View view = LayoutInflater.from(PlayerActivity.this).inflate(R.layout.layout_server_tv, null);
            RecyclerView serverRv = view.findViewById(R.id.serverRv);
            ServerAdapter serverAdapter = new ServerAdapter(PlayerActivity.this, videoList, "movie");
            serverRv.setLayoutManager(new LinearLayoutManager(PlayerActivity.this));
            serverRv.setHasFixedSize(true);
            serverRv.setAdapter(serverAdapter);

            Button closeBt = view.findViewById(R.id.close_bt);

            builder.setView(view);

            final AlertDialog dialog = builder.create();
            dialog.show();

            closeBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
            serverAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, Video obj, int position, ServerAdapter.OriginalViewHolder holder) {
                    Intent playerIntent = new Intent(PlayerActivity.this, PlayerActivity.class);
                    PlaybackModel video = new PlaybackModel();
                    video.setId(model.getId());
                    video.setTitle(model.getTitle());
                    video.setDescription(model.getDescription());
                    video.setCategory("movie");
                    video.setVideo(obj);
                    video.setVideoList(model.getVideoList());
                    video.setVideoUrl(obj.getFileUrl());
                    video.setVideoType(obj.getFileType());
                    video.setBgImageUrl(model.getBgImageUrl());
                    video.setCardImageUrl(model.getCardImageUrl());
                    video.setIsPaid(model.getIsPaid());

                    playerIntent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, video);

                    startActivity(playerIntent);
                    dialog.dismiss();
                    finish();
                }
            });
        } else {
            new ToastMsg(this).toastIconError(getString(R.string.no_other_server_found));
        }
    }

    private void openSubtitleDialog() {
        if (video != null) {
            if (!video.getSubtitle().isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                View view = LayoutInflater.from(PlayerActivity.this).inflate(R.layout.layout_subtitle_dialog, null);
                RecyclerView serverRv = view.findViewById(R.id.serverRv);
                SubtitleListAdapter adapter = new SubtitleListAdapter(PlayerActivity.this, video.getSubtitle());
                serverRv.setLayoutManager(new LinearLayoutManager(PlayerActivity.this));
                serverRv.setHasFixedSize(true);
                serverRv.setAdapter(adapter);

                Button closeBt = view.findViewById(R.id.close_bt);

                builder.setView(view);
                final AlertDialog dialog = builder.create();
                dialog.show();

                closeBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                //click event
                adapter.setListener(new SubtitleListAdapter.OnSubtitleItemClickListener() {
                    @Override
                    public void onSubtitleItemClick(View view, Subtitle subtitle, int position, SubtitleListAdapter.SubtitleViewHolder holder) {
                        setSelectedSubtitle(mediaSource, subtitle.getUrl(),getApplicationContext());
                        dialog.dismiss();
                    }
                });

            } else {
                new ToastMsg(this).toastIconError(getResources().getString(R.string.no_subtitle_found));
            }
        } else {
            new ToastMsg(this).toastIconError(getResources().getString(R.string.no_subtitle_found));
        }
    }

    public void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            Uri subtitleUri = Uri.parse(subtitle);

            // Create a subtitle format
            Format subtitleFormat = new Format.Builder()
                    .setSampleMimeType(MimeTypes.TEXT_VTT) // MIME type for WebVTT
                    .setLanguage("en")                     // Language code for English
                    .build(); // The subtitle language. May be null.

            // Create a DefaultDataSource.Factory with a DefaultBandwidthMeter
            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                    context
            );

            // Create the subtitle MediaSource using the SingleSampleMediaSource.Factory
            // Create the subtitle MediaSource using the SingleSampleMediaSource.Factory
            MediaItem subtitleMediaItem = MediaItem.fromUri(subtitleUri);

            MediaItem.SubtitleConfiguration subtitleConfiguration = new MediaItem.SubtitleConfiguration.Builder(subtitleUri).build();
            MediaSource subtitleSource = new SingleSampleMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(subtitleConfiguration, C.TIME_UNSET);


            // Merge the video MediaSource and the subtitle MediaSource
            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);

            // Prepare the player with the merged media source
            ExoPlayer player = new ExoPlayer.Builder(context).build();
            player.setMediaSource(mergedSource);
            player.prepare();
            player.setPlayWhenReady(true);
            // Optionally call resumePlayer() here if you have such a method
        } else {
            Toast.makeText(context, "There is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }
    public void initVideoPlayer(String url, String type) {
        if (player != null) {
            player.release();
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create a DefaultBandwidthMeter (optional)
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext())
                .setInitialBitrateEstimate(1,1)
                .build();

        // Create a DefaultTrackSelector
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(getApplicationContext());

        // Initialize the SimpleExoPlayer instance using ExoPlayer.Builder
        player = new SimpleExoPlayer.Builder(getApplicationContext())
                .setTrackSelector(trackSelector)
                .build();

        exoPlayerView.setPlayer(player);

        // Resize mode to fit screen
        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        exoPlayerView.setControllerShowTimeoutMs(5000);
        player.setPlayWhenReady(true);

        Uri uri = Uri.parse(url);

        // Prepare the media source based on the type
        switch (type) {
            case "hls":
                mediaSource = hlsMediaSource(uri, PlayerActivity.this);
                break;
            case "rtmp":
                mediaSource = rtmpMediaSource(uri);
                break;
            default:
                mediaSource = mediaSource(uri, PlayerActivity.this);
                break;
        }

        // Prepare the player if it's not YouTube content
        if (!type.contains("youtube")) {
            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(true);
        }

        seekToStartPosition();

        // Add player event listeners
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY && player.getPlayWhenReady()) {
                    isPlaying = true;
                    progressBar.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_BUFFERING) {
                    isPlaying = false;
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    // Handle when playback ends
                } else {
                    isPlaying = false;
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Set visibility listener for player controls
        exoPlayerView.setControllerVisibilityListener(visibility -> visible = visibility);
//        if (mediaSource == null) {
//            // Handle the error or initialize properly
//            Log.e("PlayerActivity", "MediaSource is null");
//            return;
//        }
//        // Initialize the player with the mediaSource
//        player.setMediaSource(mediaSource);
//        player.prepare();
//        player.play();
    }


    private void seekToStartPosition() {
        // Skip ahead if given a starting position.
        if (mStartingPosition > -1L) {
            if (player.getPlayWhenReady()) {
                Log.d("VideoFragment", "Is prepped, seeking to " + mStartingPosition);
                player.seekTo(mStartingPosition);
            }
        }
    }


    private MediaSource mp3MediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext());
        ProgressiveMediaSource.Factory extractorFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);
        return extractorFactory.createMediaSource(MediaItem.fromUri(uri));
    }


    private MediaSource mediaSource(Uri uri, PlayerActivity playerActivity) {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext());
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri));
    }


    private MediaSource rtmpMediaSource(Uri uri) {
        RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri));
    }


   /* @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {

        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;
                    String dashUrl = ytFiles.get(itag).getUrl();

                    try {
                        com.google.android.exoplayer2.source.MediaSource source = mediaSource(Uri.parse(dashUrl), context);
                        player.prepare(source, true, false);
                        //player.setPlayWhenReady(false);
                        exoPlayerView.setPlayer(player);
                        player.setPlayWhenReady(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.extract(url, true, true);
    }*/

    private com.google.android.exoplayer2.source.MediaSource hlsMediaSource(Uri uri, Context context) {

        DefaultBandwidthMeter bandwidthMeter =  new DefaultBandwidthMeter.Builder(context).setInitialBitrateEstimate(1,1).build();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "oxoo"), bandwidthMeter);
        com.google.android.exoplayer2.source.MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));

        return videoSource;
    }

    @Override
    public void onBackPressed() {
        if (visible == View.GONE) {
            exoPlayerView.showController();
        } else {
            releasePlayer();
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (wakeLock != null)
            wakeLock.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
    }

    private void releasePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
            player = null;
            exoPlayerView.setPlayer(null);
        }
    }
}