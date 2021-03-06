package me.muapp.android.UI.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.muapp.android.Classes.Chat.ConversationItem;
import me.muapp.android.R;
import me.muapp.android.UI.Activity.ChatActivity;

import static me.muapp.android.UI.Activity.ChatActivity.CONVERSATION_EXTRA;


/**
 * Created by rulo on 4/04/17.
 */

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder> implements Filterable {
    private List<ConversationItem> conversationItemList;
    private SortedList<ConversationItem> itemSortedList;
    private final LayoutInflater mInflater;
    private Context mContext;
    HashMap<String, String> attachmentMap;
    private ConversationFilter conversationFilter;

    public MatchesAdapter(final Context context) {
        this.conversationItemList = new ArrayList<>();
        this.attachmentMap = new HashMap<String, String>() {{
            put("contentAud", context.getString(R.string.lbl_add_voice_note));
            put("contentGif", context.getString(R.string.lbl_add_giphy));
            put("contentPic", context.getString(R.string.lbl_add_gallery));
            put("contentSpt", context.getString(R.string.lbl_add_music));
            put("contentStkr", context.getString(R.string.lbl_add_sticker));
            put("contentYtv", context.getString(R.string.lbl_add_video));
        }};
        this.itemSortedList = new SortedList<>(ConversationItem.class, new SortedList.Callback<ConversationItem>() {
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
            public int compare(ConversationItem o1, ConversationItem o2) {
                Long date1 = o1.getConversation().getLastMessage() != null ? o1.getConversation().getLastMessage().getTimeStamp() : o1.getConversation().getCreationDate();
                Long date2 = o2.getConversation().getLastMessage() != null ? o2.getConversation().getLastMessage().getTimeStamp() : o2.getConversation().getCreationDate();
                return new Date(date1).compareTo(new Date(date2));
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(ConversationItem oldItem, ConversationItem newItem) {
                return oldItem.toString().equals(newItem.toString());
            }

            @Override
            public boolean areItemsTheSame(ConversationItem item1, ConversationItem item2) {
                return item1.getConversation().getKey().equals(item2.getConversation().getKey());
            }
        });
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void updateConversationUser(String key, String newUrl) {
        for (ConversationItem item : conversationItemList) {
            if (item.getKey().equals(key) && !item.getProfilePicture().equals(newUrl)) {
                item.setProfilePicture(newUrl);
                break;
            }
        }
        for (int i = 0; i < itemSortedList.size(); i++) {
            if (itemSortedList.get(i).getKey().equals(key) && !itemSortedList.get(i).getProfilePicture().equals(newUrl)) {
                itemSortedList.get(i).setProfilePicture(newUrl);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void clearConversations() {
        conversationItemList.clear();
        itemSortedList.clear();
    }

    public void addConversation(ConversationItem c) {
        conversationItemList.add(c);
        itemSortedList.add(c);
    }

    public void removeConversation(String conversationKey) {
        for (ConversationItem item : conversationItemList) {
            if (item.getKey().equals(conversationKey)) {
                conversationItemList.remove(item);
                break;
            }
        }
        for (int i = 0; i < itemSortedList.size(); i++) {
            if (itemSortedList.get(i).getKey().equals(conversationKey)) {
                itemSortedList.removeItemAt(i);
                break;
            }
        }
    }

    @Override
    public MatchesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.matches_item_layout, parent, false);
        return new MatchesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MatchesViewHolder holder, int position) {
        holder.bind(itemSortedList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemSortedList.size();
    }

    public class MatchesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView matchImage;
        TextView matchLine1;
        TextView matchLine2;
        ImageView matchIndicator;
        RelativeLayout match_item_container;
        ConversationItem thisConversation;
        RelativeTimeTextView match_item_time;

        public MatchesViewHolder(View itemView) {
            super(itemView);
            this.match_item_container = (RelativeLayout) itemView.findViewById(R.id.match_item_container);
            this.matchImage = (ImageView) itemView.findViewById(R.id.match_user_image);
            this.matchLine1 = (TextView) itemView.findViewById(R.id.match_item_line_1);
            this.matchLine2 = (TextView) itemView.findViewById(R.id.match_item_line_2);
            this.matchIndicator = (ImageView) itemView.findViewById(R.id.match_notification);
            this.match_item_time = (RelativeTimeTextView) itemView.findViewById(R.id.match_item_time);
        }

        public void bind(ConversationItem conversation) {
            thisConversation = conversation;
            match_item_container.setOnClickListener(this);
            matchLine2.setTextColor(ContextCompat.getColor(mContext, R.color.color_muapp_dark));
            if (conversation.getConversation().getSeen() != null) {
                matchIndicator.setVisibility(conversation.getConversation().getSeen() ? View.GONE : View.VISIBLE);
            } else {
                matchIndicator.setVisibility(View.VISIBLE);
            }
            Glide.with(mContext).load(conversation.getProfilePicture()).error(R.drawable.ic_placeholder_error).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_placeholder).bitmapTransform(new CropCircleTransformation(mContext)).into(matchImage);
            matchLine1.setText(conversation.getFullName());
            long agoTime = conversation.getConversation().getLastMessage() != null ? conversation.getConversation().getLastMessage().getTimeStamp() : conversation.getConversation().getCreationDate();
            match_item_time.setReferenceTime(agoTime > new Date().getTime() ? new Date().getTime() : agoTime);
            if (conversation.getConversation().getLastMessage() != null) {
                if (conversation.getConversation().getLastMessage().getAttachment() != null) {
                    String attachmentKey = conversation.getConversation().getLastMessage().getAttachment().getCatContent();
                    if (attachmentMap.containsKey(attachmentKey)) {
                        matchLine2.setText(attachmentMap.get(attachmentKey));
                    }
                } else {
                    matchLine2.setText(conversation.getConversation().getLastMessage().getContent());
                }
            } else {
                if (conversation.getConversation().getCreationDate() > new Date().getTime())
                    conversation.getConversation().setCreationDate(new Date().getTime());
                matchLine2.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                matchLine2.setText("Match " + DateUtils.getRelativeTimeSpanString(conversation.getConversation().getCreationDate(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));
            }
        }

        @Override
        public void onClick(View v) {
            Intent chatIntent = new Intent(mContext, ChatActivity.class);
            chatIntent.putExtra(CONVERSATION_EXTRA, thisConversation);
            mContext.startActivity(chatIntent);
        }
    }

    @Override
    public Filter getFilter() {
        if (conversationFilter == null)
            conversationFilter = new MatchesAdapter.ConversationFilter(this, conversationItemList);
        return conversationFilter;
    }

    public class ConversationFilter extends Filter {
        private final MatchesAdapter adapter;
        private final List<ConversationItem> originalList;
        private final List<ConversationItem> filteredList;

        public ConversationFilter(MatchesAdapter adapter, List<ConversationItem> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new LinkedList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final String filterPattern = constraint.toString().toLowerCase().trim();
            final FilterResults results = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filteredList.addAll(originalList);
            } else {
                for (final ConversationItem conversation : originalList) {
                    if (conversation.getFullName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(conversation);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        public void publishResults(CharSequence constraint, FilterResults results) {
            itemSortedList.clear();
            itemSortedList.addAll((ArrayList<ConversationItem>) results.values);
        }

    }
}
