/*doc
---
title: Password Policies
name: password-policies
category: Javascripts - Password Policies
---

Password policies are rules for checking validity of password.

It is used in pair with `[data-field="password"]`. The policy name is set via the `[data-rule]` attribute.

The following policy names exist: `LENGTH_8_MIXED`, `LENGTH_12_SIMPLE`, `WEBSERVICE`.

You can also register and use custom policy:

```htmlexample
<script type="text/javascript">
  AGN.Lib.PasswordPolicies.registerPolicy('LENGTH_24_CUSTOM', password => {
      return password.length < 24 ? ['Password length must be >= 24 characters'] : null;
  });
</script>

<form class="d-flex flex-column gap-2">
    <div class="row" data-field="password">
      <div class="col-6">
          <label class="form-label" for="password">Password</label>
          <input id="password" type="password" class="form-control js-password-strength" size="52" data-rule="LENGTH_24_CUSTOM" />
      </div>
      <div class="col-6">
          <label class="form-label" for="passwordRepeat">Repeat</label>
          <input id="passwordRepeat" type="password" class="form-control js-password-match" size="52" readonly />
      </div>
    </div>

  <a href="#" class="btn btn-primary" data-form-submit="">Submit</a>
</form>
```

*/

(function () {

  class PasswordPolicies {

    static policyMap = new Map();

    // Returns the policy function associated with given name
    static findPasswordPolicy(name) {
      if (PasswordPolicies.policyMap.has(name)) {
        return PasswordPolicies.policyMap.get(name);
      }

      return ['password.error.generic_error'];
    }

    static registerPolicy(name, policyFn) {
      PasswordPolicies.policyMap.set(name, policyFn);
    }
  }

  // Register all policies
  PasswordPolicies.registerPolicy("LENGTH_8_MIXED", password => {
    const errors = [];

    if (password.length < 8) {
      errors.push('password.error.too_short_min8');
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
  });

  PasswordPolicies.registerPolicy("LENGTH_12_SIMPLE", password => {
    return password.length < 12 ? ['password.error.too_short_min12'] : null;
  });

  PasswordPolicies.registerPolicy("WEBSERVICE", password => {
    return password.length < 32 ? ['password.error.too_short_min32'] : null;
  });


  AGN.Lib.PasswordPolicies = PasswordPolicies;

})();
