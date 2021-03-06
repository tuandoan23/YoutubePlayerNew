package com.example.youtube.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.example.youtube.R;
import com.example.youtube.activity.ContentVideoActivity;
import com.example.youtube.database.DBHelper;
import com.example.youtube.event.MessageEvent;
import com.example.youtube.model.listvideomodel.Item;
import com.example.youtube.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<Item> arrayListVideo;
    private Context context;
    private Activity activity;
//    private Fragment fragment;
    private DBHelper db;
    private boolean isYoutubeInstalled;

    public VideoAdapter(ArrayList<Item> items, Context context, Activity activity) {
        arrayListVideo = items;
        this.context = context;
        this.activity = activity;
//        this.fragment = fragment;
//        if (fragment != null) {
//            isYoutubeInstalled = Utils.isAppInstalled(Utils.packageName, fragment.getActivity().getPackageManager());
//        } else {
            isYoutubeInstalled = Utils.isAppInstalled(Utils.packageName, activity.getPackageManager());
//        }
        db = new DBHelper(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.tvTitleVideo)
        AppCompatTextView tvTitleVideo;

        @BindView(R.id.tvDescription)
        AppCompatTextView tvDescription;

        @BindView(R.id.imgVideo)
        SimpleDraweeView imgVideo;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View listVideoView = inflater.inflate(R.layout.video, parent, false);
        return new ViewHolder(listVideoView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Item video = arrayListVideo.get(position);
        Uri uri = Uri.parse(video.getSnippet().getThumbnails().getHigh().getUrl().toString());
        viewHolder.imgVideo.setImageURI(uri);
        viewHolder.tvTitleVideo.setText(video.getSnippet().getTitle());
        viewHolder.tvDescription.setText(video.getSnippet().getDescription().toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = arrayListVideo.get(position);
                db.insertRecently(item);
                String videoId = video.getId().getVideoId();
                String videoTitle = video.getSnippet().getTitle();
                Intent newIntent = new Intent(context, ContentVideoActivity.class );
                newIntent.putExtra("videoId", videoId);
                newIntent.putExtra("videoTitle", videoTitle);
                newIntent.putExtra("video", item);
                if (isYoutubeInstalled) {
                    context.startActivity(newIntent);
                    EventBus.getDefault().post(new MessageEvent(true));
                } else {
                    installSuggestDialog();
                }
            }
        });
    }

    private void installSuggestDialog(){
        new MaterialDialog.Builder(context)
                .title(R.string.title_dialog)
                .content(R.string.content)
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        openPlayStore();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private void openPlayStore(){
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Utils.packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Utils.packageName)));
        }
    }

    @Override
    public int getItemCount() {
        return arrayListVideo.size();
    }
}
