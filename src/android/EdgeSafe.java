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
    private boolean transparentBars = true;
    private boolean lightStatusIcons = true, lightNavIcons = true;

    private boolean fitMode = false; // false=edge (edge-to-edge), true=fit (below bars)

    private int lastL = 0, lastT = 0, lastR = 0, lastB = 0;
    private CallbackContext watchCallback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // Read prefs set in app's config.xml (or plugin defaults)
        applyPadding     = preferences.getBoolean("EdgeSafeApplyPadding", true);
        padTop           = preferences.getBoolean("EdgeSafePadTop", true);
        padBottom        = preferences.getBoolean("EdgeSafePadBottom", true);
        padSides         = preferences.getBoolean("EdgeSafePadSides", true);
        transparentBars  = preferences.getBoolean("EdgeSafeTransparentBars", true);
        lightStatusIcons = preferences.getBoolean("EdgeSafeLightStatusBarIcons", true);
        lightNavIcons    = preferences.getBoolean("EdgeSafeLightNavBarIcons", true);

        String mode = preferences.getString("EdgeSafeMode", "edge");
        fitMode = "fit".equalsIgnoreCase(mode);

        final Window window = cordova.getActivity().getWindow();

        cordova.getActivity().runOnUiThread(() -> {
            // Keyboard should resize the WebView
            try {
                window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                );
            } catch (Throwable ignore) {}

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transparentBars) {
                try { window.setStatusBarColor(Color.TRANSPARENT); } catch (Throwable ignore) {}
                try { window.setNavigationBarColor(Color.TRANSPARENT); } catch (Throwable ignore) {}
            }

            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, webView.getView());
            try { controller.setAppearanceLightStatusBars(lightStatusIcons); } catch (Throwable ignore) {}
            try { controller.setAppearanceLightNavigationBars(lightNavIcons); } catch (Throwable ignore) {}

            applyMode();       // sets decorFits and padding mode
            attachInsetsListener(); // starts watching insets (no-op in fit mode)
        });
    }

    private void applyMode() {
        final Window window = cordova.getActivity().getWindow();
        if (fitMode) {
            // Legacy: content sits below system bars (no edge-to-edge)
            WindowCompat.setDecorFitsSystemWindows(window, true);
            // No native padding needed
            applyPadding = false;
            // ensure zero padding
            final View v = webView.getView();
            v.setPadding(0,0,0,0);
        } else {
            // Proper edge-to-edge; we will pad with insets
            WindowCompat.setDecorFitsSystemWindows(window, false);
            // allow native padding if enabled
            ViewCompat.requestApplyInsets(webView.getView());
        }
    }

    private void attachInsetsListener() {
        final View content = webView.getView();

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            if (fitMode) {
                // In fit mode we don't modify padding or stream updates (values will be near 0)
                lastL = lastT = lastR = lastB = 0;
                sendInsetsToJs(0,0,0,0);
                return insets;
            }

            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int l = padSides ? sys.left : 0;
            int t = padTop   ? sys.top  : 0;
            int r = padSides ? sys.right: 0;
            int b = padBottom? sys.bottom:0;

            lastL = l; lastT = t; lastR = r; lastB = b;

            if (applyPadding) {
                v.setPadding(l, t, r, b);
            }
            sendInsetsToJs(l, t, r, b);
            return insets; // don't consume, so IME etc. keeps working
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
                String mode = args.optString(0, "edge");
                fitMode = "fit".equalsIgnoreCase(mode);
                cordova.getActivity().runOnUiThread(this::applyMode);
                cb.success();
                return true;
        }
        return false;
    }
}
