# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView.  
**Default mode = `edge`** and the plugin applies **IME-aware margins** so the viewport avoids the status bar, nav bar, **and the on-screen keyboard**. No app CSS changes required.

## Install
```bash
cordova plugin add https://github.com/<you>/cordova-plugin-edgesafe.git
# or via config.xml:
# <plugin name="cordova-plugin-edgesafe" spec="https://github.com/<you>/cordova-plugin-edgesafe.git" />
```

## Configure (optional)
```xml
<preference name="EdgeSafeMode" value="edge" />   <!-- or 'fit' -->
<preference name="EdgeSafeTransparentBars" value="true" />
<preference name="EdgeSafeForceOpaqueBars" value="false" />
```

## Runtime
```js
document.addEventListener('deviceready', function () {
  // Default is edge (no call required). To switch:
  // cordova.plugins.edgeSafe.setMode('fit');
});
```

### Notes
- Keep `<preference name="fullScreen" value="false" />` for Android.
- Remove any conflicting immersive/fullscreen calls from other plugins or set them after deviceready then re-call `setMode('edge')` to re-assert.
- Keyboard resizing is handled via IME insets + margins (no CSS).

