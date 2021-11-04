AGN_FACEBOOK.Login = {};


AGN_FACEBOOK.Login.attach_login_function = function (selector, callback, scope) {
  var $loginButtons = $(selector);

  $loginButtons.on('click', function() {
    FB.login(
      function (response) {
        callback(response.authResponse.accessToken);
      },
      {
        scope: scope
      }
    )
  })
};

AGN_FACEBOOK.Login.doByLoginStatus = function (onLoggedIn, onNotLoggedIn) {
  var cb = function (response) {
    if (response.status === 'connected') {
      if (_.isFunction(onLoggedIn)) {
        onLoggedIn(response.authResponse.accessToken);
      }
    } else {
      if (_.isFunction(onLoggedIn)) {
        onNotLoggedIn();
      }
    }
  };

    FB.getLoginStatus(cb, true);
};
