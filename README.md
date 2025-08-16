# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView (SDK 35+). Applies WindowInsets as WebView padding and streams inset values to JS.

## Install
```bash
cordova plugin add https://github.com/<you>/cordova-plugin-edgesafe.git
# or via config.xml
# <plugin name="cordova-plugin-edgesafe" spec="https://github.com/<you>/cordova-plugin-edgesafe.git" />
```

## Use
Add to your `<head>`:
```html
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
```

Optional CSS using the variables the plugin sets:
```css
.app {
  padding-top: var(--safe-top, 0);
  padding-right: var(--safe-right, 0);
  padding-bottom: var(--safe-bottom, 0);
  padding-left: var(--safe-left, 0);
}
```

Start watching on `deviceready`:
```js
document.addEventListener('deviceready', function () {
  if (cordova?.plugins?.edgeSafe) {
    cordova.plugins.edgeSafe.watch(); // sets CSS vars and streams updates
  }
});
```

## Config (optional)
Override in your app `config.xml`:
```xml
<preference name="EdgeSafeApplyPadding" value="true" />
<preference name="EdgeSafePadTop" value="true" />
<preference name="EdgeSafePadBottom" value="true" />
<preference name="EdgeSafePadSides" value="true" />
<preference name="EdgeSafeTransparentBars" value="true" />
<preference name="EdgeSafeLightStatusBarIcons" value="true" />
<preference name="EdgeSafeLightNavBarIcons" value="true" />
```

## Notes
- Do **not** set `<preference name="StatusBarOverlaysWebView" value="true" />` with this plugin.
- Keep `<preference name="fullScreen" value="false" />` for Android.
- The plugin enforces `adjustResize` so the keyboard behaves correctly.
