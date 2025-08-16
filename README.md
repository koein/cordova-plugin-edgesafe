# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView. **Default mode = `fit`** (WebView below status bar, above nav bar).
Switch to `edge` for true edge-to-edge + padding via WindowInsets.

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
  // cordova.plugins.edgeSafe.watch(); // to keep CSS vars updated
});
```

If you use `edge`, optional CSS:
```css
.app {
  padding-top: var(--safe-top, 0);
  padding-right: var(--safe-right, 0);
  padding-bottom: var(--safe-bottom, 0);
  padding-left: var(--safe-left, 0);
}
```

## Notes
- Keep `<preference name="fullScreen" value="false" />` for Android.
- With `fit` mode, leave `StatusBarOverlaysWebView` **false** (or omit it).
- Keyboard is handled (`adjustResize`) at runtime.
