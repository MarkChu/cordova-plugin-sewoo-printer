var exec = require('cordova/exec');

exports.Send = function (target, sendcmd , success, error) {
    cordova.exec(success, error, "sewoo", "Send", [target, sendcmd]);
};
