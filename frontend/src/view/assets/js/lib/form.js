/*doc
---
title: Form
name: form-features
category: Javascripts - Forms
---
*/

/*doc
---
title: Custom Loader
name: form-features-01
parent: form-features
---

The `data-custom-loader` attribute makes (if specified) form trigger `form:loadershow` and `form:loaderhide` events (on form element)
when loader is expected to be shown/hidden so listening those events you can provide custom loading indication.

Also it enables `form:progress` event that is being thrown on form uploading progress.

```htmlexample
<form action="#form-features-01" id="customLoaderDemoForm" data-form="" data-custom-loader="">
  <button type="submit" class="btn btn-regular btn-primary">Submit</button>
</form>
```

```jsexample
$('#customLoaderDemoForm').on({
  'form:loadershow': function() {
    AGN.Lib.Messages('Form', 'Form submission started!', 'success');
  },
  'form:loaderhide': function() {
    AGN.Lib.Messages('Form', 'Form submission finished!', 'success');
  }
});
```

The `form:progress` event depends on browser so it's not quite reliable and may be missing. When fired it's supplied with the following data:

Property | Description
---------|------------
`loaded` | Number of bytes already loaded (only available if `progress !== true`).
`total` | Total number of bytes to be loaded (only available if `progress !== true`).
`progress` | Formatted progress percentage number or `true` (if detailed progress percentage is not supported).
*/

/*doc
---
title: Controls disabling
name: form-features-02
parent: form-features
---

The `[data-disable-controls]` attribute allows to disable some controls (the ones having `[data-controls-group]` attribute) during form submission (and enable them back afterwards).
Use `[data-disable-controls="*"]` to refer all the elements having `[data-controls-group]` attribute or specify a particular value to refer a named group (group's name is defined by `[data-controls-group]` attribute).

```htmlexample
<form action="#form-features-02" id="controlsDisablingDemoForm" data-form="" data-disable-controls="group-1" class="form-column">
    <div>
        <label class="form-label" for="Name">First name</label>
        <input type="text" name="firstname" class="form-control" data-controls-group="group-1"/>
    </div>
    <div>
        <label class="form-label">Last name</label>
        <input type="text" name="lastname" class="form-control" data-controls-group="group-2"/>
    </div>
    <button type="submit" class="btn btn-regular btn-primary" data-controls-group="group-1">Submit</button>
</form>
```
*/

/*doc
---
title: Form Dirty Check
name: form-features-03
parent: form-features
---

Add `[data-form-dirty-checking]` to a form to enable automatic dirty tracking of all input, select, and textarea fields.
To exclude specific fields from being tracked, add `[data-dirty-ignore]` to those inputs;

```htmlexample
<form action="#form-features-03" data-form="" class="form-column" data-form-dirty-checking>
    <div>
        <label class="form-label" for="Name">First name</label>
        <input type="text" name="firstname" class="form-control" placeholder="Change and try to leave page"/>
    </div>
    <div>
        <label class="form-label">Last name</label>
        <input type="text" name="lastname" class="form-control" placeholder="Ignored by dirty check" data-dirty-ignore/>
    </div>
    <a href="javascripts_-_helpers.html" class="btn btn-regular btn-primary">Go to next docs page - Helpers</a>
</form>
```
*/

(() => {

  const Field = AGN.Lib.Field;
  const CSRF = AGN.Lib.CSRF;

  class ScrollHelper {
    static scrollTo($fields) {
      const points = this.#findMinScrollPoints($fields);
      const minPointData = this.#findMinScrollPoint(points);

      if (this.inHiddenTab($(_.last($fields)))) {
        this.showTab($(_.last($fields)));
      }
      points.forEach((pos, container) => this.scrollToPos($(container), pos)); // scroll containers

      // scroll page
      if (AGN.Lib.Helpers.isMobileView() && !minPointData?.container?.closest('.modal').exists()) {
        const $tilesContainer = $('.tiles-container, .filter-overview');
        $tilesContainer.animate({scrollTop: minPointData.pos - $tilesContainer.offset().top + $tilesContainer.scrollTop()});
      }
    }

    static scrollToPos($el, pos) {
      $el.animate({scrollTop: pos - $el.offset().top + $el.scrollTop()});
    }

    static inHiddenTab($el) {
      const $container = this.#findScrollableContainer$($el);
      return $container.hasClass('hidden') && this.#getTileHeader$($el).is('.navbar');
    }

    static showTab($el) {
      const $container = this.#findScrollableContainer$($el);
      AGN.Lib.Tab.show(this.#getTileHeader$($el).find(`[data-toggle-tab="#${$container.attr('id')}"]`));
    }

    static #findMinScrollPoints($fields) {
      const points = new Map();

      _.each($fields, field => {
        const $field = $(field);

        const scrollableParent = this.#findScrollableContainer$($field).get(0);
        const point = this.#getFieldContainer($field).offset().top;

        if (points.has(scrollableParent)) {
          if (points.get(scrollableParent) > point) {
            points.set(scrollableParent, point);
          }
        } else {
          points.set(scrollableParent, point);
        }
      });

      return points;
    }

    static #findScrollableContainer$($field) {
      const $tile = this.#getTile$($field);
      return $tile.exists() ? $field.closest('.tile-body') : $field.closest('.modal-body').first();
    }

    static #getTile$($el) {
      return $el.closest('.tiles-container > .tile, .filter-overview > .tile, .tiles-block > .tile');
    }

    static #getTileHeader$($el) {
      return this.#getTile$($el).find('> .tile-header');
    }

    static #getFieldContainer($field) {
      const editor = $field.data('_editor')?.editor;
      const $container = editor ? $(editor.container) : $field.closest('.has-feedback');
      return $container.exists() ? $container : $field.parent();
    }

    static #findMinScrollPoint(points) {
      let result;
      let minPos = Infinity;

      points.forEach((pos, container) => {
        if (pos < minPos) {
          minPos = pos;
          result = { container: $(container), pos: pos };
        }
      });

      return result;
    }
  }

  class ControlsToDisable {
    constructor(group) {
      this.group = group;
      this.$controls = $();
    }
  
    _collect(group) {
      const $all = $('[data-controls-group]');
  
      if (group === '*') {
        return $all;
      }
  
      if (group) {
        return $all.filter(function() {
          return group === $(this).attr('data-controls-group');
        });
      } else {
        return $();
      }
    }
  
    collect() {
      if (this.group) {
        this.$controls = this._collect(this.group).filter(function() {
          const $e = $(this);
          // Exclude disabled controls (otherwise they will be enabled when submission is complete).
          return !$e.prop('disabled') && !$e.hasClass('disabled');
        });
      }
    }
  
    setDisabled(isDisabled) {
      if (isDisabled === false) {
        this.$controls.removeClass('disabled');
        this.$controls.prop('disabled', false);
      } else {
        this.$controls.addClass('disabled');
        this.$controls.prop('disabled', true);
      }
    }
  }

  class Form {
    constructor($form) {
      this.$form = $form;
      this.form = $form[0];

      this._abort = null;
      this._data = {};
      this._dataNextRequest = {};
      this._isSubmitting = false;
      this.fields = [];
      this.validatorName = null;
      this.validatorOptions = null;
      this.dirtyChecking = $form.is('[data-form-dirty-checking]');
      this.editable = !$form.is('[data-editable]') || !!$form.data('editable');

      this.url = $form.attr('action');
      this.urlNextRequest = null;
      this.methodNextRequest = null;
      this.method = $form.attr('method');
      this.isMultipart = 'multipart/form-data' == ($form.attr('enctype') || '').toLowerCase();
      this.isCustomLoader = $form.is('[data-custom-loader]');
      this.controlsToDisable = new ControlsToDisable($form.attr('data-disable-controls'));

      if ($form.is('[data-custom-loader]')) {
        this.isCustomLoader = $form.attr('data-custom-loader') !== 'false';
      } else {
        this.isCustomLoader = false;
      }

      this.initFields();
      this.initValidator();

      this.#handleFieldsMessages();

      if (this.dirtyChecking) {
        this.#enableDirtyChecking($form);
      }

      if (!this.editable) {
        $form.find(':input').prop('disabled', true);
      }
    }

    #enableDirtyChecking($form) {
      $form.dirty({
        preventLeaving: true,
        leavingMessage: t('grid.layout.leaveQuestion')
      });

      window.onbeforeunload = () => {
        //prevent show loader if form is dirty
        if (!$form.dirty('isDirty') === true) {
          AGN.Lib.Loader.show();
        }
      };
    }

    // gets a singleton instance of the form object
    static get($needle) {
      const $form = Form.getWrapper($needle);
      let formObj = $form.data('_form');

      if (!formObj) {
        const formType = $form.data('form');
        const type = AGN.Opt.Forms[formType] || AGN.Lib.Form;
        formObj = new type($form);
        $form.data('_form', formObj);
      }

      return formObj;
    }

    // gets the jquery wrapped form tag
    static getWrapper($needle) {
      let $form = $($needle.data('form-target'));

      if (!$form.exists()) {
        if ($needle.attr('form')) {
          $form = $(`#${$needle.attr('form')}`)
        }

        if (!$form.exists()) {
          $form = $needle.closest('form');
        }
      }

      return $form;
    }

    valid(options) {
      const validationEvent = $.Event('validation');
      options = _.merge({}, this.validatorOptions, options);
      this.$form.trigger(validationEvent, options);

      if (validationEvent.isDefaultPrevented()) {
        return false;
      }

      const fieldsValid = _.every(this.fields, field => field.valid());

      if (fieldsValid && this.validatorName) {
        const validator = AGN.Lib.Validator.get(this.validatorName);
        if (validator) {
          return validator.valid(this, options, fieldsValid);
        }
      }

      return fieldsValid;
    }

    validate(options) {
      if (this.valid(options)) {
        this.cleanFieldFeedback();
        return true;
      } else {
        this.handleErrors();
        return false;
      }
    }

    validateField($field) {
      this.cleanFieldFeedback($field);

      const field = this.fields.find(f => f.isInitializedFor($field));
      if (field && !field.valid()) {
        this.#handleErrors(field.errors());
      }
    }

    get$() {
      return this.$form;
    }

    isEditable() {
      return this.editable;
    }

    getAction() {
      if (this.urlNextRequest) {
        const value = this.urlNextRequest;
        this.urlNextRequest = null;
        return value;
      }
      return this.url;
    }

    setActionOnce(url) {
      this.urlNextRequest = url;
      return this;
    }

    setMethodOnce(method) {
      this.methodNextRequest = method;
      return this;
    }

    getMethod() {
      if (this.methodNextRequest) {
        const value = this.methodNextRequest;
        this.methodNextRequest = null;
        return value;
      }

      return this.method;
    }

    errors() {
      let errors = this.fields.map(field => field.errors()).flat();

      if (this.validatorName) {
        const validator = AGN.Lib.Validator.get(this.validatorName);
        if (validator) {
          errors = errors.concat(validator.errors(this, this.validatorOptions));
        }
      }

      return errors;
    };

    // set a value transparently,
    // if no input is found, value gets
    // set inside the _data object
    setValue(field, value) {
      const node = this.form[field];
      if (typeof node === 'object') {
        const $node = $(node);
        if ($node.is('select')) {
          AGN.Lib.Select.get($node).selectValue(value);
        } else {
          $node.val([value]);
        }

        $node.trigger('change');
      } else {
        if (typeof value === 'undefined') {
          delete this._data[field];
        } else {
          this._data[field] = value;
        }
      }
    }

    // set a value for the next request,
    setValueOnce(field, value) {
      if (typeof value === 'undefined') {
        delete this._dataNextRequest[field];
      } else {
        this._dataNextRequest[field] = value;
      }
    }

    // get a value
    getValue(field) {
      const node = this.form[field];
      if (typeof node === 'object') {
        return node.value;
      } else {
        return this._data[field];
      }
    }

    getValues(field) {
      const node = this.form[field];
      if (node instanceof NodeList) {
        return Array.from(node, _node => _node.value);
      }

      return [this.getValue(field)];
    }

    // merges data from the input fields
    // and the _data object
    data() {
      if (this.isMultipart) {
        const formData = new FormData();
        const data = this.$form.serializeFormDataObject();

        $.each(this._data, (k, v) => {
          if (!data.hasOwnProperty(k)) {
            data[k] = v;
          }
        });

        $.each(this._dataNextRequest, (k, v) => {
          data[k] = v;
        });

        $.each(data, (k, v) => {
          if ($.isArray(v)) {
            $.each(v, (index, value) => {
              formData.append(k, value);
            });
          } else {
            formData.append(k, v);
          }
        });

        return formData;
      } else {
        return _.merge(
            {},
            this._data,
            this.$form.serializeObject(),
            this._dataNextRequest
        );
      }
    }

    params() {
      return $.param(this.data(), true);
    }

    // handle for the ajax request
    jqxhr() {
      const self = this;
      const xhr = $.ajaxSettings.xhr();
      const deferred = $.Deferred();

      this.$form.trigger('form:submit');

      const parameters = {
        url: this.getAction(),
        method: this.getMethod(),
        loader: this.loader(),
        xhr: function () {
          return xhr;
        }
      };

      if (this.isMultipart) {
        parameters.data = this.data();
        parameters.enctype = 'multipart/form-data';
        parameters.processData = false;
        parameters.contentType = false;
      } else {
        parameters.data = this.params();
      }

      if (this.isCustomLoader) {
        const onProgress = function (e) {
          let progress = true;

          if (e.lengthComputable) {
            progress = (e.loaded / e.total * 100).toFixed(1);
          }

          self.$form.trigger('form:progress', {
            loaded: e.loaded,
            total: e.total,
            progress: progress
          });
        };

        if ('upload' in xhr) {
          parameters.xhr = function () {
            xhr.upload.onprogress = onProgress;
            return xhr;
          };
        } else {
          parameters.xhrFields = {
            onprogress: onProgress
          };
        }
      }

      if (typeof xhr.abort === 'function') {
        this._abort = function () {
          xhr.abort();
        };
      }

      const jqxhr = $.ajax(parameters);

      this._dataNextRequest = {};

      this.setLoaderShown(true);
      deferred.always(function () {
        self.setLoaderShown(false);
      });

      jqxhr.done(resp => deferred.resolve(resp));
      jqxhr.fail(function () {
        deferred.reject.apply(this, arguments);
      });

      return deferred.promise();
    }

    #submit() {
      this._isSubmitting = true;
      this.setLoaderShown(true);
      const jqxhr = this.jqxhr();

      jqxhr.done(resp => {
        this.setLoaderShown(false);
        this.updateHtml(resp);
        this.$form.trigger('submitted', resp);

        if (this.dirtyChecking) {
          this.#changeDirtyState(this.$form);
        }
      });

      jqxhr.fail(jqxhr => {
        if (jqxhr.responseText) {
          this.handleFieldsMessages(jqxhr.responseText);
          this.$form.trigger('submitted', jqxhr.responseText);
        }
      });

      jqxhr.always(() => this._isSubmitting = false);

      return jqxhr;
    }

    #changeDirtyState($form) {
      const formRewritten = $('body').has($form).length === 0;
      if (formRewritten) {
        $form.dirty('destroy');
      } else {
        $form.dirty('setAsClean')
      }
    }

    #submitStatic() {
      const data = this.data();

      if (CSRF.isProtectionEnabled()) {
        data[CSRF.getParameterName()] = CSRF.readActualToken();
      }

      const $staticForm = $(`<form method="${this.getMethod()}" action="${this.getAction()}"></form>`);
      _.each(data, (value, param) => {
        if (_.isArray(value)) {
          const $select = $(`<select multiple name="${param}"></select>`);
          _.each(value, option => {
            const $option = $('<option selected="selected"></option>');
            $option.val(option);
            $option.appendTo($select);
          });
          $select.appendTo($staticForm);
        } else {
          const $input = $(`<input type="hidden" name="${param}"/>`);
          $input.val(value);
          $input.appendTo($staticForm);
        }
      });

      $staticForm.appendTo($('body'));
      $staticForm.submit();
      $staticForm.remove();

      this._dataNextRequest = {};
    }

    #submitEvent() {
      const submissionEvent = $.Event('submission');
      this.$form.trigger(submissionEvent);

      if (!submissionEvent.isDefaultPrevented()) {
        this.$form.trigger('submitted');
        this._dataNextRequest = {};
      }
    }

    #submitConfirm() {
      this._isSubmitting = true;
      const jqxhr = this.jqxhr();
      jqxhr.done(resp => this.$form.trigger('submitted', resp));
      jqxhr.fail(jqxhr => {
        if (jqxhr.responseText) {
          this.handleFieldsMessages(jqxhr.responseText);
          this.$form.trigger('submitted', jqxhr.responseText);
        }
      });

      jqxhr.always(() => this._isSubmitting = false);

      return jqxhr;
    }

    submit(type, validationOptions = {}) {
      if (this._isSubmitting) {
        console.warn('The form is already being submitted, please wait...')
        return $.Deferred().promise();
      }

      _.each(this.fields, field => field.onSubmit());
      
      if (this.valid(validationOptions)) {
        this.cleanFieldFeedback();
      } else {
        this.handleErrors();
        return false;
      }

      switch (type) {
        case "static":
          return this.#submitStatic();
        case "confirm":
          return this.#submitConfirm();
        case "event":
          return this.#submitEvent();
        default:
          return this.#submit();
      }
    }

    revertAction() {
      // If a user cancels an action - what to do
    }

    reset() {
      this.$form.trigger('reset');
      AGN.Lib.CoreInitializer.run('select', this.$form);
    }

    abort() {
      this.$form.trigger('form:abort');

      if (this._abort) {
        this._abort();
        this._abort = null;
      }
    }

    clear() {
      _.forEach(this.data(), (value, field) => this.setValue(field, ''));
    }

    handleMessages($resp) {
      if (!($resp instanceof $)) {
        $resp = $($resp);
      }

      this.handleFieldsMessages($resp);

      const $messages = $resp.all('script[data-message]');
      _.each($messages, msg => $('body').append(msg));
    }

    #handleFieldsMessages() {
      const $modal = AGN.Lib.Modal.getWrapper(this.$form);
      this.handleFieldsMessages($modal.exists() ? $modal : $('body'));
    }

    handleFieldsMessages($resp) {
      const self = this;

      if (!($resp instanceof $)) {
        $resp = $($resp);
      }

      $resp.all('script[data-message][type="text/html"]').each(function () {
        const $msg = $(this);
        self.showFieldError($msg.data('message'), $msg.text())
        $msg.remove();
      });
    }

    initFields($scope = $(document)) {
      this.fields.length = 0;

      $scope.all('[data-field]').each((index, field) => {
        const $field = $(field);
        if (Form.getWrapper($field).is(this.$form)) {
          this.fields.push(Field.create($field));
        }
      });
    }

    initValidator() {
      this.validatorName = this.$form.data('validator');

      const options = this.$form.data('validator-options');
      this.validatorOptions = options ? AGN.Lib.Helpers.objFromString(options) : null;
    }

    updateHtml(resp) {
      let $newForm;

      if (this.$form.attr('id')) {
        $newForm = $(resp).all(`#${this.$form.attr('id')}`);
      } else if (this.$form.attr('name')) {
        $newForm = $(resp).all(`[name="${this.$form.attr('name')}"]`);
      } else {
        $newForm = $(resp).all('form');
      }

      this.$form.html($newForm.html());

      this.handleMessages(resp);
      this.initFields();
      this.initValidator();

      AGN.Lib.Controller.init(this.$form);
      AGN.runAll(this.$form);
    }

    loader() {
      const self = this;

      return {
        show: function () {
          self.controlsToDisable.collect();
          self.controlsToDisable.setDisabled(true);
          self.setLoaderShown(true);
        },

        hide: function () {
          self.controlsToDisable.setDisabled(false);
          self.setLoaderShown(false);
        }
      };
    }

    setLoaderShown(shown) {
      if (this.isCustomLoader) {
        if (shown) {
          this.$form.trigger('form:loadershow');
        } else {
          this.$form.trigger('form:loaderhide');
        }
      } else {
        if (shown) {
          AGN.Lib.Loader.show();
        } else {
          AGN.Lib.Loader.hide();
        }
      }
    }

    cleanFieldFeedback($field) {
      Form.cleanFieldFeedback$($field, this.$form);

      if (!$field && this.$form.attr('id')) {
        const $inputsOutsideForm = $(`:input[form='${(this.$form.attr('id'))}']`);
        $inputsOutsideForm.each((index, input) => Form.cleanFieldFeedback$($(input)))
      }
    }

    static cleanFieldFeedback$($field, $form) {
      const $group = $field
        ? Form.#findAnchorForFeedback($field).parent()
        : $form.find('.js-form-field-feedback');

      _.each($group, group=> {
        const $group = $(group);

        $group.removeClass('has-feedback has-alert has-success has-warning js-form-field-feedback');
        $group.find('.form-control-feedback-message').remove();
      });
    }

    static appendFeedbackMessage($field, message, type = 'alert') {
      Form.#findAnchorForFeedback($field).parent().append(AGN.Lib.Template.text('input-feedback', {type, message}));
      $field.data('_editor')?.editor?.resize();
    }

    handleErrors() {
      this.cleanFieldFeedback();
      this.#handleErrors(this.errors());
    }

    #handleErrors(errors) {
      errors = errors.filter(error => error.field?.exists());

      //print errors
      _.each(errors, error => {
        const $field = error.field;
        this.markField($field);
        Form.appendFeedbackMessage($field, error.msg);
      });

      const $fields = errors.map(e => e.field);
      ScrollHelper.scrollTo($fields);
    }

    cleanFieldError(field) {
      if (this.form[field]) {
        this.cleanFieldError$($(this.form[field]));
      }
    }

    cleanFieldError$($field) {
      this.cleanFieldFeedback($field);
    }

    showFieldError(field, message, disableScrolling) {
      if (!field || !message) {
        return;
      }

      if (this.form[field]) {
        this.showFieldError$($(this.form[field]), message, disableScrolling);
      } else {
        console.warn(`Field '${field}' not found inside the form!`);
        AGN.Lib.Messages.alertText(message);
      }
    }

    showFieldError$($field, message, disableScrolling) {
      Form.showFieldError$($field, message, disableScrolling)
    }

    static showFieldError$($field, message, disableScrolling) {
      Form.markField($field);

      $field.closest('.js-form-field-feedback').find('.form-control-feedback-message').remove();
      Form.appendFeedbackMessage($field, message);

      if (disableScrolling !== true) {
        Form.scrollToField($field);
      }
    }

    static showFieldError(fieldName, message) {
      const formWithField = Array.from(document.forms)
        .find(form => form[fieldName]);

      if (formWithField) {
        this.get($(formWithField)).showFieldError(fieldName, message);
      }
    }

    markField($field, markType = 'alert') {
      Form.markField($field, markType);
    };

    static markField($field, markType = 'alert') {
      const $anchor = Form.#findAnchorForFeedback($field);
      $anchor.parent().addClass(`has-${markType} has-feedback js-form-field-feedback`);
    }

    static #findAnchorForFeedback($field) {
      if ($field.is('[data-feedback-anchor]')) {
        return $field.closest($field.data('feedback-anchor'));
      }

      const editor = $field.data('_editor')?.editor;
      if (!editor || !editor.container) {
        return $field;
      }

      return $(editor.container);
    }

    static scrollToField($field) {
      ScrollHelper.scrollTo($field);
    }
  }
  
  AGN.Lib.Form = Form;

})();
