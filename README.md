# cordova-plugin-edgesafe
Android edge-to-edge safety for Cordova WebView. **Default mode = edge** (viewport avoids bars & keyboard).  
**No CSS updates needed**

## Config.xml settings (optional)
```xml
<preference name="StatusBarColor" value="#000000" />
<preference name="NavBarColor" value="#000000" />
<preference name="NavBarDividerColor" value="#1F000000" />
<preference name="LightStatusBarIcons" value="true" />
<preference name="LightNavBarIcons" value="true" />
```

## Runtime
```js
cordova.plugins.edgeSafe.StatusBarColor('#000000');
cordova.plugins.edgeSafe.NavBarColor('#000000');
cordova.plugins.edgeSafe.NavBarDividerColor('#1F000000');
cordova.plugins.edgeSafe.LightStatusBarIcons(true);
cordova.plugins.edgeSafe.LightNavBarIcons(true);
```

## Mode switch to re-enable edgeToEdge
```js
cordova.plugins.edgeSafe.setMode('fit');
```

Notes: 
- Color values: '#RRGGBB', '#AARRGGBB', 'transparent', or 'auto'.
- Keep Android <preference name="fullScreen" value="false" />.
- On Samsung / One UI (API 29+), the system may enforce contrast and draw a grey scrim. This plugin disables that when you set non-transparent colors, and you can explicitly control it:
  ```js
  cordova.plugins.edgeSafe.StatusBarContrastEnforced(false);
  cordova.plugins.edgeSafe.NavBarContrastEnforced(false);
  ```
