package me.muapp.android.UI.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by rulo on 4/04/17.
 */

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> {
    @Override
    public MatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MatchesViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MatchesViewHolder extends RecyclerView.ViewHolder {
        public MatchesViewHolder(View itemView) {
            super(itemView);
        }
    }
   /* private final LayoutInflater mInflater;
    private SortedList<DialogCacheObject> dialogs;
    private Context mContext;


    public MatchesAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.dialogs = new SortedList<>(DialogCacheObject.class, new SortedList.Callback<DialogCacheObject>() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public int compare(DialogCacheObject o1, DialogCacheObject o2) {
                return new Date(o2.getLastMessageDateSent()).compareTo(new Date(o1.getLastMessageDateSent()));
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(DialogCacheObject oldItem, DialogCacheObject newItem) {
                return oldItem.toString().equals(newItem.toString());
            }

            @Override
            public boolean areItemsTheSame(DialogCacheObject item1, DialogCacheObject item2) {
                return item1.equals(item2);
            }
        });
        this.mContext = context;
    }

    public void addDialog(DialogCacheObject dco) {
        dialogs.add(dco);
    }

    @Override
    public MatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.matches_item_layout, parent, false);
        return new MatchesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MatchesViewHolder holder, int position) {
        holder.bind(dialogs.get(position));
    }

    @Override
    public int getItemCount() {
        return dialogs != null ? dialogs.size() : 0;
    }

    public class MatchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView matchImage;
        TextView matchLine1;
        TextView matchLine2;
        ImageView matchIndicator;
        RelativeLayout match_item_container;
        DialogCacheObject thisDialog;

        public MatchesViewHolder(View itemView) {
            super(itemView);
            this.match_item_container = (RelativeLayout) itemView.findViewById(R.id.match_item_container);
            this.matchImage = (ImageView) itemView.findViewById(R.id.match_user_image);
            this.matchLine1 = (TextView) itemView.findViewById(R.id.match_item_line_1);
            this.matchLine2 = (TextView) itemView.findViewById(R.id.match_item_line_2);
            this.matchIndicator = (ImageView) itemView.findViewById(R.id.match_notification);
        }

        public void bind(DialogCacheObject dialog) {
            thisDialog = dialog;
            match_item_container.setOnClickListener(this);
            Glide.with(mContext).load(dialog.getOpponentPhoto()).placeholder(R.drawable.ic_logo_muapp_no_caption).centerCrop().bitmapTransform(new CropCircleTransformation(mContext)).into(matchImage);
            matchLine1.setText(dialog.getOpponentName());
            if (dialog.getLastMessageUserId() == null ||
                    dialog.getDeletedAt() != null && dialog.getLastMessageDateSent() < dialog.getDeletedAt().getTime() / 1000) {
                matchLine2.setText(DateUtils.getMatchCrushTimeAgo(dialog.getCreatedAt().getTime(), mContext, false));
                matchLine2.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            } else { //with messages
                if (dialog.getLastMessage() != null && dialog.getLastMessage().length() > 0) {
                    if (dialog.getLastMessage().equalsIgnoreCase(QuickbloxHelper.POKE_IMAGE)) {
                        matchLine2.setText(mContext.getString(R.string.matches_last_message_photo));
                    } else if (dialog.getLastMessage().equalsIgnoreCase(QuickbloxHelper.POKE_STICKER)) {
                        matchLine2.setText(mContext.getString(R.string.matches_last_message_sticker));
                    } else if (dialog.getLastMessage().equalsIgnoreCase(QuickbloxHelper.POKE_VOICE)) {
                        matchLine2.setText(R.string.matches_last_message_voice_message);
                    } else {
                        matchLine2.setText(dialog.getLastMessage());
                    }
                } else {
                    matchLine2.setText("");
                }
                matchLine2.setTextColor(ContextCompat.getColor(mContext, R.color.color_muapp_dark));
            }
        }

        @Override
        public void onClick(View v) {
            Intent chatIntent = new Intent(mContext, ChatActivity.class);
            chatIntent.putExtra(DIALOG_EXTRA, thisDialog);
            mContext.startActivity(chatIntent);
        }
    }*/
}
