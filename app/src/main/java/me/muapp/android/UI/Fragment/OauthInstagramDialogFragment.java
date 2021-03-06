package me.muapp.android.UI.Fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import me.muapp.android.Classes.Util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.muapp.android.R;


/**
 * Created by rulo on 11/04/17.
 */

public class OauthInstagramDialogFragment extends DialogFragment {
    private WebView webViewOauth;
    int userId;
    public static final String TAG = "OauthInstagramDialogFragment";

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            String mainUrl = String.format("http://dev.muapp.me/oauth/connect?user_id=%s", userId);
            Log.wtf("opening", mainUrl);
            clearCookies();
            webViewOauth.clearCache(true);
            webViewOauth.loadUrl(mainUrl);
            webViewOauth.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.setVisibility(View.VISIBLE);
                }

                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                    return shouldOverrideUrlLoading(url);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                    Uri uri = request.getUrl();
                    return shouldOverrideUrlLoading(uri.toString());
                }

                private boolean shouldOverrideUrlLoading(final String url) {
                    Log.i(TAG, "shouldOverrideUrlLoading() URL : " + url);
                    if (url.startsWith("http://dev.muapp.me/oauth/callback") && url.contains("&code="))
                        OauthInstagramDialogFragment.this.dismiss();
                    return false; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
                }
            });
            WebSettings webSettings = webViewOauth.getSettings();
            webSettings.setJavaScriptEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.oauth_instagram_layout, container, false);
        webViewOauth = (WebView) v.findViewById(R.id.web_oauth);
        return v;
    }
}
