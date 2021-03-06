package me.muapp.android.UI.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dewarder.holdinglibrary.HoldingButtonLayout;
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.muapp.android.Classes.API.APIService;
import me.muapp.android.Classes.API.Handlers.BlockHandler;
import me.muapp.android.Classes.API.Handlers.LikeUserHandler;
import me.muapp.android.Classes.Chat.ChatReferences;
import me.muapp.android.Classes.Chat.Conversation;
import me.muapp.android.Classes.Chat.ConversationItem;
import me.muapp.android.Classes.Chat.Message;
import me.muapp.android.Classes.Chat.MuappSticker;
import me.muapp.android.Classes.FirebaseAnalytics.Analytics;
import me.muapp.android.Classes.Internal.LikeUserResult;
import me.muapp.android.Classes.Internal.User;
import me.muapp.android.Classes.Internal.UserContent;
import me.muapp.android.Classes.Util.Log;
import me.muapp.android.Classes.Util.PreferenceHelper;
import me.muapp.android.Classes.Util.Tutorials;
import me.muapp.android.Classes.Util.Utils;
import me.muapp.android.R;
import me.muapp.android.UI.Adapter.MessagesAdapter;
import me.muapp.android.UI.Fragment.AddAttachmentDialogFragment;
import me.muapp.android.UI.Fragment.CrushExpiredDialogFragment;
import me.muapp.android.UI.Fragment.CrushedExpiredLikeDialogFragment;
import me.muapp.android.UI.Fragment.Interface.OnTimeExpiredListener;
import me.muapp.android.UI.Fragment.MuappStickerDialogFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Build.VERSION_CODES.M;
import static me.muapp.android.Application.MuappApplication.DATABASE_REFERENCE;
import static me.muapp.android.UI.Activity.MatchActivity.MATCHING_CONVERSATION;
import static me.muapp.android.UI.Activity.MatchActivity.MATCHING_USER;
import static me.muapp.android.UI.Activity.ViewProfileActivity.FROM_CRUSH;
import static me.muapp.android.UI.Activity.ViewProfileActivity.FROM_MATCH;
import static me.muapp.android.UI.Activity.ViewProfileActivity.IS_LIKED_BY_ME;
import static me.muapp.android.UI.Activity.ViewProfileActivity.USER_ID;
import static me.muapp.android.UI.Activity.ViewProfileActivity.USER_NAME;

public class ChatActivity extends BaseActivity implements ChildEventListener, AddAttachmentDialogFragment.ChatAttachmentListener, MuappStickerDialogFragment.MuappStickerListener, OnTimeExpiredListener, LikeUserHandler {
    public static final String CONVERSATION_EXTRA = "CONVERSATION_EXTRA";
    public static final String CONTENT_FROM_CHAT = "CONTENT_FROM_CHAT";
    private static final String AUDIO_RECORDER_FILE_EXT_M4A = ".m4a";
    private static final String AUDIO_RECORDER_FOLDER = "Muapp/voiceNotes";
    private static final int PROFILE_VIEW_CODE = 486;
    private MediaRecorder recorder = null;
    CountDownTimer voiceCountdown;
    private static final int ATTACHMENT_PICTURE = 911;
    private static final int REQUEST_GALLERY_PERMISSIONS = 470;
    private static final int REQUEST_MIC_PERMISSIONS = 471;
    private static final float SLIDE_TO_CANCEL_ALPHA_MULTIPLIER = 2.5f;
    Boolean isOpponentConversationExist = false;
    Boolean isShowingDialog = false;
    Boolean isShowingTutorial = false;
    private File thisFile;
    HoldingButtonLayout input_holder;
    ConversationItem conversationItem = null;
    Toolbar toolbar;
    TextView toolbar_opponent_fullname, chat_user_last_conversations, txt_remaining_time, txt_recording_timer;
    RecyclerView recycler_conversation;
    DatabaseReference conversationReference;
    MessagesAdapter messagesAdapter;
    EditText etMessage;
    ImageButton chatSendButton, chatSendButtonVoicenote;
    ImageButton chatAddAttachmentButton;
    ImageView toolbar_opponent_photo;
    DatabaseReference myConversation, yourConversation, yourPresence, myLastSeenByYou;
    StorageReference myStorageReference;
    ChatReferences chatReferences;
    Vibrator v;
    Timer remainingTimer;
    int conversationsCount = 0;
    int ellapsedSeconds = 0;
    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    CardView container_empty_messages;
    Timer recordingTimer;
    TextView txt_name_empty_messages, txt_conversation_count_empty_messages, txt_created_empty_messages;
    ImageView img_empty_messages;
    public static final String PROFILE_VIEW_RESULT = "PROFILE_VIEW_RESULT";
    ValueEventListener presenceListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            boolean imOnline = false;
            try {
                imOnline = dataSnapshot.getValue(Boolean.class);
            } catch (Exception x) {

            }
            Drawable img = ContextCompat.getDrawable(ChatActivity.this, imOnline ? R.drawable.ic_chat_user_online : R.drawable.ic_chat_user_offline);
            toolbar_opponent_fullname.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void logEvent(Analytics.Messages.MESSAGE_TYPE message_type) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString(Analytics.Messages.MESSAGE_PROPERTY.Conversation.toString(), conversationItem.getConversation().getCrush() ? Analytics.Messages.MESSAGE_CONVERSATION.Crush.toString() : Analytics.Messages.MESSAGE_CONVERSATION.Match.toString());
        messageBundle.putString(Analytics.Messages.MESSAGE_PROPERTY.Type.toString(), message_type.toString());
        messageBundle.putBoolean(Analytics.Messages.MESSAGE_PROPERTY.First.toString(), messagesAdapter.getItemCount() == 0);
        mFirebaseAnalytics.logEvent(Analytics.Messages.MESSAGES_EVENT.Messages.toString(), messageBundle);
    }

    ValueEventListener lastSeenByOpponentListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Long lastSeenByYou = dataSnapshot.getValue(Long.class);
            if (lastSeenByYou != null)
                messagesAdapter.setLastSeenByOpponent(lastSeenByYou);
            else
                messagesAdapter.setLastSeenByOpponent(0L);

        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private boolean shouldSendToSettingsChatPermissions;


    // Voice note stuff
    int mAnimationDuration;
    View slide_to_cancel;
    View input_container;
    ViewPropertyAnimator mTimeAnimator;
    ViewPropertyAnimator mSlideToCancelAnimator;
    ViewPropertyAnimator mInputAnimator;
    private boolean hasAudioPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        conversationItem = getIntent().getParcelableExtra(CONVERSATION_EXTRA);
        if (conversationItem == null)
            finish();

        preferenceHelper.putCurrentActiveChat(conversationItem.getKey());
        Log.wtf("Entering item", conversationItem.toString());
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        toolbar_opponent_photo = (ImageView) findViewById(R.id.toolbar_opponent_photo);
        chat_user_last_conversations = (TextView) findViewById(R.id.chat_user_last_conversations);
        if (User.Gender.getGender(loggedUser.getGender()) != User.Gender.Female)
            chat_user_last_conversations.setVisibility(View.GONE);
        txt_remaining_time = (TextView) findViewById(R.id.txt_remaining_time);
        txt_recording_timer = (TextView) findViewById(R.id.txt_recording_timer);
        myStorageReference = FirebaseStorage.getInstance().getReference().child(String.valueOf(loggedUser.getId())).child("conversations").child(conversationItem.getKey());
        yourPresence = FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE).child("users").child(String.valueOf(conversationItem.getConversation().getOpponentId())).child("online");
        messagesAdapter = new MessagesAdapter(this);
        messagesAdapter.setFragmentManager(getSupportFragmentManager());
        messagesAdapter.setParticipantsPhotos(loggedUser.getPhoto(), conversationItem.getProfilePicture());
        messagesAdapter.setLoggedUserId(loggedUser.getId());
        conversationReference = FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE)
                .child("conversations")
                .child(String.valueOf(loggedUser.getId()))
                .child(conversationItem.getKey())
                .child("conversation");

        conversationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    container_empty_messages.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myLastSeenByYou = conversationReference.getParent().child("lastSeenByOpponent");
        myConversation = FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE)
                .child("conversations")
                .child(String.valueOf(loggedUser.getId()))
                .child(conversationItem.getKey());


        Log.wtf("convesration", "mine " + myConversation.getRef().toString());


        Log.wtf("convesration", "yours " + conversationItem.getConversation().getOpponentId() + " - " + conversationItem.getConversation().getOpponentConversationId());
        Log.wtf("convesration", conversationItem.getConversation().toString());
        yourConversation = FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE)
                .child("conversations")
                .child(String.valueOf(conversationItem.getConversation().getOpponentId()))
                .child(conversationItem.getConversation().getOpponentConversationId());

        Log.wtf("convesration", "yours " + yourConversation.getRef().toString());


        //For empty messages
        container_empty_messages = (CardView) findViewById(R.id.container_empty_messages);
        txt_name_empty_messages = (TextView) findViewById(R.id.txt_name_empty_messages);
        txt_created_empty_messages = (TextView) findViewById(R.id.txt_created_empty_messages);
        img_empty_messages = (ImageView) findViewById(R.id.img_empty_messages);
        txt_conversation_count_empty_messages = (TextView) findViewById(R.id.txt_conversation_count_empty_messages);
        toolbar_opponent_fullname = (TextView) findViewById(R.id.toolbar_opponent_fullname);
        if (User.Gender.getGender(loggedUser.getGender()) == User.Gender.Female)
            getRecentsConversations();

        txt_name_empty_messages.setText(String.format("%s %s", conversationItem.getName(), conversationItem.getLastName()));

        txt_created_empty_messages.setText((conversationItem.getConversation().getCrush() ? "Crush " : "Match ") + DateUtils.getRelativeTimeSpanString(conversationItem.getConversation().getCreationDate(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS).toString().toLowerCase());
        Glide.with(this).load(conversationItem.getProfilePicture()).placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder_error).error(R.drawable.ic_placeholder_error).diskCacheStrategy(DiskCacheStrategy.ALL).bitmapTransform(new CropCircleTransformation(this)).into(img_empty_messages);
        img_empty_messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitUserProfile();
            }
        });

        chatReferences = new ChatReferences(myConversation.getRef().toString(), yourConversation.getRef().toString());
        input_holder = (HoldingButtonLayout) findViewById(R.id.input_holder);
        slide_to_cancel = findViewById(R.id.slide_to_cancel);
        input_container = findViewById(R.id.input_container);
        etMessage = (EditText) findViewById(R.id.etMessage);
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() == 0) {
                    chatSendButton.setVisibility(View.GONE);
                    if (chatSendButtonVoicenote.getVisibility() == View.GONE)
                        chatSendButtonVoicenote.setVisibility(View.VISIBLE);
                    if (hasAudioPermissions)
                        input_holder.setButtonEnabled(true);
                } else {
                    if (chatSendButton.getVisibility() == View.GONE)
                        chatSendButton.setVisibility(View.VISIBLE);
                    if (chatSendButtonVoicenote.getVisibility() == View.VISIBLE)
                        chatSendButtonVoicenote.setVisibility(View.GONE);
                    if (hasAudioPermissions)
                        input_holder.setButtonEnabled(false);
                }
            }
        });
        chatSendButton = (ImageButton) findViewById(R.id.chatSendButton);
        chatSendButtonVoicenote = (ImageButton) findViewById(R.id.chatSendButtonVoicenote);
        chatSendButtonVoicenote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        chatAddAttachmentButton = (ImageButton) findViewById(R.id.chatAddAttachmentButton);
        toolbar_opponent_fullname.setText(conversationItem.getFullName());
        toolbar.findViewById(R.id.toolbar_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recycler_conversation = (RecyclerView) findViewById(R.id.recycler_conversation);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recycler_conversation.setLayoutManager(llm);
        recycler_conversation.setAdapter(messagesAdapter);
        Glide.with(this).load(conversationItem.getProfilePicture()).placeholder(R.drawable.ic_placeholder).bitmapTransform(new CropCircleTransformation(this)).error(R.drawable.ic_placeholder_error).diskCacheStrategy(DiskCacheStrategy.ALL).into(toolbar_opponent_photo);
        toolbar_opponent_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitUserProfile();
            }
        });
        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend(null);
            }
        });
        chatAddAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAttachmentDialogFragment.newInstance().show(getSupportFragmentManager(), "attach");
            }
        });
        setupVoicenote();
        updateMyConversationSeen();
    }


    private void getRecentsConversations() {
        final Long searchingDay = Calendar.getInstance().getTimeInMillis() - DateUtils.DAY_IN_MILLIS;
        FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE)
                .child("conversations").child(String.valueOf(conversationItem.getConversation().getOpponentId())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    Log.wtf("converdeation", s.getKey() + " " + conversationItem.getConversation().getOpponentConversationId());
                    Conversation c = s.getValue(Conversation.class);
                    c.setKey(s.getKey());
                    if (c != null && !s.getKey().equals(conversationItem.getConversation().getOpponentConversationId()) && !TextUtils.isEmpty(c.getOpponentConversationId())) {
                        Log.wtf("Conversations", c.toString());
                        Log.wtf("Conversations", searchingDay + "");
                        Long timeConversation = 0L;
                        if (c.getLastMessage() != null) {
                            Log.wtf("Conversations", "LastMessage " + c.getLastMessage().getTimeStamp());
                            timeConversation = c.getLastMessage().getTimeStamp();
                        } else {
                            Log.wtf("Conversations", "CreationDate " + c.getCreationDate());
                            timeConversation = c.getCreationDate();
                        }
                        Log.wtf("Conversations", "timeConversation " + timeConversation + "");
                        if (timeConversation > searchingDay)
                            conversationsCount++;
                        Log.wtf("Conversations", c.toString());
                    }

                }
                Log.wtf("Conversations", conversationsCount + " in last 24 hours");
                txt_conversation_count_empty_messages.setText(String.valueOf(conversationsCount));
                if (conversationsCount > 0)
                    chat_user_last_conversations.setText(String.format(getString(R.string.format_conversations_last_24), String.valueOf(conversationsCount)));

                if (!conversationItem.getConversation().getCrush() && new PreferenceHelper(ChatActivity.this).getTutorialMatchConversation()
                        ) {
                    new Tutorials(ChatActivity.this).showTutorialForView(chat_user_last_conversations, true, getString(R.string.lbl_tutorial_conversation_match_title), getString(R.string.lbl_tutorial_conversation_match_content), 120, new TapTargetView.Listener() {
                        @Override
                        public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                            super.onTargetDismissed(view, userInitiated);
                            new PreferenceHelper(ChatActivity.this).putTutorialMatchConversationDisabled();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupRemainingTime() {


        final Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTimeInMillis(conversationItem.getConversation().getCreationDate());
        expirationDate.add(Calendar.DATE, 1);
        remainingTimer = new Timer();
        remainingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long difference = expirationDate.getTime().getTime() - Calendar.getInstance().getTime().getTime();
                if (difference > 0) {
                    final long hh = (int) (TimeUnit.MILLISECONDS.toHours(difference));
                    final long mm = (int) (TimeUnit.MILLISECONDS.toMinutes(difference) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference)));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_remaining_time.setText(String.format(getString(R.string.format_remaining_time), getFormatedString(hh) + ":" + getFormatedString(mm)));
                            if (!isShowingTutorial && new PreferenceHelper(ChatActivity.this).getTutorialCrushConversation()) {
                                new Tutorials(ChatActivity.this).showTutorialForView(txt_remaining_time, true, getString(R.string.lbl_tutorial_conversation_crush_title), getString(R.string.lbl_tutorial_conversation_crush_content), null, new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                        super.onTargetDismissed(view, userInitiated);
                                        new PreferenceHelper(ChatActivity.this).putTutorialCrushConversationDisabled();
                                    }
                                });
                                isShowingTutorial = true;
                            }

                        }
                    });
                } else {
                    remainingTimer.cancel();
                    if (!isShowingDialog) {
                        if (conversationItem.getConversation().getLikeByMe() && !conversationItem.getConversation().getLikeByOpponent())
                            new CrushedExpiredLikeDialogFragment().newInstance(conversationItem).show(getSupportFragmentManager(), conversationItem.getKey());
                        else
                            new CrushExpiredDialogFragment().newInstance(conversationItem).show(getSupportFragmentManager(), conversationItem.getKey());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_remaining_time.setText(String.format(getString(R.string.format_remaining_time), "00:00"));
                        }
                    });
                    isShowingDialog = true;
                }

            }
        }, 0, 1000);
    }


    private String getFormatedString(long l) {
        String r = l + "";
        if (l < 10)
            r = "0" + l;
        return r;
    }

    private void updateYourConversationLastSeen() {
        try {
            yourConversation.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue(ConversationItem.class) != null) {
                        yourConversation.child("lastSeenByOpponent").setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception x) {

        }
    }

    private void updateMyConversationSeen() {
        myConversation.child("seen").setValue(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        conversationReference.keepSynced(true);
        Log.wtf("MessageReference", "Added");
        conversationReference.addChildEventListener(this);
        conversationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.wtf("Conversation", "Has " + dataSnapshot.getChildrenCount() + " messages");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        yourPresence.addValueEventListener(presenceListener);
        myLastSeenByYou.addValueEventListener(lastSeenByOpponentListener);
        updateYourConversationLastSeen();
        if (conversationItem.getConversation().getCrush()) {
            setupRemainingTime();
            txt_remaining_time.setVisibility(View.VISIBLE);
        } else
            txt_remaining_time.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messagesAdapter.stopMediaPlayer();
        updateYourConversationLastSeen();
        Log.wtf("MessageReference", "Removed");
        conversationReference.removeEventListener(this);
        myLastSeenByYou.removeEventListener(lastSeenByOpponentListener);
        yourPresence.removeEventListener(presenceListener);
        if (remainingTimer != null)
            remainingTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceHelper.clearCurrentActiveChat();
        if (messagesAdapter != null)
            messagesAdapter.clearMediaPlayer();
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if (container_empty_messages.getVisibility() == View.VISIBLE)
            container_empty_messages.setVisibility(View.GONE);
        updateYourConversationLastSeen();
        updateMyConversationSeen();
        Message m = dataSnapshot.getValue(Message.class);
        if (m != null) {
            m.setKey(dataSnapshot.getKey());
            messagesAdapter.addMessage(m);

        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        updateYourConversationLastSeen();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        if (container_empty_messages.getVisibility() == View.GONE)
            container_empty_messages.setVisibility(View.VISIBLE);
        // updateYourConversationLastSeen();
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        updateYourConversationLastSeen();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private void attemptSend(UserContent content) {
        if (!TextUtils.isEmpty(etMessage.getText().toString()) || content != null) {
            logEvent(content != null ? content.getCatContent().equals("contentAud") ? Analytics.Messages.MESSAGE_TYPE.VoiceNote : Analytics.Messages.MESSAGE_TYPE.Photo : Analytics.Messages.MESSAGE_TYPE.Text);
            Log.wtf("Sending message", "");
            Message m = new Message();
            m.setTimeStamp(new Date().getTime());
            m.setSenderId(loggedUser.getId());
            m.setContent(etMessage.getText().toString());
            if (content != null) {
                content.setLikes(0);
                m.setAttachment(content);
            }
            etMessage.setText("");
            conversationReference.child(conversationReference.push().getKey()).setValue(m.toMap());
            yourConversation.child("conversation").child(yourConversation.push().getKey()).setValue(m.toMap());
            myConversation.child("lastMessage").setValue(m.toMap());
            yourConversation.child("lastMessage").setValue(m.toMap());
            yourConversation.child("seen").setValue(false);
            sendPushMessage();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        messagesAdapter.clearConversation();
    }

    private void sendPushMessage() {
        if (!TextUtils.isEmpty(conversationItem.getPushToken())) {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject sendObject = new JSONObject();
            try {
                sendObject.put("to", conversationItem.getPushToken());
                sendObject.put("collapse_key", conversationItem.getKey());
                sendObject.put("priority", "high");
                sendObject.put("content_available", true);
                JSONObject data = new JSONObject();
                data.put("message_dialog_key", conversationItem.getConversation().getOpponentConversationId());
                sendObject.put("data", data);
                JSONObject notification = new JSONObject();
                notification.put("tag", conversationItem.getKey());
                notification.put("title", getString(R.string.app_name));
                notification.put("color", "#ff666e");
                notification.put("sound", "default");
                notification.put("body_loc_key", "notif_sent_message");
                notification.put("body_loc_args", new JSONArray(new String[]{loggedUser.getFirstName()}));
                sendObject.put("notification", notification);
            } catch (Exception x) {

            }
            Log.wtf("Push", sendObject.toString());
            RequestBody body = RequestBody.create(mediaType, sendObject.toString());
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("authorization", "key=AIzaSyCAPtbJ8ZFXLF8ot_hyadW2_zqD9E9fMkE")
                    .addHeader("content-type", "application/json")
                    .build();
            Log.wtf("sendPushMessage", sendObject.toString());
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });
        }
    }

    @Override
    public void onChatAttachmentItemClicked(AddAttachmentDialogFragment.AttachmentType attachmentType) {
        switch (attachmentType) {
            //Pictures
            case TypePicture:
                if (checkAndRequestGalleryPermissions())
                    galleryIntent();
                break;
            case TypeSticker:
                MuappStickerDialogFragment.newInstance().show(getSupportFragmentManager(), "sticker");
                break;
            case TypeGif:
                Intent giphyIntent = new Intent(this, AddGiphyActivity.class);
                giphyIntent.putExtra(CONTENT_FROM_CHAT, chatReferences);
                startActivity(giphyIntent);
                break;
            case TypeMusic:
                Intent spotifyIntent = new Intent(this, AddSpotifyActivity.class);
                spotifyIntent.putExtra(CONTENT_FROM_CHAT, chatReferences);
                startActivity(spotifyIntent);
                break;
            case TypeYoutube:
                Intent youtubeIntent = new Intent(this, AddYoutubeActivity.class);
                youtubeIntent.putExtra(CONTENT_FROM_CHAT, chatReferences);
                startActivity(youtubeIntent);
                break;
        }

    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), ATTACHMENT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ATTACHMENT_PICTURE:
                    Log.wtf("attach", "picture started");
                    onSelectFromGalleryResult(data);
                    break;
                case PROFILE_VIEW_CODE:
                    Boolean hasMuapp = data.getBooleanExtra(PROFILE_VIEW_RESULT, false);
                    Log.wtf("ViewProfile", "Returned OK + Muapp? " + hasMuapp);
                    if (hasMuapp) {
                        Fragment f = getSupportFragmentManager().findFragmentByTag(conversationItem.getKey());
                        if (f != null) {
                            android.support.v4.app.DialogFragment df = (android.support.v4.app.DialogFragment) f;
                            df.dismiss();
                        }
                        onExpiredMuapp();
                    } else
                        onExpiredNoMuapp();
                    break;
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        File f = null;
        String filePath = "";
        Uri selectedImage = null;
        if (data != null && data.getData() != null) {
            try {
                selectedImage = data.getData();
                Log.v("file", selectedImage.toString());
                String wholeID = DocumentsContract.getDocumentId(selectedImage);
                String id = wholeID.split(":")[1];
                String[] column = {MediaStore.Images.Media.DATA};
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
                int columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                    Log.v("filePath", filePath);
                    f = new File(filePath);
                    compressAndUpload(f);
                } else {
                }
                cursor.close();
                Log.wtf("selected", f.getAbsolutePath());
            } catch (Exception x) {
                try {
                    f = new File(getRealPathFromURI(selectedImage));
                    compressAndUpload(f);
                } catch (Exception ex) {

                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void compressAndUpload(File f) {
        final UserContent thisContent = new UserContent();
        Log.wtf("selected", f.exists() + "");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
        final StorageReference imageReference = myStorageReference.child("media" + new Date().getTime());
        UploadTask uploadTask = imageReference.putBytes(out.toByteArray());
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = task.getResult().getDownloadUrl();
                    thisContent.setComment("");
                    thisContent.setCreatedAt(new Date().getTime());
                    thisContent.setCatContent("contentPic");
                    thisContent.setContentUrl(downloadUrl.toString());
                    thisContent.setStorageName(imageReference.getPath());
                    attemptSend(thisContent);
                }
            }
        });
    }


    private boolean checkAndRequestGalleryPermissions() {
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(ChatActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_GALLERY_PERMISSIONS);
            return false;
        }
        return true;
    }

    private boolean checkAndRequestMicPermissions() {
        int permissionMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionMic != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(ChatActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_MIC_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PermissionChecker.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            hasAudioPermissions = false;
            input_holder.setButtonEnabled(false);
            chatSendButtonVoicenote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkAndRequestMicPermissions();
                }
            });
        } else {
            hasAudioPermissions = true;
        }
    }

    @Override
    @TargetApi(M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionsGranted = true;
        switch (requestCode) {
            case REQUEST_GALLERY_PERMISSIONS:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        permissionsGranted = false;
                        // user rejected the permission
                        boolean showRationale = shouldShowRequestPermissionRationale(permission);
                        if (!showRationale) {
                            // user also CHECKED "never ask again"
                            shouldSendToSettingsChatPermissions = true;
                            break;
                        }
                    }
                }
                if (permissionsGranted) {
                    galleryIntent();
                }
                break;
            case REQUEST_MIC_PERMISSIONS:
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        permissionsGranted = false;
                        // user rejected the permission
                        boolean showRationale = shouldShowRequestPermissionRationale(permission);
                        if (!showRationale) {
                            // user also CHECKED "never ask again"
                            shouldSendToSettingsChatPermissions = true;
                            break;
                        }
                    }
                }
                if (permissionsGranted) {
                    input_holder.setButtonEnabled(true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
//Voicenote Setup

    private void setupVoicenote() {
        input_holder.setIcon(R.drawable.ic_mic_white);
        mAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        input_holder.addListener(new HoldingButtonLayoutListener() {
            @Override
            public void onBeforeExpand() {
                cancelAllAnimations();
                slide_to_cancel.setTranslationX(0f);
                slide_to_cancel.setAlpha(0f);
                slide_to_cancel.setVisibility(View.VISIBLE);
                mSlideToCancelAnimator = slide_to_cancel.animate().alpha(1f).setDuration(mAnimationDuration);
                mSlideToCancelAnimator.start();

                mInputAnimator = input_container.animate().alpha(0f).setDuration(mAnimationDuration);
                mInputAnimator.setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        input_container.setVisibility(View.INVISIBLE);
                        mInputAnimator.setListener(null);
                    }
                });
                mInputAnimator.start();
            }

            @Override
            public void onExpand() {
                if (checkAndRequestMicPermissions())
                    startRecording();
            }

            @Override
            public void onBeforeCollapse() {
                cancelAllAnimations();
                mSlideToCancelAnimator = slide_to_cancel.animate().alpha(0f).setDuration(mAnimationDuration);
                mSlideToCancelAnimator.setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        slide_to_cancel.setVisibility(View.INVISIBLE);
                        mSlideToCancelAnimator.setListener(null);
                    }
                });
                mSlideToCancelAnimator.start();

                input_container.setAlpha(0f);
                input_container.setVisibility(View.VISIBLE);
                mInputAnimator = input_container.animate().alpha(1f).setDuration(mAnimationDuration);
                mInputAnimator.start();
            }

            @Override
            public void onCollapse(boolean isCancel) {
                stopRecording(!isCancel);
                Log.wtf("collapse", !isCancel + "");
            }

            @Override
            public void onOffsetChanged(float offset, boolean isCancel) {
                slide_to_cancel.setTranslationX(-input_holder.getWidth() * offset);
                slide_to_cancel.setAlpha(1 - SLIDE_TO_CANCEL_ALPHA_MULTIPLIER * offset);
                if (offset > 0.65f) {
                    input_holder.cancel();
                }
            }
        });
    }

    private void cancelAllAnimations() {
        if (mInputAnimator != null) {
            mInputAnimator.cancel();
        }

        if (mSlideToCancelAnimator != null) {
            mSlideToCancelAnimator.cancel();
        }

        if (mTimeAnimator != null) {
            mTimeAnimator.cancel();
        }
    }

    private void startRecording() {
        Log.wtf("startRecording", "now");
        if (thisFile != null) {
            thisFile.delete();
        }
        v.vibrate(100);
        //For timer
        /*final Float Length = 20000f;
        final Float Period = 1f;
        voiceCountdown = new CountDownTimer(Length.longValue(), Period.longValue()) {
            @Override
            public void onTick(long millisUntilFinished_) {

            }

            @Override
            public void onFinish() {
                stopRecording(true);

            }
        }.start();
        */
        thisFile = new File(getFilename());
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(thisFile.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(256);
        recorder.setAudioChannels(1);
        recorder.setAudioSamplingRate(44100);
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.v("RECORDING", "Error: " + what + ", " + extra);
            }
        });
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.v("RECORDING", "Error: " + what + ", " + extra);
            }
        });
        try {
            recorder.prepare();
            recorder.start();

            recordingTimer = new Timer();
            recordingTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            txt_recording_timer.setText(String.valueOf(formatter.format(new Date(ellapsedSeconds * 1000))));
                            ellapsedSeconds++;
                        }
                    });
                }
            }, 0, 1000);


        } catch (IllegalStateException e) {
            Log.wtf("startRecording1", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.wtf("startRecording2", e.getMessage());
            e.printStackTrace();
        }
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File newfile = new File(filepath, AUDIO_RECORDER_FOLDER);
        if (!newfile.exists()) {
            newfile.mkdirs();
        }
        String fname = (newfile.getAbsolutePath() + "/" + "voiceNote" + new Date().getTime() + AUDIO_RECORDER_FILE_EXT_M4A);
        Log.wtf("VOICENOTE", fname);
        File f = new File(fname);
        if (f.exists())
            f.delete();
        return fname;
    }

    private void stopRecording(boolean sendFile) {
        if (recordingTimer != null)
            recordingTimer.cancel();
        if (voiceCountdown != null)
            voiceCountdown.cancel();
        try {
            if (recorder != null) {
                ellapsedSeconds = 0;
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
                v.vibrate(100);
                if (sendFile) {
                    sendVoiceNote(thisFile);
                } else {
                    Log.wtf("Sending", "delete " + thisFile.getAbsolutePath());
                    thisFile.delete();
                    thisFile = null;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void sendVoiceNote(final File f) {
        if (thisFile.exists() && Utils.getAudioLength(this, f) >= 1) {
            try {
                int size = (int) f.length();
                byte[] bytes = new byte[size];
                final UserContent thisContent = new UserContent();
                thisContent.setCreatedAt(new Date().getTime());
                thisContent.setLikes(0);
                thisContent.setAudioLenght(Utils.getAudioLength(this, f));
                thisContent.setCatContent("contentAud");
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
                buf.read(bytes, 0, bytes.length);
                buf.close();
                final StorageReference audioReference = myStorageReference.child("audio" + new Date().getTime());
                UploadTask uploadTask = audioReference.putBytes(bytes);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = task.getResult().getDownloadUrl();
                            thisContent.setContentUrl(downloadUrl.toString());
                            thisContent.setStorageName(audioReference.getPath());
                            Log.wtf("Sending complete", thisContent.toString());
                            attemptSend(thisContent);
                            f.delete();
                            thisFile = null;
                        }
                    }
                });
            } catch (Exception x) {

            }
        }
    }

    @Override
    public void onExpiredMuapp() {
        conversationItem.getConversation().setLikeByMe(true);
        txt_remaining_time.setVisibility(View.GONE);
        Log.wtf("Expired", "Muapp");
        Log.wtf("Expired", txt_remaining_time.getId() + " Must be gone");
        Log.wtf("likeUser", "Sending: " + conversationItem.getConversation().getKey() + " " + conversationItem.getConversation().getOpponentConversationId());
        new APIService(this).likeUser(conversationItem.getConversation().getOpponentId(), null, this, conversationItem.getConversation().getKey(), conversationItem.getConversation().getOpponentConversationId());
        if (!conversationItem.getConversation().getLikeByOpponent()) {
            new CrushedExpiredLikeDialogFragment().newInstance(conversationItem).show(getSupportFragmentManager(), conversationItem.getKey());
            isShowingDialog = true;
        }
        /*
        FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE).child("conversations").child(String.valueOf(loggedUser.getId())).child(conversationItem.getKey()).removeValue();
        FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE).child("conversations").child(String.valueOf(conversationItem.getConversation().getOpponentId())).child(conversationItem.getConversation().getOpponentConversationId()).removeValue();
        */

    }

    @Override
    public void onExpiredNoMuapp() {
        Log.wtf("Expired", "NoMuapp");
        showProgressDialog();
        new APIService(this).blockUser(conversationItem.getConversation().getOpponentId(), conversationItem.getKey(), conversationItem.getConversation().getOpponentConversationId(), new BlockHandler() {
            @Override
            public void onSuccess(String jsonObject) {
                myStorageReference.delete();
                hideProgressDialog();
                finish();
            }

            @Override
            public void onResponseError(Exception x) {
                hideProgressDialog();
            }

            @Override
            public void onFailure(boolean isSuccessful, String responseString) {
                hideProgressDialog();
            }
        });

    }

    @Override
    public void onExpiredCancel() {
        Log.wtf("Expired", "Cancelled");
        final Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTimeInMillis(conversationItem.getConversation().getCreationDate());
        expirationDate.add(Calendar.DATE, 1);
        long difference = expirationDate.getTime().getTime() - Calendar.getInstance().getTime().getTime();
        if (difference <= 0) {
            finish();
        }

    }

    @Override
    public void onPictureClicked() {
        visitUserProfile();
    }

    private void visitUserProfile() {
        Bundle cpBundle = new Bundle();
        cpBundle.putString(Analytics.Conversation_Profile.CONVERSATION_PROFILE_PROPERTY.Type.toString(), conversationItem.getConversation().getCrush() ? Analytics.Conversation_Profile.CONVERSATION_PROFILE_TYPE.Crushed.toString() : Analytics.Conversation_Profile.CONVERSATION_PROFILE_TYPE.Matched.toString());
        mFirebaseAnalytics.logEvent(Analytics.Conversation_Profile.CONVERSATION_PROFILE_EVENT.Conversation_Profile.toString(), cpBundle);
        Log.wtf("visitUserProfile", conversationItem.toString());
        Intent profileIntent = new Intent(this, ViewProfileActivity.class);
        profileIntent.putExtra(USER_ID, conversationItem.getConversation().getOpponentId());
        profileIntent.putExtra(USER_NAME, conversationItem.getFullName());
        profileIntent.putExtra(IS_LIKED_BY_ME, conversationItem.getConversation().getLikeByMe());
        profileIntent.putExtra(FROM_CRUSH, conversationItem.getConversation().getCrush());
        profileIntent.putExtra(FROM_MATCH, !conversationItem.getConversation().getCrush());
        startActivityForResult(profileIntent, PROFILE_VIEW_CODE);
    }


    @Override
    public void onFailure(boolean isSuccessful, String responseString) {

    }

    @Override
    public void onSuccess(final int responseCode, final LikeUserResult result) {
        Log.wtf("LikeResult", result.toString());
        try {
            if (result.getLikeUserMatch() != null) {
                Bundle matchBundle = new Bundle();
                matchBundle.putString(Analytics.Match.MATCH_PROPERTY.Type.toString(), Analytics.Match.MATCH_TYPE.New.toString());
                FirebaseAnalytics.getInstance(this).logEvent(Analytics.Match.MATCH_EVENT.Match.toString(), matchBundle);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DATABASE_REFERENCE)
                        .child("conversations")
                        .child(String.valueOf(loggedUser.getId())).child(result.getDialogKey());
                Log.wtf("conversationReference", reference.toString());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Conversation c = dataSnapshot.getValue(Conversation.class);
                        if (c != null) {
                            FirebaseDatabase.getInstance().getReference(DATABASE_REFERENCE).child("users").child(String.valueOf(result.getLikeUserMatch().getMatcherId())).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    ConversationItem conversationItem = dataSnapshot.getValue(ConversationItem.class);
                                    if (conversationItem != null) {
                                        conversationItem.setKey(result.getDialogKey());
                                        conversationItem.setConversation(c);
                                        Intent matchIntent = new Intent(ChatActivity.this, MatchActivity.class);
                                        matchIntent.putExtra(MATCHING_USER, result.getLikeUserMatch().getLikeUserMatchUser());
                                        matchIntent.putExtra(MATCHING_CONVERSATION, conversationItem);
                                        matchIntent.putExtra(MatchActivity.FROM_MATCH, true);
                                        startActivity(matchIntent);
                                    } else {
                                        Log.wtf("ConversationItem", "isNull");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            Log.wtf("Conversation", "isNull");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                Log.wtf("LikeResult", "Theres not motherfucking match");
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    @Override
    public void onMuappStickerClicked(MuappSticker sticker) {
        final UserContent stickerContent = new UserContent();
        stickerContent.setCreatedAt(new Date().getTime());
        stickerContent.setLikes(0);
        stickerContent.setCatContent("contentStkr");
        stickerContent.setContentUrl(sticker.getImage());
        attemptSend(stickerContent);
    }
}


