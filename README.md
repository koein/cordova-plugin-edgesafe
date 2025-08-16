# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView (SDK 35+). Applies WindowInsets as WebView padding, **or** switch to legacy *fit* mode to keep content below the system bars (no CSS changes).

## Install
```bash
cordova plugin add https://github.com/<you>/cordova-plugin-edgesafe.git
# or via config.xml
# <plugin name="cordova-plugin-edgesafe" spec="https://github.com/<you>/cordova-plugin-edgesafe.git" />
```

## Modes
- **edge** (default): Edge-to-edge with proper padding based on insets.
- **fit**: Legacy layout. Content starts **below** the status bar and ends **above** the navigation bar. No per-element CSS needed.

## Use
```html
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
```
```js
document.addEventListener('deviceready', function () {
  if (cordova?.plugins?.edgeSafe) {
    // Choose ONE:
    // 1) Edge-to-edge (default)
    cordova.plugins.edgeSafe.watch();

    // 2) Legacy fit mode (content below bars) – no CSS needed
    // cordova.plugins.edgeSafe.setMode('fit');
  }
});
```

## Config (optional)
Override in your app `config.xml`:
```xml
<preference name="EdgeSafeMode" value="edge" /> <!-- or 'fit' -->
<preference name="EdgeSafeApplyPadding" value="true" />
<preference name="EdgeSafePadTop" value="true" />
<preference name="EdgeSafePadBottom" value="true" />
<preference name="EdgeSafePadSides" value="true" />
<preference name="EdgeSafeTransparentBars" value="true" />
<preference name="EdgeSafeLightStatusBarIcons" value="true" />
<preference name="EdgeSafeLightNavBarIcons" value="true" />
```

## Notes
- In **fit** mode the plugin calls `WindowCompat.setDecorFitsSystemWindows(window, true)` so the WebView is laid out below system bars.
- In **edge** mode the plugin calls `WindowCompat.setDecorFitsSystemWindows(window, false)` and pads the WebView using `WindowInsets`.
- Keep `<preference name="fullScreen" value="false" />` on Android.
