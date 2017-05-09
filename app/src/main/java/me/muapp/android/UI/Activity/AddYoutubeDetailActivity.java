package me.muapp.android.UI.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import me.muapp.android.Classes.Internal.UserContent;
import me.muapp.android.Classes.Youtube.Data.YoutubeVideo;
import me.muapp.android.R;

import static me.muapp.android.Classes.Youtube.Config.getYoutubeApiKey;

public class AddYoutubeDetailActivity extends BaseActivity implements YouTubePlayer.OnInitializedListener {
    public static final String CURRENT_VIDEO = "CURRENT_VIDEO";
    public static final int YOUTUBE_REQUEST_CODE = 511;
    YoutubeVideo currentVideo;
    EditText et_youtube_about;
    YouTubePlayerFragment youtube_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_add_youtube_detail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            et_youtube_about = (EditText) findViewById(R.id.et_youtube_about);
            youtube_fragment = (YouTubePlayerFragment) getFragmentManager()
                    .findFragmentById(R.id.youtube_fragment);
            youtube_fragment.initialize(getYoutubeApiKey(), this);
            currentVideo = getIntent().getParcelableExtra(CURRENT_VIDEO);
        } catch (Exception x) {
            Log.wtf("currentVideo", x.getMessage());
            x.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.publish_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_publish:
                publishThisVIdeo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AddYoutubeDetailActivity() {
    }

    private void publishThisVIdeo() {
        UserContent thisContent = new UserContent();
        thisContent.setComment(et_youtube_about.getText().toString());
        thisContent.setCreatedAt(new Date().getTime());
        thisContent.setLikes(0);
        thisContent.setCatContent("contentYtv");
        thisContent.setThumbUrl(currentVideo.getSnippet().getThumbnails().getHigh().getUrl());
        thisContent.setVideoId(currentVideo.getId().getVideoId());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("content").child(String.valueOf(loggedUser.getId()));
        String key = ref.push().getKey();
        ref.child(key).setValue(thisContent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        YouTubePlayer.PlayerStyle style = YouTubePlayer.PlayerStyle.MINIMAL;
        youTubePlayer.setPlayerStyle(style);
        if (!wasRestored) {
            youTubePlayer.cueVideo(currentVideo.getId().getVideoId());
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}