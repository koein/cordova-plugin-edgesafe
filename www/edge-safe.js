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

  setMode: function (mode /* 'fit' | 'edge' */) {
    exec(function () {}, function () {}, 'EdgeSafe', 'setMode', [mode || 'fit']);
  }
};

module.exports = EdgeSafe;
