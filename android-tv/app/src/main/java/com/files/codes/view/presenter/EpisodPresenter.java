package com.files.codes.view.presenter;

import static android.os.Build.VERSION_CODES.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import com.files.codes.database.DatabaseHelper;
import com.files.codes.model.PlaybackModel;
import com.files.codes.model.Video;
import com.files.codes.model.movieDetails.Episode;
import com.files.codes.utils.LoginAlertDialog;
import com.files.codes.utils.PaidDialog;
import com.files.codes.utils.PreferenceUtils;
import com.files.codes.utils.ToastMsg;
import com.files.codes.view.PlayerActivity;
import com.files.codes.view.VideoPlaybackActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class EpisodPresenter extends Presenter {
    private static int CARD_WIDTH = 330;
    private static int CARD_HEIGHT = 180;
    private static Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d("onCreateViewHolder", "creating viewholder");
        mContext = parent.getContext();
        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        cardView.requestLayout();
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final Episode video = (Episode) item;
        ((ViewHolder) viewHolder).mCardView.setTitleText(video.getEpisodesName());
        ((ViewHolder) viewHolder).mCardView.findViewById(R).setVisibility(View.GONE);
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        ((ViewHolder) viewHolder).updateCardViewImage(video.getImageUrl());
        final DatabaseHelper db = new DatabaseHelper(mContext);

        ((ViewHolder) viewHolder).mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (video.getIsPaid().equals("1")) {
                    if (PreferenceUtils.isLoggedIn(mContext)) {
                        String status= db.getActiveStatusData() != null ? db.getActiveStatusData().getStatus() : "inactive";
                        if (  status.equalsIgnoreCase("active")) {

                            if (video.getFileType().equalsIgnoreCase("embed")) {
                                new ToastMsg(mContext).toastIconError(mContext.getResources().getString(R));
                                return;
                            }
                            PlaybackModel model = new PlaybackModel();
                            model.setId(Long.parseLong(video.getEpisodesId()));
                            model.setTitle(video.getTvSeriesTitle());
                            model.setDescription("Seasson: " + video.getSeasonName() + "; Episode: " + video.getEpisodesName());
                            model.setVideoType(video.getFileType());
                            model.setCategory("tvseries");
                            model.setVideoUrl(video.getFileUrl());
                            Video videoModel = new Video();
                            videoModel.setSubtitle(video.getSubtitle());
                            model.setVideo(videoModel);
                            model.setCardImageUrl(video.getCardBackgroundUrl());
                            model.setBgImageUrl(video.getImageUrl());
                            model.setIsPaid(video.getIsPaid());

                            Intent intent = new Intent(mContext, PlayerActivity.class);
                            intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, model);
                            mContext.startActivity(intent);

                        } else {
                            //saved data is not valid, because it was saved more than 2 hours ago
                            PreferenceUtils.updateSubscriptionStatus(mContext);
                            //subscription is not active
                            //new PaidDialog(getActivity()).showPaidContentAlertDialog();
                            PaidDialog dialog = new PaidDialog(mContext);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                            dialog.show();
                        }
                    } else {
                        // user is not logged in
                        // show an alert dialog
                        LoginAlertDialog dialog = new LoginAlertDialog(mContext);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                        dialog.show();
                    }
                } else {
                    if (video.getFileType().equalsIgnoreCase("embed")) {
                        new ToastMsg(mContext).toastIconError(mContext.getResources().getString(R));
                        return;
                    }
                    PlaybackModel model = new PlaybackModel();
                    model.setId(Long.parseLong(video.getEpisodesId()));
                    model.setTitle(video.getTvSeriesTitle());
                    model.setDescription("Seasson: " + video.getSeasonName() + "; Episode: " + video.getEpisodesName());
                    model.setVideoType(video.getFileType());
                    model.setCategory("tvseries");
                    model.setVideoUrl(video.getFileUrl());
                    Video videoModel = new Video();
                    videoModel.setSubtitle(video.getSubtitle());
                    model.setVideo(videoModel);
                    model.setCardImageUrl(video.getCardBackgroundUrl());
                    model.setBgImageUrl(video.getImageUrl());
                    model.setIsPaid(video.getIsPaid());

                    Intent intent = new Intent(mContext, PlayerActivity.class);
                    intent.putExtra(VideoPlaybackActivity.EXTRA_VIDEO, model);
                    mContext.startActivity(intent);
                }
            }
        });

    }


    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }


    class ViewHolder extends Presenter.ViewHolder {

        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext
                    .getResources()
                    .getDrawable(R);
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(String url) {

            Picasso.get()
                    .load(url)
                    .resize(CARD_WIDTH * 2, CARD_HEIGHT * 2)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);
        }
    }


    static class PicassoImageCardViewTarget implements Target {


        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView mImageCardView) {
            this.mImageCardView = mImageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            mImageCardView.setMainImage(errorDrawable);
        }


        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}


