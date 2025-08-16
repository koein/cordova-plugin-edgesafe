package com.weevi.edgesafe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
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

    private boolean applyPadding = true;
    private boolean padTop = true, padBottom = true, padSides = true;
    private boolean transparentBars = false; // default false in FIT
    private boolean lightStatusIcons = true, lightNavIcons = true;

    private boolean fitMode = true; // default FIT
    private int lastL = 0, lastT = 0, lastR = 0, lastB = 0;
    private CallbackContext watchCallback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // Preferences (override via app config.xml)
        String mode = preferences.getString("EdgeSafeMode", "fit");
        fitMode          = !"edge".equalsIgnoreCase(mode);
        applyPadding     = preferences.getBoolean("EdgeSafeApplyPadding", true);
        padTop           = preferences.getBoolean("EdgeSafePadTop", true);
        padBottom        = preferences.getBoolean("EdgeSafePadBottom", true);
        padSides         = preferences.getBoolean("EdgeSafePadSides", true);
        transparentBars  = preferences.getBoolean("EdgeSafeTransparentBars", false);
        lightStatusIcons = preferences.getBoolean("EdgeSafeLightStatusBarIcons", true);
        lightNavIcons    = preferences.getBoolean("EdgeSafeLightNavBarIcons", true);

        final Activity activity = cordova.getActivity();
        final Window window = activity.getWindow();

        cordova.getActivity().runOnUiThread(() -> {
            // Ensure keyboard resizes WebView (runtime to avoid manifest conflicts)
            try {
                window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                );
            } catch (Throwable ignore) {}

            // Icon appearance (works for both modes)
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, webView.getView());
            try { controller.setAppearanceLightStatusBars(lightStatusIcons); } catch (Throwable ignore) {}
            try { controller.setAppearanceLightNavigationBars(lightNavIcons); } catch (Throwable ignore) {}

            // Apply initial mode
            applyMode(window, webView.getView(), fitMode);
        });
    }

    private void applyMode(Window window, View content, boolean fit) {
        if (fit) {
            // Classic layout: content BELOW bars (no per-element CSS needed)
            WindowCompat.setDecorFitsSystemWindows(window, true);
            // Do not force transparent bars in FIT (use system default colors)
            try { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // leave system colors as-is
            }} catch (Throwable ignore) {}
            // No native padding needed
            content.setPadding(0,0,0,0);
            // Still attach listener to stream zeros (or any OEM-provided insets)
            attachInsetsListener(content, /*edgeMode*/ false);
        } else {
            // True edge-to-edge: content under bars (we'll pad with insets)
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

            int l = padSides ? sys.left : 0;
            int t = padTop   ? sys.top  : 0;
            int r = padSides ? sys.right: 0;
            int b = padBottom? sys.bottom:0;

            if (!edgeMode) {
                // In FIT mode, Android already lays out below bars; use zeros.
                l = t = r = b = 0;
            }

            lastL = l; lastT = t; lastR = r; lastB = b;

            if (edgeMode && applyPadding) {
                v.setPadding(l, t, r, b);
            } else if (!edgeMode) {
                v.setPadding(0,0,0,0);
            }

            sendInsetsToJs(l, t, r, b);
            return insets; // don't consume; IME continues to work
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

            case "setApplyPadding":
                applyPadding = args.optBoolean(0, true);
                final View v = webView.getView();
                cordova.getActivity().runOnUiThread(() -> {
                    if (applyPadding) v.setPadding(lastL, lastT, lastR, lastB);
                    else v.setPadding(0, 0, 0, 0);
                });
                cb.success();
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
