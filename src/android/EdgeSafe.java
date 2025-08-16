package com.weevi.edgesafe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
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

    private boolean transparentBars = true;
    private boolean lightStatusIcons = true, lightNavIcons = true;
    private boolean forceOpaqueBars = false;

    private String statusBarColorPref = "auto";
    private String navBarColorPref = "auto";
    private String navBarDividerColorPref = "auto";

    private boolean fitMode = false; // default EDGE
    private int lastL = 0, lastT = 0, lastR = 0, lastB = 0;
    private CallbackContext watchCallback;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        String mode = preferences.getString("EdgeSafeMode", "edge");
        fitMode          = "fit".equalsIgnoreCase(mode);
        transparentBars  = preferences.getBoolean("EdgeSafeTransparentBars", true);
        // Unprefixed booleans
        lightStatusIcons = preferences.getBoolean("LightStatusBarIcons", true);
        lightNavIcons    = preferences.getBoolean("LightNavBarIcons", true);
        forceOpaqueBars  = preferences.getBoolean("EdgeSafeForceOpaqueBars", false);

        // Unprefixed colors ONLY
        statusBarColorPref = preferences.getString("StatusBarColor", "auto");
        navBarColorPref    = preferences.getString("NavBarColor", "auto");
        navBarDividerColorPref = preferences.getString("NavBarDividerColor", "auto");

        final Activity activity = cordova.getActivity();
        final Window window = activity.getWindow();

        cordova.getActivity().runOnUiThread(() -> {
            try {
                window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                );
            } catch (Throwable ignore) {}

            applyIconAppearance(window, webView.getView(), lightStatusIcons, lightNavIcons);
            applyMode(window, webView.getView(), fitMode);
            applyColors(window, statusBarColorPref, navBarColorPref, navBarDividerColorPref);
        });
    }

    // --- Icon contrast ---
    private void applyIconAppearance(Window window, View content, boolean lightStatus, boolean lightNav) {
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, content);
        try { controller.setAppearanceLightStatusBars(lightStatus); } catch (Throwable ignore) {}
        try { controller.setAppearanceLightNavigationBars(lightNav); } catch (Throwable ignore) {}
    }

    // --- Colors ---
    private Integer parseColorMaybe(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase();
        if (TextUtils.isEmpty(v) || "auto".equals(v)) return null; // no change
        if ("transparent".equals(v)) return Color.TRANSPARENT;
        try {
            return Color.parseColor(v); // #RRGGBB or #AARRGGBB
        } catch (Throwable ignore) {}
        return null;
    }

    private void applyColors(Window window, String status, String nav, String navDivider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Integer s = parseColorMaybe(status);
            Integer n = parseColorMaybe(nav);
            if (s != null) {
                try { window.setStatusBarColor(s); } catch (Throwable ignore) {}
            }
            if (n != null) {
                try { window.setNavigationBarColor(n); } catch (Throwable ignore) {}
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Integer d = parseColorMaybe(navDivider);
                if (d != null) {
                    try { window.setNavigationBarDividerColor(d); } catch (Throwable ignore) {}
                }
            }
        }
    }

    // --- Layout / Insets ---
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
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime  = insets.getInsets(WindowInsetsCompat.Type.ime());

            int l = bars.left;
            int t = bars.top;
            int r = bars.right;
            int b = Math.max(bars.bottom, ime.bottom);

            if (!edgeMode) {
                l = t = r = b = 0;
            } else {
                setMargins(v, l, t, r, b);
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
                String mode = args.optString(0, "edge");
                final boolean fit = "fit".equalsIgnoreCase(mode);
                fitMode = fit;
                final Window window = cordova.getActivity().getWindow();
                final View content = webView.getView();
                cordova.getActivity().runOnUiThread(() -> applyMode(window, content, fit));
                cb.success();
                return true;

            // Colors (individual)
            case "statusBarColor":
                final String s = args.isNull(0) ? null : args.optString(0, null);
                cordova.getActivity().runOnUiThread(() -> applyColors(cordova.getActivity().getWindow(), s, null, null));
                cb.success(); return true;
            case "navBarColor":
                final String n = args.isNull(0) ? null : args.optString(0, null);
                cordova.getActivity().runOnUiThread(() -> applyColors(cordova.getActivity().getWindow(), null, n, null));
                cb.success(); return true;
            case "navBarDividerColor":
                final String d = args.isNull(0) ? null : args.optString(0, null);
                cordova.getActivity().runOnUiThread(() -> applyColors(cordova.getActivity().getWindow(), null, null, d));
                cb.success(); return true;

            // Icons
            case "lightStatusIcons":
                final boolean lsi = args.optBoolean(0, true);
                lightStatusIcons = lsi;
                cordova.getActivity().runOnUiThread(() -> applyIconAppearance(cordova.getActivity().getWindow(), webView.getView(), lsi, lightNavIcons));
                cb.success(); return true;
            case "lightNavIcons":
                final boolean lni = args.optBoolean(0, true);
                lightNavIcons = lni;
                cordova.getActivity().runOnUiThread(() -> applyIconAppearance(cordova.getActivity().getWindow(), webView.getView(), lightStatusIcons, lni));
                cb.success(); return true;

            // Back-compat combined setter left intact (doesn't depend on old pref names)
            case "setBarColors":
                String sb = args.isNull(0) ? null : args.optString(0, null);
                String nb = args.isNull(1) ? null : args.optString(1, null);
                String db = args.isNull(2) ? null : args.optString(2, null);
                final Window w = cordova.getActivity().getWindow();
                cordova.getActivity().runOnUiThread(() -> applyColors(w, sb, nb, db));
                cb.success(); return true;
        }
        return false;
    }
}
