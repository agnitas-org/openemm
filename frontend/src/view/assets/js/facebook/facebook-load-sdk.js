AGN_FACEBOOK = {};
AGN_FACEBOOK.SDK = {};

AGN_FACEBOOK.SDK.load_sdk = function (appId, afterInitCallback) {

	window.fbAsyncInit = function() {
		FB.init({
			appId      : appId,
			xfbml      : true,
			version    : 'v3.3'
		});
		
		afterInitCallback();
	};
	
	
	(function(d, s, id) {
		var js, fjs = d.getElementsByTagName(s)[0];
		
		if (d.getElementById(id)) {
			return;
		}
	    
		js = d.createElement(s); 
		js.id = id;
		js.src = "https://connect.facebook.net/en_US/sdk.js";
		fjs.parentNode.insertBefore(js, fjs);
	} (document, 'script', 'facebook-jssdk'));
 
}
