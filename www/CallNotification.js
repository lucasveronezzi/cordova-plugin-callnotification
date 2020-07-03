var exec = require('cordova/exec');

exports.onActions = function (success, error) {
    exec(success, error, 'CallNotification', 'onActions', []);
};
