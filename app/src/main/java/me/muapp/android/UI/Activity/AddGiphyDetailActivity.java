package me.muapp.android.UI.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import me.muapp.android.Classes.Giphy.Data.GiphyEntry;
import me.muapp.android.Classes.Internal.UserContent;
import me.muapp.android.R;

public class AddGiphyDetailActivity extends BaseActivity {
    public static final String CURRENT_GIPHY = "CURRENT_GIPHY";
    public static final int GIPHY_CODE = 44;
    EditText et_giphy_comment;
    ImageView img_giphy_detail;
    StorageReference mainReference;
    GiphyEntry currentGiphy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_giphy_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        img_giphy_detail = (ImageView) findViewById(R.id.img_giphy_detail);
        et_giphy_comment = (EditText) findViewById(R.id.et_giphy_comment);
        if ((currentGiphy = getIntent().getParcelableExtra(CURRENT_GIPHY)) != null) {
            Log.wtf("Loading", currentGiphy.getImages().getFixed().getUrl());
            Glide.with(this).load(currentGiphy.getImages().getOriginal().getUrl()).asGif().priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.SOURCE).placeholder(R.drawable.ic_logo_muapp_no_caption).fitCenter().into(img_giphy_detail);
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
                publishThisMedia();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publishThisMedia() {
        final UserContent thisContent = new UserContent();
        thisContent.setComment(et_giphy_comment.getText().toString());
        thisContent.setCreatedAt(new Date().getTime());
        thisContent.setLikes(0);
        thisContent.setContentUrl(currentGiphy.getImages().getOriginal().getUrl());
        thisContent.setCatContent("contentGif");
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
}
