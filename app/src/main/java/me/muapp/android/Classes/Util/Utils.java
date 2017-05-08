package me.muapp.android.Classes.Util;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by rulo on 22/03/17.
 */

public class Utils {
    private static final String jsonUseInvitation = "has_use_invitation";

    public static int getScreenWidth(Context ctx) {
        return getDisplayMetrics(ctx).widthPixels;
    }

    public static DisplayMetrics getDisplayMetrics(Context ctx) {
        DisplayMetrics dm = new DisplayMetrics();

        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);

        return dm;
    }

    public static String serializeUser(JSONObject user) {
        try {
            int has_use_invitation = user.getInt(jsonUseInvitation);
            if (has_use_invitation > 0)
                user.put(jsonUseInvitation, true);
            else
                user.put(jsonUseInvitation, false);
        } catch (Exception x) {
        }
        Log.v("serializeUser", user.toString());
        return user.toString();
    }

    public static String serializeMatchingUsers(String matchingString) {
        try {
            JSONObject matchingUsers = new JSONObject(matchingString);
            JSONArray matchingUserArray = matchingUsers.getJSONArray("users");
            for (int i = 0; i < matchingUserArray.length(); i++) {
                JSONObject user = matchingUserArray.getJSONObject(i);
                try {
                    int has_use_invitation = user.getInt(jsonUseInvitation);
                    if (has_use_invitation > 0)
                        user.put(jsonUseInvitation, true);
                    else
                        user.put(jsonUseInvitation, false);
                    matchingUserArray.put(i, user);
                } catch (Exception ex) {

                }
            }
            matchingUsers.put("users", matchingUserArray);
            return matchingUsers.toString();
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }

    public String secondToTimeFormat(int seconds) {
        int s = seconds % 60;
        int m = seconds / 60;

        String res = "";
        if (m < 10) {
            res += "0";
        }
        res += m + ":";
        if (s < 10) {
            res += "0";
        }
        res += s;
        return res;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void animView(final View v, final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            v.setVisibility(show ? View.VISIBLE : View.GONE);
            v.animate().setDuration(200).alpha(
                    !show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    v.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            v.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void animView(final View v, final boolean show, float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            if (show)
                v.setVisibility(show ? View.VISIBLE : View.GONE);
            v.animate().setDuration(200).alpha(
                    !show ? 0 : alpha).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    v.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            v.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public static boolean hasLocationPermissions(Context context) {
        int permissionFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionFine != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarse != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            return false;
        }
        return true;
    }

}
