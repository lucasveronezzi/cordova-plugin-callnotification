var exec = require('cordova/exec');

exports.onActions = function (success, error) {
    exec(success, error, 'CallNotification', 'onActions', []);
};

exports.removeFromLockScreen = function (success, error) {
    exec(success, error, 'CallNotification', 'removeFromLockScreen', []);
};

exports.showNotification = function (options, success, error) {
    exec(success, error, 'CallNotification', 'showNotification', [options]);
};
