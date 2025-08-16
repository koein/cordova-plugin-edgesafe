# cordova-plugin-edgesafe

Android edge-to-edge safety for Cordova WebView.  
**Default mode = `edge`** (IME-aware margins so viewport avoids bars & keyboard).  
**Unprefixed-only config** + **simple runtime API (no 'set' prefix)**.
No CSS updates needed

## Config.xml settings (optional)
```xml
<preference name="StatusBarColor" value="#000000" />
<preference name="NavBarColor" value="#000000" />
<preference name="NavBarDividerColor" value="#1F000000" />
<preference name="LightStatusBarIcons" value="true" />
<preference name="LightNavBarIcons" value="true" />

## Runtime
```js
cordova.plugins.edgeSafe.StatusBarColor('#000000');
cordova.plugins.edgeSafe.NavBarColor('#000000');
cordova.plugins.edgeSafe.NavBarDividerColor('#1F000000');
cordova.plugins.edgeSafe.LightStatusBarIcons(true);
cordova.plugins.edgeSafe.LightNavBarIcons(true);

## Mode switch to re-enable edgeToEdge
cordova.plugins.edgeSafe.setMode('fit');

Notes:
- Color values: `'#RRGGBB'`, `'#AARRGGBB'`, `'transparent'`, or `'auto'`.
- Keep Android `<preference name="fullScreen" value="false" />`.
