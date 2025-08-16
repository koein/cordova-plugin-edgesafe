# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView. **Default mode = `fit`** (WebView below status bar, above nav bar).
`fit` now clears system UI overlay flags that other plugins might set (e.g., statusbar immersive). Switch to `edge` for true edge-to-edge + padding via WindowInsets.

## Install
```bash
cordova plugin add https://github.com/<you>/cordova-plugin-edgesafe.git
# or via config.xml:
# <plugin name="cordova-plugin-edgesafe" spec="https://github.com/<you>/cordova-plugin-edgesafe.git" />
```

## Configure (optional)
Default is `fit`. Override in `config.xml`:
```xml
<preference name="EdgeSafeMode" value="fit" />   <!-- or 'edge' -->
<preference name="EdgeSafeTransparentBars" value="false" />
<preference name="EdgeSafeForceOpaqueBars" value="false" />
```
When using `edge`, you can also tweak:
```xml
<preference name="EdgeSafeApplyPadding" value="true"/>
<preference name="EdgeSafePadTop" value="true"/>
<preference name="EdgeSafePadBottom" value="true"/>
<preference name="EdgeSafePadSides" value="true"/>
```

## Runtime API
```js
document.addEventListener('deviceready', function () {
  // FIT (default): no CSS changes required
  cordova.plugins.edgeSafe.setMode('fit');

  // Or go EDGE (translucent bars + insets)
  // cordova.plugins.edgeSafe.setMode('edge');
  // cordova.plugins.edgeSafe.watch(); // to keep --safe-* CSS vars updated
});
```

### Notes
- Keep `<preference name="fullScreen" value="false" />` for Android.
- With `fit` mode, leave `StatusBarOverlaysWebView` **false** (or omit it).
- Keyboard is handled (`adjustResize`) at runtime.
