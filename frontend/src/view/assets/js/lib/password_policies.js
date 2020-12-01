/*doc
---
title: PasswordPolicies
name: passwordPolicies
category: Javascripts - Password Policies
---

Password policies are rules for checking validity of new user / supervisor password.
*/

(function(){

	var PasswordPolicies = {};
	
	var policy_LENGTH_8_MIXED = function(password) {
		var errors = [];
		
		if(password.length < 8) {
			errors.push('password.error.too_short');
		} 

		if (!password.match("[a-z]")) {
			errors.push('password.error.no_lower_case');
	    }

	    if (!password.match("[A-Z]")) {
			errors.push('password.error.no_upper_case');
	    }
	    
	    if (!password.match("[0-9]")) {
			errors.push('password.error.no_digits');
	    }
	    
	    if (!password.match("[^a-zA-Z0-9]")) {
			errors.push('password.error.no_special');
	    }
		
		return errors;
	}; 
	
	var policy_LENGTH_12_SIMPLE = function(password) {
		if(password.length < 12) {
			return ['password.error.too_short'];
		} else {
			return null;
		}
	}; 
	
	var policy_WEBSERVICE = function(password) {
		if(password.length < 32) {
			return ['password.error.too_short'];
		} else {
			return null;
		}
	};
	
	var policy_FALLBACK = function(password) {
		return ['password.error.generic_error'];
	};
	
	var policy_map = [];
	
	// Register all policies (except "FALLBACK")
	register_policy("LENGTH_8_MIXED", policy_LENGTH_8_MIXED);
	register_policy("LENGTH_12_SIMPLE", policy_LENGTH_12_SIMPLE);
	register_policy("WEBSERVICE", policy_WEBSERVICE);

	// Internal function
	function register_policy(name, policy_fn) {
		policy_map.push({name: name, fn: policy_fn});
	}
	
	// Returns the policy function associated with given name
	PasswordPolicies.find_password_policy = function(name) {
		for(var i = 0; i < policy_map.length; i++) {
			if(policy_map[i].name == name) {
				return policy_map[i].fn;
			}
		}
		
		return policy_FALLBACK;	// Return FALLBACK function
	};
	
	AGN.Lib.PasswordPolicies = PasswordPolicies;
})();
