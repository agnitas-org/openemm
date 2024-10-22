/*doc
---
title: Password Field
name: fields-04-password
parent: fields
---

Is a combination between a password entry field and a password confirmation field.

For the first field you need to add the `.js-password-strength` class. For second one - `.js-password-match`.

Also, for the main password field, you need to specify password police name via `data-rule` attribute.
List of all policies can be found [here](/javascripts_-_password_policies.html).

```htmlexample
<form class="d-flex flex-column gap-2">
    <div class="row" data-field="password">
      <div class="col-6">
          <label class="form-label" for="password">Password</label>
          <input id="password" type="password" class="form-control js-password-strength" size="52" data-rule="LENGTH_8_MIXED" />
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

(() => {

  class PasswordField extends AGN.Lib.Field {
    constructor($field) {
      super($field);

      this.$strength = this.el.find('.js-password-strength');
      this.$match = this.el.parent().find('.js-password-match');

      this.$strength.on('keyup', event => {
        if (event.keyCode !== 13) {
          if (!this.safe() || this.$strength.val().length === 0) {
            this.$match.val('');
          }

          this.updateStrength();
          this.updateMatch();
        }
      });

      this.$match.on('keyup', () => this.updateMatch())
    }

    match() {
      return this.$strength.val() === this.$match.val();
    }

    safe() {
      return this.#getSecurityIssues().length === 0;
    }

    valid() {
      if (!this.$match.exists()) {
        return true;
      }
      return this.match() && this.safe();
    }

    errors() {
      const errors = [];
      if (this.valid()) {
        return errors;
      }

      if (!this.match()) {
        errors.push({
          field: this.$match,
          msg: t('fields.password.errors.notMatching')
        });
      }

      const securityErrors = this.#getSecurityIssues();
      if (securityErrors.length > 0) {
        securityErrors.forEach(el => {
          errors.push({
            field: this.$strength,
            msg: el
          });
        });
      }

      return errors;
    }

    resetStrength() {
      const form = AGN.Lib.Form.get(this.$strength);
      form.cleanFieldFeedback(this.$strength);
    }

    resetMatch() {
      const form = AGN.Lib.Form.get(this.$match);
      form.cleanFieldFeedback(this.$match);
    }

    updateStrength() {
      this.resetStrength();

      if (this.$strength.val().length === 0) {
        return;
      }

      const form = AGN.Lib.Form.get(this.$strength);
      const errors = this.#getSecurityIssues();

      if (errors.length === 0) {
        form.markField(this.$strength, 'success');
        AGN.Lib.Form.appendFeedbackMessage(this.$strength, t('fields.password.safe'), 'success');
      } else {
        form.markField(this.$strength);
        errors.forEach(e => AGN.Lib.Form.appendFeedbackMessage(this.$strength, e));
      }
    }

    updateMatch() {
      this.resetMatch();
      this.$match.get(0).toggleAttribute('readonly', !this.safe() || this.$strength.val().length === 0);

      if (this.$match.val().length === 0) {
        return;
      }

      const form = AGN.Lib.Form.get(this.$match);
      if (this.match()) {
        form.markField(this.$match, 'success');
        AGN.Lib.Form.appendFeedbackMessage(this.$match, t('fields.password.matches'), 'success');
      } else {
        form.markField(this.$match);
        AGN.Lib.Form.appendFeedbackMessage(this.$match, t('fields.password.matchesNot'));
      }
    }

    #getSecurityIssues() {
      const errors = [];
      const pass = this.$strength.val();

      if (pass.length === 0) {
        return errors;
      }

      // Decide, which rules to use
      const ruleName = this.$strength.attr("data-rule"); // Do not use data('rule') here, because the value is read once and cached
      const policy = AGN.Lib.PasswordPolicies.findPasswordPolicy(ruleName);
      const errorKeys = policy(pass);

      if (errorKeys && errorKeys.length) {
        errorKeys.forEach(key => errors.push(t(key)));
      }

      return errors;
    }
  }

  AGN.Lib.PasswordField = PasswordField;
  AGN.Opt.Fields['password'] = PasswordField;
})();
