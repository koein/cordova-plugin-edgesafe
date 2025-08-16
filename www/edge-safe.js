var exec = require('cordova/exec');

var EdgeSafe = {
  watch: function (callback) {
    exec(function (insets) {
      try {
        var r = document.documentElement && document.documentElement.style;
        if (r) {
          r.setProperty('--safe-top',    (insets.top    || 0) + 'px');
          r.setProperty('--safe-right',  (insets.right  || 0) + 'px');
          r.setProperty('--safe-bottom', (insets.bottom || 0) + 'px');
          r.setProperty('--safe-left',   (insets.left   || 0) + 'px');
        }
      } catch (e) {}
      if (typeof callback === 'function') callback(insets);
    }, function () {}, 'EdgeSafe', 'watch', []);
  },

  getInsets: function (callback) {
    exec(function (insets) {
      if (typeof callback === 'function') callback(insets);
    }, function () {}, 'EdgeSafe', 'getInsets', []);
  },

  setMode: function (mode /* 'edge' | 'fit' */) {
    exec(function () {}, function () {}, 'EdgeSafe', 'setMode', [mode || 'edge']);
  },

  // Individual runtime controls (no 'set' prefix)
  StatusBarColor: function (color) {
    exec(function () {}, function () {}, 'EdgeSafe', 'statusBarColor', [color]);
  },
  NavBarColor: function (color) {
    exec(function () {}, function () {}, 'EdgeSafe', 'navBarColor', [color]);
  },
  NavBarDividerColor: function (color) {
    exec(function () {}, function () {}, 'EdgeSafe', 'navBarDividerColor', [color]);
  },
  LightStatusBarIcons: function (on) {
    exec(function () {}, function () {}, 'EdgeSafe', 'lightStatusIcons', [!!on]);
  },
  LightNavBarIcons: function (on) {
    exec(function () {}, function () {}, 'EdgeSafe', 'lightNavIcons', [!!on]);
  },

  // Back-compat combined setter
  setBarColors: function (statusBar, navBar, navDivider) {
    exec(function () {}, function () {}, 'EdgeSafe', 'setBarColors', [statusBar, navBar, navDivider]);
  },

  // Advanced (API 29+): explicitly toggle contrast enforcement (Samsung / One UI)
  StatusBarContrastEnforced: function (on) {
    exec(function () {}, function () {}, 'EdgeSafe', 'statusBarContrastEnforced', [!!on]);
  },
  NavBarContrastEnforced: function (on) {
    exec(function () {}, function () {}, 'EdgeSafe', 'navBarContrastEnforced', [!!on]);
  }
};

module.exports = EdgeSafe;
