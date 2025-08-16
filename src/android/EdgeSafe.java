package com.weevi.edgesafe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class EdgeSafe extends CordovaPlugin {

    private boolean transparentBars = false;
    private boolean lightStatusIcons = true, lightNavIcons = true;
    private boolean forceOpaqueBars = false;

    private boolean fitMode = true; // default FIT
    private int lastL = 0, lastT = 0, lastR = 0, lastB = 0;
    private CallbackContext watchCallback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        String mode = preferences.getString("EdgeSafeMode", "fit");
        fitMode          = !"edge".equalsIgnoreCase(mode);
        transparentBars  = preferences.getBoolean("EdgeSafeTransparentBars", false);
        lightStatusIcons = preferences.getBoolean("EdgeSafeLightStatusBarIcons", true);
        lightNavIcons    = preferences.getBoolean("EdgeSafeLightNavBarIcons", true);
        forceOpaqueBars  = preferences.getBoolean("EdgeSafeForceOpaqueBars", false);

        final Activity activity = cordova.getActivity();
        final Window window = activity.getWindow();

        cordova.getActivity().runOnUiThread(() -> {
            try {
                window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                );
            } catch (Throwable ignore) {}

            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, webView.getView());
            try { controller.setAppearanceLightStatusBars(lightStatusIcons); } catch (Throwable ignore) {}
            try { controller.setAppearanceLightNavigationBars(lightNavIcons); } catch (Throwable ignore) {}

            applyMode(window, webView.getView(), fitMode);
        });
    }

    private void clearOverlayFlags(Window window) {
        try {
            final View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            flags &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decor.setSystemUiVisibility(flags);
        } catch (Throwable ignore) {}

        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } catch (Throwable ignore) {}
    }

    private void maybeForceOpaqueBars(Window window) {
        if (!forceOpaqueBars) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try { window.setStatusBarColor(Color.BLACK); } catch (Throwable ignore) {}
            try { window.setNavigationBarColor(Color.BLACK); } catch (Throwable ignore) {}
        }
    }

    private void setMargins(View v, int l, int t, int r, int b) {
        try {
            ViewGroup.LayoutParams p = v.getLayoutParams();
            if (p instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) p;
                if (mp.leftMargin != l || mp.topMargin != t || mp.rightMargin != r || mp.bottomMargin != b) {
                    mp.leftMargin = l;
                    mp.topMargin = t;
                    mp.rightMargin = r;
                    mp.bottomMargin = b;
                    v.setLayoutParams(mp);
                }
            }
        } catch (Throwable ignore) {}
    }

    private void applyMode(Window window, View content, boolean fit) {
        if (fit) {
            WindowCompat.setDecorFitsSystemWindows(window, true);
            clearOverlayFlags(window);
            maybeForceOpaqueBars(window);
            setMargins(content, 0, 0, 0, 0);
            attachInsetsListener(content, /*edgeMode*/ false);
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transparentBars) {
                try { window.setStatusBarColor(Color.TRANSPARENT); } catch (Throwable ignore) {}
                try { window.setNavigationBarColor(Color.TRANSPARENT); } catch (Throwable ignore) {}
            }
            attachInsetsListener(content, /*edgeMode*/ true);
            ViewCompat.requestApplyInsets(content);
        }
    }

    private void attachInsetsListener(View content, boolean edgeMode) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int l = sys.left;
            int t = sys.top;
            int r = sys.right;
            int b = sys.bottom;

            if (!edgeMode) {
                l = t = r = b = 0;
            } else {
                setMargins(v, l, t, r, b); // shrink WebView via margins so viewport is safe
            }

            lastL = l; lastT = t; lastR = r; lastB = b;

            sendInsetsToJs(l, t, r, b);
            return insets;
        });
        ViewCompat.requestApplyInsets(content);
    }

    private void sendInsetsToJs(int l, int t, int r, int b) {
        if (watchCallback == null) return;
        try {
            JSONObject obj = new JSONObject();
            obj.put("left", l);
            obj.put("top", t);
            obj.put("right", r);
            obj.put("bottom", b);

            PluginResult pr = new PluginResult(PluginResult.Status.OK, obj);
            pr.setKeepCallback(true);
            watchCallback.sendPluginResult(pr);
        } catch (JSONException ignore) {}
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cb) throws JSONException {
        switch (action) {
            case "watch":
                watchCallback = cb;
                PluginResult no = new PluginResult(PluginResult.Status.NO_RESULT);
                no.setKeepCallback(true);
                cb.sendPluginResult(no);
                return true;

            case "getInsets":
                JSONObject obj = new JSONObject();
                obj.put("left", lastL);
                obj.put("top", lastT);
                obj.put("right", lastR);
                obj.put("bottom", lastB);
                cb.success(obj);
                return true;

            case "setMode":
                String mode = args.optString(0, "fit");
                final boolean fit = !"edge".equalsIgnoreCase(mode);
                fitMode = fit;
                final Window window = cordova.getActivity().getWindow();
                final View content = webView.getView();
                cordova.getActivity().runOnUiThread(() -> applyMode(window, content, fit));
                cb.success();
                return true;
        }
        return false;
    }
}
