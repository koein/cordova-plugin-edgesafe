# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView. **Default = `fit`** (below bars).  
**EDGE mode now applies layout MARGINS equal to system bar insets**, so even `position:fixed` headers/footers stay inside the safe area without any app CSS changes.

## Install
```bash
cordova plugin add https://github.com/<you>/cordova-plugin-edgesafe.git
# or via config.xml:
# <plugin name="cordova-plugin-edgesafe" spec="https://github.com/<you>/cordova-plugin-edgesafe.git" />
```

## Configure (optional)
```xml
<preference name="EdgeSafeMode" value="fit" />   <!-- or 'edge' -->
<preference name="EdgeSafeTransparentBars" value="false" />
<preference name="EdgeSafeForceOpaqueBars" value="false" />
```

## Runtime
```js
document.addEventListener('deviceready', function () {
  // FIT (default): no CSS changes required
  cordova.plugins.edgeSafe.setMode('fit');

  // EDGE (content under bars, but WebView shrinks using margins)
  // cordova.plugins.edgeSafe.setMode('edge');
});
```

### Notes
- Keep `<preference name="fullScreen" value="false" />` for Android.
- With `fit`, leave `StatusBarOverlaysWebView` **false** (or omit).
- Keyboard uses `adjustResize` at runtime.
