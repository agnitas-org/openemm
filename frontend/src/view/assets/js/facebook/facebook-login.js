AGN_FACEBOOK.Login = {};


AGN_FACEBOOK.Login.attach_login_function = function(selector, callback, scope) {
	var elements = $(selector);
	
	_.each(elements, function(element) {
		element.onclick = function() { 
			FB.login(
					function(response) {
						callback(response.authResponse.accessToken);
					}) 
			};
	},
	{scope: scope});
}

AGN_FACEBOOK.Login.do_by_login_state = function(onLoggedIn, onNotLoggedIn) {
	FB.getLoginStatus(
			function(response) {
				if(response.status === 'connected') {
					onLoggedIn(response.authResponse.accessToken);
				} else {
					onNotLoggedIn();
				}
			}
	);
	
}


AGN_FACEBOOK.Login.do_if_logged_in = function(handler) {
	FB.getLoginStatus(
			function(response) {
				if(response.status === 'connected') {
					handler(response.authResponse.accessToken);
				}
			}
	);
}

AGN_FACEBOOK.Login.do_if_not_logged_in = function(handler) {
	FB.getLoginStatus(
			function(response) {
				if(response.status != 'connected') {
					handler();
				}
			}
	);
}

AGN_FACEBOOK.Login.show_login_button_if_needed = function(selector) {
	FB.getLoginStatus(
			function(response) {
				if(response.status != 'connected') {
					var elements = $(selector);
					
					elements.show();
				}
			}
	);
}
