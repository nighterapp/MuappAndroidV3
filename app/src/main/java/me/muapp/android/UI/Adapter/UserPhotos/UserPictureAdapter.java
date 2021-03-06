package me.muapp.android.UI.Adapter.UserPhotos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import me.muapp.android.Classes.API.APIService;
import me.muapp.android.Classes.API.Handlers.UserInfoHandler;
import me.muapp.android.Classes.API.Params.AlbumParam;
import me.muapp.android.Classes.Internal.User;
import me.muapp.android.Classes.Util.Log;
import me.muapp.android.R;
import me.muapp.android.UI.Activity.FacebookAlbumsActivity;
import me.muapp.android.UI.Activity.ProfileSettingsActivity;
import me.muapp.android.UI.Fragment.Interface.OnProfileImageSelectedListener;

import static android.content.Context.VIBRATOR_SERVICE;
import static me.muapp.android.UI.Activity.ProfileSettingsActivity.ADAPTER_POSITION;
import static me.muapp.android.UI.Activity.ProfileSettingsActivity.REQUEST_FACEBOOK_ALBUMS;

/**
 * Created by rulo on 27/03/17.
 */

public class UserPictureAdapter extends RecyclerView.Adapter<UserPictureAdapter.UserPhotoViewHolder> implements ItemTouchHelperAdapter {
    private final LayoutInflater mInflater;
    public static List<String> picturesData;
    private ProfileSettingsActivity mContext;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_PHOTO = 2;
    private static final int ACTION_GET_PHOTO_FB = 3;
    private static final int ACTION_DELETE_PHOTO = 99;
    private static final int ACTION_CANCEL = 0;
    private OnProfileImageSelectedListener onProfileImageSelectedListener;

    public UserPictureAdapter(ProfileSettingsActivity context, List<String> picturesData) {
        mInflater = LayoutInflater.from(context);
        if (picturesData != null) {
            this.picturesData = picturesData;
        } else
            this.picturesData = new ArrayList<>();
        mContext = context;
    }


    public void setOnProfileImageSelectedListener(OnProfileImageSelectedListener onProfileImageSelectedListener) {
        this.onProfileImageSelectedListener = onProfileImageSelectedListener;
    }

    @Override
    public UserPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.user_picture_item_layout, parent, false);
        return new UserPhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserPhotoViewHolder holder, int position) {
        holder.bind(picturesData.get(position));
    }


    @Override
    public int getItemCount() {
        return picturesData.size();
    }


    public void setUserPictures(final List<String> picturesData) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UserPictureAdapter.this.picturesData = picturesData;
                notifyDataSetChanged();
            }
        });
    }

    public void swap(int firstPosition, int secondPosition) {
        Collections.swap(picturesData, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    public String removeItem(int position) {
        final String removed = picturesData.remove(position);
        notifyItemRemoved(position);
        return removed;
    }


    public void addItem(int position, String picture) {
        picturesData.add(position, picture);
        notifyItemInserted(position);
    }

    public void addItem(String picture) {
        picturesData.add(picture);
        notifyDataSetChanged();
    }

    public void removeAllItems() {
        picturesData = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        final String picture = picturesData.remove(fromPosition);
        picturesData.add(toPosition, picture);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(picturesData, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    public void validateMovedElements() {
        for (String s : picturesData) {
            Log.wtf("Original", s != null ? s.toString() :"empty");
        }



    /*    for (int i = 0; i < picturesData.size(); i++) {
            Log.wtf("Original", picturesData.get(i).toString() + " " + (i + 1));
        }*/
        //notifyDataSetChanged();
    /*    for (String s : picturesData) {
            Log.wtf("Original", s.toString());
        }

     *//*   Collections.sort(picturesData, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (TextUtils.isEmpty(o2)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
*//*
        for (String s : picturesData) {
            Log.wtf("Original ordered", s.toString());
        }
        notifyDataSetChanged();*/

       /* for (int i = 0; i < picturesData.size() - 1; i++) {

        }
*/

        List<AlbumParam> prms = new ArrayList<>();
        for (String image : picturesData) {
            if (image != null && !TextUtils.isEmpty(image)) {
                AlbumParam albumParam = new AlbumParam();
                albumParam.setUrl(image);
                prms.add(albumParam);
            }
        }

        new APIService(mContext).uploadPhotos(prms, new UserInfoHandler() {
            @Override
            public void onSuccess(int responseCode, String userResponse) {

            }

            @Override
            public void onSuccess(int responseCode, User user) {

            }

            @Override
            public void onFailure(boolean isSuccessful, String responseString) {

            }
        });
    }

    @Override
    public void onItemDismiss(int position) {

    }

    public class UserPhotoViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder, View.OnClickListener {
        ImageView img_photo_item;
        ImageButton btn_photo_more;
        Boolean hasData;
        LinkedHashMap<String, Integer> optionsMap;

        public UserPhotoViewHolder(View itemView) {
            super(itemView);
            img_photo_item = (ImageView) itemView.findViewById(R.id.img_photo_item);
            btn_photo_more = (ImageButton) itemView.findViewById(R.id.btn_photo_more);
            optionsMap = new LinkedHashMap<>();
        }

        public void bind(final String photoUrl) {
            hasData = !TextUtils.isEmpty(photoUrl);
            Glide.with(mContext).load(photoUrl).placeholder(hasData ? R.drawable.ic_placeholder : R.drawable.background_gray_light).centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(img_photo_item);
            optionsMap.put(mContext.getString(R.string.action_take_photo), ACTION_TAKE_PHOTO);
            optionsMap.put(mContext.getString(R.string.action_select_photo), ACTION_SELECT_PHOTO);
            optionsMap.put(mContext.getString(R.string.action_select_from_facebook), ACTION_GET_PHOTO_FB);
            if (hasData)
                optionsMap.put(mContext.getString(R.string.action_delete_photo), ACTION_DELETE_PHOTO);
            optionsMap.put(mContext.getString(R.string.action_cancel_photo), ACTION_CANCEL);
            btn_photo_more.setOnClickListener(this);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            ((Vibrator) mContext.getSystemService(VIBRATOR_SERVICE)).vibrate(50);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final String[] options = optionsMap.keySet().toArray(new String[0]);
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (optionsMap.get(options[which])) {
                        case ACTION_TAKE_PHOTO:
                            if (onProfileImageSelectedListener != null)
                                onProfileImageSelectedListener.onCameraSelected(img_photo_item, getAdapterPosition());
                            break;
                        case ACTION_SELECT_PHOTO:
                            if (onProfileImageSelectedListener != null)
                                onProfileImageSelectedListener.onGallerySelected(img_photo_item, getAdapterPosition());
                            break;
                        case ACTION_GET_PHOTO_FB:
                            Intent requestFB = new Intent(mContext, FacebookAlbumsActivity.class);
                            requestFB.putExtra(ADAPTER_POSITION, getAdapterPosition());
                            mContext.startActivityForResult(requestFB, REQUEST_FACEBOOK_ALBUMS);
                            break;
                        case ACTION_DELETE_PHOTO:
                            picturesData.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                            picturesData.add(5, "");
                            notifyItemInserted(5);
                            if (onProfileImageSelectedListener != null)
                                onProfileImageSelectedListener.onPictureDeleted(img_photo_item, getAdapterPosition());
                            break;
                        case ACTION_CANCEL:
                            break;
                    }
                }
            });
            builder.show();
        }
    }

    public static class UserPhotoContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public UserPhotoContextMenuInfo(int position, String url) {
            this.position = position;
            this.url = url;
        }

        final public int position;
        final public String url;
    }

}