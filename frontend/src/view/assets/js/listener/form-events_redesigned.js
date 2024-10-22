/*doc
---
title: Forms
name: form-directives
category: Javascripts - Forms
---

*/

/*doc
---
title: Text Inputs
name: form-inputs
parent: form-directives
---

It's very common behavior if a form gets submitted when user presses enter key. Inputs having `[type="text"]`, `[type="password"]` or `[type="number"]` behave like that.

Sometimes a form itself is not needed since request is performed as custom ajax call or UI provide some client-side logic. In this case you can handle a custom `enterdown` event emitted by input element:

```htmlexample
<div>
    <label class="form-label">Type anything and press enter key</label>
    <input type="text" id="myInput" class="form-control"/>
</div>
```

```js_example
$('#myInput').on('enterdown', function() {
  AGN.Lib.Messages('This is yours!', $(this).val(), 'success');
});
```

*/

/*doc
---
title: Form Type
name: form-directives-01
parent: form-directives
---

The `data-form` attribute is used to specify the type of the form which should decorate the html form. It should only be set on the form tag itself.

data-form | Form | Ajax | Description
----------|------|------|------------
not set | AGN.Lib.Form | yes | default, will send the data via an ajax request and replace its content with the content of form which is found in the servers response
`search` | AGN.Lib.SearchForm | yes | similar to AGN.Lib.Form, but the replaceable area is defined via the `data-form-content` attribute, to be used when only the search results should be replaced
`resource` | AGN.Lib.ResourceForm | yes | similar to AGN.Lib.Form, but will replace either the whole page content or a replaceable area (specified by `data-resource-selector` attribute placed on form element) - should be used if changes could occur outside of the the form (e.g. header, navigation)
`static` | AGN.Lib.StaticForm | no | disabled ajax submit
`polling` | AGN.Lib.PollingForm | yes | intermediary form, used for polling the server until the rendering of a result table is finished
`loading` | AGN.Lib.LoadingForm | yes | intermediary form, similar to the polling form but it will replace the content on each polling step (useful in case the server returns a loading bar)
*/

/*doc
---
title: Form Listener
name: form-directives-02
parent: form-directives
---

Directive | Description
----------|------------
`data-action="actionName"` | Makes the form behave on inputs enter click like if it was triggered with `data-form-submit-event` directive
`data-form-change` | Will set the value for `numberOfRowsChanged` to true
`data-form-set="field: value, otherField: value"` | Sets values on the form for the next submit, values are kept in an internal storage which is cleared after each submit, values are <b>not</b> shown in their corresponding inputs
`data-form-persist="field: value, otherField: value"` | Sets values on the form for all subsequent request or until the value is overwritten by the servers response, values are shown in their corresponding inputs
`data-form-url="new-url"` | Sets an `action` form attribute for the next submit, the new value is stored in js object so the actual form element is untouched
`data-form-method="new-method"` | Sets an `method` form attribute for the next submit, the new value is stored in js object so the actual form element is untouched
`data-form-resource="new-resource-selector"` | Only applicable for resource forms (`data-form="resource"`). Sets a `resource-selector` for the next submit
`data-form-reset` | Resets the form to its original state
`data-form-abort` | Aborts the current request to the server (if any is being performed at the moment)
`data-form-submit` | Submits the form
`data-form-submit-event` | Triggers `submission` javascript event on the form (if validation succeeds) element instead of an actual form submission. Allows to implement asynchronous form validation (like sending an ajax request that validates some input).
`data-form-submit-static` | Submits the form without using ajax
`data-form-confirm` | Submits the form to the server and expects the server to return html for a confirmation modal.

If the trigger for the directives cannot be placed inside the form, e.g. in case of a submit button in the header, you can use the `data-form-target="jquerySelectorForForm"` to bind the trigger to a form.
*/

(() => {

  const Action = AGN.Lib.Action,
    Form = AGN.Lib.Form,
    Confirm = AGN.Lib.Confirm,
    Helpers = AGN.Lib.Helpers;


  Action.new({click: '[data-form-change]', change: 'select[data-form-change]'}, function() {
    Form.get(this.el).setValueOnce('numberOfRowsChanged', true);
  });

  // set values on the form (once)
  Action.new({
    click:  'a[data-form-set], button[data-form-set]',
    change: 'select[data-form-set], input[data-form-set]'
  }, function() {
    const opts  = Helpers.objFromString(this.el.data('form-set'));
    const form  = Form.get(this.el);

    _.forEach(opts, (value, field) => form.setValueOnce(field, value));
  });

  Action.new({
    click:  'a[data-form-url], button[data-form-url]',
    change: 'select[data-form-url], input[data-form-url]'
  }, function() {
    Form.get(this.el).setActionOnce(this.el.data('form-url'));
  });

  Action.new({
    click:  'a[data-form-method], button[data-form-method]',
    change: 'select[data-form-method], input[data-form-method]'
  }, function() {
    Form.get(this.el).setMethodOnce(this.el.data('form-method'));
  });

  Action.new({
    click:  'a[data-form-resource], button[data-form-resource]',
    change: 'select[data-form-resource], input[data-form-resource]'
  }, function() {
    const form = Form.get(this.el);

    if (form instanceof AGN.Lib.ResourceForm) {
      form.setResourceSelectorOnce(this.el.data('form-resource'));
    }
  });

  Action.new({click:  'a[data-form-clear], button[data-form-clear]'}, function() {
    const container = this.el.data('form-clear');
    if (!container) {
      Form.get(this.el).clear();
      return;
    }
    clearAllInputs($(container));
    this.el.trigger('apply-filter');
  });

  function clearAllInputs($container) {
    $container.find(":input, select").val("").trigger('change');
    $container.find(":checkbox, :radio").prop("checked", false);
    AGN.Lib.CoreInitializer.run('select', $container);
  }

  // set values on the form (always)
  Action.new({
    click:  'a[data-form-persist], button[data-form-persist]',
    change: 'select[data-form-persist], input[data-form-persist]'
  }, function() {
    const opts  = Helpers.objFromString(this.el.data('form-persist'));
    const form  = Form.get(this.el);

    _.forEach(opts, (value, field) => form.setValue(field, value));
  });

  // reset a form
  Action.new({click:  'a[data-form-reset], button[data-form-reset]'}, function() {
    Form.get(this.el).reset()
  });

  // abort a request
  Action.new({click:  'a[data-form-abort], button[data-form-abort]'}, function() {
    Form.get(this.el).abort();
  });

  // submit an action (e.g. bulk-delete)
  Action.new({click: '[data-form-confirm]'}, function() {
    const form = Form.get(this.el);

    form.submit('confirm').done(resp => {
      const $modal = $(resp).all('.modal');
      if (!$modal.exists()) {
        form.updateHtml(resp);
        return;
      }

      // create a confirm
      // if the user confirms the action
      // the html returned by the confirmation
      // request is passed to the form
      Confirm.create(resp)
        .done(resp => form.updateHtml(resp))
        .fail(form.revertAction);
    });
  });

  // submit the form
  Action.new({
    click:  'a[data-form-submit], button[data-form-submit]',
    change: 'select[data-form-submit], input[data-form-submit]'
  }, function() {
    // delay submit of form to wait for
    // other events to finish
    setTimeout(() => Form.get(this.el).submit(), 5);
  });

  Action.new({
    click:  'a[data-form-submit-static], button[data-form-submit-static]',
    change: 'select[data-form-submit-static], input[data-form-submit-static]'
  }, function() {
    // delay submit of form to wait for
    // other events to finish
    setTimeout(() => Form.get(this.el).submit('static'), 5);
  });

  Action.new({
    click:  'a[data-form-submit-event], button[data-form-submit-event]',
    change: 'select[data-form-submit-event], input[data-form-submit-event]'
  }, function() {
    // delay submit of form to wait for
    // other events to finish
    setTimeout(() => Form.get(this.el).submit('event'), 5);
  });

  Action.new({submit: '[data-form]'}, function() {
    if ( this.el.data('form') !== 'static' ) {
      this.event.preventDefault();

      // delay submit of form to wait for
      // other events to finish
      setTimeout(() => Form.get(this.el).submit(), 5);
    }
  });

  function isEnterClickEvent(event) {
    const code = event.keyCode || event.which;
    if (code === 13) {
      return true;
    }

    if (code === 229 && event.originalEvent) {
      const name = event.originalEvent.code;
      if (name === 'Enter' || name === 'NumpadEnter') {
        return true;
      }
    }

    return false;
  }

  Action.new({keydown: 'input[type="text"], input[type="password"], input[type="number"]'}, function() {
    if (isEnterClickEvent(this.event)) {
      const event = $.Event('enterdown');
      this.el.trigger(event);

      if (event.isDefaultPrevented()) {
        return;
      }

      const $form = Form.getWrapper(this.el);

      if ($form.exists()) {
        if ($form.is('[data-form]')) {
          const form = Form.get($form);

          if ($form.is('[data-action]')) {
            form.submit('event');
          } else {
            form.submit();
          }
        } else {
          $form.submit();
        }
      }
    }
  });

})();
