package me.muapp.android.UI.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import me.muapp.android.Classes.Util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import me.muapp.android.Classes.Internal.UserMedia;
import me.muapp.android.R;
import me.muapp.android.UI.Fragment.FacebookPhotosFragment;
import me.muapp.android.UI.Fragment.GalleryPhotosFragment;
import me.muapp.android.UI.Fragment.InstagramPhotosFragment;
import me.muapp.android.UI.Fragment.Interface.OnImageSelectedListener;

import static me.muapp.android.UI.Activity.AddPhotosDetailActivity.CURRENT_MEDIA;
import static me.muapp.android.UI.Activity.AddPhotosDetailActivity.PHOTOS_REQUEST;

public class AddPhotosActivity extends BaseActivity implements OnImageSelectedListener {
    public static Boolean hasSelectedMedia = false;
    TabLayout tabLayout;
    int[] activeIcons = new int[]{R.drawable.ic_tab_fb, R.drawable.ic_tab_gal, R.drawable.ic_tab_inst};
    int[] inactiveIcons = new int[]{R.drawable.ic_tab_fb_inactive, R.drawable.ic_tab_gal_inactive, R.drawable.ic_tab_inst_inactive};
    ImageView img_photo_preview;
    VideoView vv_video_preview;
    UserMedia currentMedia;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    Fragment[] fragments;
    ImageView lastSelectedContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragments = new Fragment[]{FacebookPhotosFragment.newInstance(loggedUser), GalleryPhotosFragment.newInstance(loggedUser), InstagramPhotosFragment.newInstance(loggedUser)};
        Log.wtf("MyUserIs", loggedUser.getId() + "");
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        img_photo_preview = (ImageView) findViewById(R.id.img_photo_preview);
        vv_video_preview = (VideoView) findViewById(R.id.vv_video_preview);
        Glide.with(this)
                .load(R.mipmap.bg_login)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(300, 300) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        Log.wtf("onResourceReady", bitmap.getByteCount() + "");
                        img_photo_preview.setImageBitmap(bitmap);
                    }
                });
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setupTabIcons(position);
                setFirstPhoto(fragments[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setupTabIcons(0);
    }


    private void setFirstPhoto(final Fragment fragment) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fragment instanceof FacebookPhotosFragment) {
                    ((FacebookPhotosFragment) fragment).fistImageLoad();
                } else if (fragment instanceof GalleryPhotosFragment) {
                    ((GalleryPhotosFragment) fragment).fistImageLoad();
                } else if (fragment instanceof InstagramPhotosFragment) {
                    ((InstagramPhotosFragment) fragment).fistImageLoad();
                }
            }
        }, 100);

    }

    private void setupTabIcons(int pos) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(inactiveIcons[i]);
        }

        tabLayout.getTabAt(pos).setIcon(activeIcons[pos]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_photos, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (vv_video_preview.isPlaying()) {
            vv_video_preview.stopPlayback();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_continue:
                Log.wtf("CurrentMedia", currentMedia.toString());
                Intent photoIntent = new Intent(this, AddPhotosDetailActivity.class);
                photoIntent.putExtra(CURRENT_MEDIA, currentMedia);
                startActivityForResult(photoIntent, PHOTOS_REQUEST);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchViews(int mediaType) {
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            if (img_photo_preview.getVisibility() != View.VISIBLE)
                img_photo_preview.setVisibility(View.VISIBLE);
            if (vv_video_preview.getVisibility() != View.GONE)
                vv_video_preview.setVisibility(View.GONE);
        } else {
            if (vv_video_preview.getVisibility() != View.VISIBLE)
                vv_video_preview.setVisibility(View.VISIBLE);
            if (img_photo_preview.getVisibility() != View.GONE)
                img_photo_preview.setVisibility(View.GONE);
        }

    }

    @Override
    public void onImageSelected(String url, Uri uri, int mediaType, ImageView container) {
        if (lastSelectedContainer != null)
            lastSelectedContainer.clearColorFilter();
        lastSelectedContainer = container;
        //Alpha 0.6 - (255 * 0.6)
        container.setColorFilter(Color.argb(153, 255, 255, 255));
        switchViews(mediaType);
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            if (url != null) {
                Log.wtf("onImageSelected", url.toString());
                Glide.with(this).load(url).centerCrop().error(R.drawable.ic_placeholder_error).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(img_photo_preview);
            } else if (uri != null) {
                Log.wtf("onImageSelected", uri.toString());
                Glide.with(this).load(url).centerCrop().error(R.drawable.ic_placeholder_error).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(img_photo_preview);
            }
        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            Log.wtf("onImageSelected", url.toString());
            vv_video_preview.setVideoPath(url);
            vv_video_preview.setMediaController(new MediaController(this));
            vv_video_preview.requestFocus();
            vv_video_preview.start();
        }
        currentMedia = new UserMedia();
        currentMedia.setMediaType(mediaType);
        currentMedia.setUri(uri);
        currentMedia.setPath(url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTOS_REQUEST:
                    finish();
                    break;
            }
        }
    }

    class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }
}
