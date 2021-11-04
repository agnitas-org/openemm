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

The `data-disable-controls` attribute allows to disable some controls (the ones having `data-controls-group` attribute) during form submission (and enable them back afterwards).
Use `data-disable-controls="*"` to refer all the elements having `data-controls-group` attribute or specify a particular value to refer a named group (group's name is defined by `data-controls-group` attribute).

```htmlexample
<form action="#form-features-02" id="controlsDisablingDemoForm" data-form="" data-disable-controls="group-1">
  <div class="form-group">
    <div class="col-sm-4">
      <label class="control-label">
        <label for="Name">First name</label>
      </label>
    </div>
    <div class="col-sm-8">
      <input type="text" name="firstname" class="form-control" data-controls-group="group-1"/>
    </div>
  </div>

  <div class="form-group">
    <div class="col-sm-4">
      <label class="control-label">
        <label for="Name">Last name</label>
      </label>
    </div>
    <div class="col-sm-8">
      <input type="text" name="lastname" class="form-control" data-controls-group="group-2"/>
    </div>
  </div>

  <div class="form-group">
    <div class="col-sm-4">
      <label class="control-label">
        <label for="Name">Last name</label>
      </label>
    </div>
    <div class="col-sm-8">
      <button type="submit" class="btn btn-regular btn-primary" data-controls-group="group-1">Submit</button>
    </div>
  </div>
</form>
```
*/

(function(){

  var Form,
      Field = AGN.Lib.Field,
      ControlsToDisable;

  ControlsToDisable = function(group) {
    this.group = group;
    this.$controls = $();
  };

  ControlsToDisable.prototype._collect = function(group) {
    var $all = $('[data-controls-group]');

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
  };

  ControlsToDisable.prototype.collect = function() {
    if (this.group) {
      this.$controls = this._collect(this.group).filter(function() {
        var $e = $(this);
        // Exclude disabled controls (otherwise they will be enabled when submission is complete).
        return !$e.prop('disabled') && !$e.hasClass('disabled');
      });
    }
  };

  ControlsToDisable.prototype.setDisabled = function(isDisabled) {
    if (isDisabled === false) {
      this.$controls.removeClass('disabled');
      this.$controls.prop('disabled', false);
    } else {
      this.$controls.addClass('disabled');
      this.$controls.prop('disabled', true);
    }
  };

  Form = function($form) {
    this.$form = $form;
    this.form  = $form[0];

    this._abort = null;
    this._data  = {};
    this._dataNextRequest = {};
    this.fields = [];
    this.validatorName = null;
    this.validatorOptions = null;

    this.url = $form.attr('action');
    this.urlNextRequest = null;
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
  };

  // Static Method
  // gets a singleton instance
  // of the form object
  Form.get = function($needle) {
    var $form,
        type,
        formType,
        formObj;

    $form = Form.getWrapper($needle);
    formObj = $form.data('_form');

    if (!formObj) {
      formType = $form.data('form');
      type = AGN.Opt.Forms[formType] || AGN.Lib.Form;
      formObj = new type($form);
      $form.data('_form', formObj);
    }

    return formObj;
  };

  // gets the jquery wrapped form tag
  Form.getWrapper = function($needle) {
    var $form;

    $form = $($needle.data('form-target'));

    if ( $form.length == 0 ) {
       $form = $needle.closest('form');
    }

    return $form;
  };

  Form.prototype.valid = function(options) {
    var validationEvent = $.Event('validation');
    options = _.merge({}, this.validatorOptions, options);
    this.$form.trigger(validationEvent, options);

    if (validationEvent.isDefaultPrevented()) {
      return false;
    }

    var fieldsValid = _.every(this.fields, function(field) {
      return field.valid()
    });

    if (this.validatorName) {
      var validator = AGN.Lib.Validator.get(this.validatorName);
      if (validator) {
        return validator.valid(this, options, fieldsValid);
      }
    }

    return fieldsValid;
  };

  Form.prototype.validate = function(options) {
    if (this.valid(options)) {
      return true;
    } else {
      this.handleErrors();
      return false;
    }
  };

  Form.prototype.get$ = function() {
    return this.$form;
  };

  Form.prototype.getAction = function() {
    if (this.urlNextRequest) {
      var value = this.urlNextRequest;
      this.urlNextRequest = null;
      return value;
    }
    return this.url;
  };

  Form.prototype.setActionOnce = function(url) {
    this.urlNextRequest = url;
  };

  Form.prototype.errors = function() {
    var errors = [];

    _.each(this.fields, function(field) {
      errors = errors.concat(field.errors());
    });

    if (this.validatorName) {
      var validator = AGN.Lib.Validator.get(this.validatorName);
      if (validator) {
        errors = errors.concat(validator.errors(this, this.validatorOptions));
      }
    }

    return errors;
  };

  // set a value transparently,
  // if no input is found, value gets
  // set inside the _data object
  Form.prototype.setValue = function(field, value) {
    var node = this.form[field];
    if (typeof node === 'object') {
      $(node).val([value]);
      $(node).trigger('change');
    } else {
      if (typeof value === 'undefined') {
        delete this._data[field];
      } else {
        this._data[field] = value;
      }
    }
  };

  // set a value for the next request,
  Form.prototype.setValueOnce = function(field, value) {
    if (typeof value === 'undefined') {
      delete this._dataNextRequest[field];
    } else {
      this._dataNextRequest[field] = value;
    }
  };

  // get a value
  Form.prototype.getValue = function(field) {
    var node = this.form[field];
    if (typeof node === 'object') {
      return node.value;
    } else {
      return this._data[field];
    }
  };

  // merges data from the input fields
  // and the _data object
  Form.prototype.data = function() {
    if (this.isMultipart) {
      var formData = new FormData();
      var data = this.$form.serializeFormDataObject();

      $.each(this._data, function(k, v) {
        if (!data.hasOwnProperty(k)) {
          data[k] = v;
        }
      });

      $.each(this._dataNextRequest, function(k, v) {
        data[k] = v;
      });

      $.each(data, function(k, v) {
        if ($.isArray(v)) {
          $.each(v, function(index, value) {
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
  };

  Form.prototype.params = function() {
    return $.param(this.data(), true);
  };

  // handle for the ajax request
  Form.prototype.jqxhr = function() {
    var self = this,
        xhr = $.ajaxSettings.xhr(),
        jqxhr,
        deferred = $.Deferred();

    this.$form.trigger('form:submit');

    var parameters = {
      url: this.getAction(),
      method: this.method,
      loader: this.loader(),
      xhr: function() { return xhr; }
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
      var onProgress = function(e) {
        var progress = true;

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
        parameters.xhr = function() {
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
      this._abort = function() {
        xhr.abort();
      };
    }

    jqxhr = $.ajax(parameters);

    this._dataNextRequest = {};

    this.setLoaderShown(true);
    deferred.always(function() {
      self.setLoaderShown(false);
    });

    jqxhr.done(function (resp){
      var $resp = $(resp),
          $pollingForm,
          pollingFormObj,
          pollingFormJqxhr;

      $pollingForm = $resp.
          filter('[data-form="polling"]').
          add($resp.find('[data-form="polling"]'));

      // response includes a polling form
      if ($pollingForm.length == 1) {
          pollingFormObj =  Form.get($pollingForm);

          // submit the polling form and wait for the response
          pollingFormJqxhr = pollingFormObj.jqhxr();

          pollingFormJqxhr.done(function() {
            deferred.resolve.apply(this, arguments);
            var $body = $('body');
            var $resp = $('<div></div>');
            $resp.html(resp);

            $resp.find('script[data-message][type="text/html"]')
                .appendTo($body);

            $resp.find('script[data-message][type="text/javascript"]').each(function() {
              try {
                eval.call(window, $(this).html());
              } catch (e) {}
            });

            $resp.remove();
          });

          pollingFormJqxhr.fail(function() {
            deferred.reject.apply(this, arguments);
          });

      // response is the response
      } else {
        deferred.resolve(resp);
      }

    });

    jqxhr.fail(function () {
      deferred.reject.apply(this, arguments);
    });

    return deferred.promise();
  };

  Form.prototype._submit = function() {
    var jqxhr,
        self = this;

    this.setLoaderShown(true);
    jqxhr = this.jqxhr();

    jqxhr.done(function(resp) {
      self.setLoaderShown(false);
      self.updateHtml(resp);
      self.$form.trigger('submitted', resp);
    });

    return jqxhr;
  };

  Form.prototype._submitStatic = function() {
    var data = this.data(),
        $staticForm;

    $staticForm = $('<form method="' + this.method + '" action="' + this.getAction() + '"></form>');
    _.each(data, function(value, param) {
      if (_.isArray(value)) {
        var $select = $('<select multiple name="' + param + '"></select>');
        _.each(value, function(option) {
          var $option = $('<option selected="selected"></option>');
          $option.val(option);
          $option.appendTo($select);
        });
        $select.appendTo($staticForm);
      } else {
        var $input = $('<input type="hidden" name="' + param + '"/>');
        $input.val(value);
        $input.appendTo($staticForm);
      }
    });

    $staticForm.appendTo($('body'));
    $staticForm.submit();
    $staticForm.remove();

    this._dataNextRequest = {};
  };

  // submit an action
  Form.prototype._submitAction = function(actionId) {
    this.setValue('previousAction', this.getValue('actionList'));
    this.setValueOnce('action', actionId);

    return this.submit();
  };

  Form.prototype._submitEvent = function() {
    var submissionEvent = $.Event('submission');
    this.$form.trigger(submissionEvent);

    if (!submissionEvent.isDefaultPrevented()) {
      this.$form.trigger('submitted');
      this._dataNextRequest = {};
    }
  };

  Form.prototype._submitConfirm = function(actionId) {
    this.setValue('previousAction', this.getValue('actionList'));
    this.setValueOnce('action', actionId);

    var jqxhr = this.jqxhr();
    var self = this;

    jqxhr.done(function(resp) {
      self.$form.trigger('submitted', resp);
    });

    return jqxhr;
  };

  Form.prototype.submit = function(type) {
    var actionId = 0;
    var validationOptions = {};
    if ((type == 'confirm' || type == 'action') &&
        (typeof arguments[1] === 'number' || typeof arguments[1] === 'string')) {
      actionId = arguments[1];
      validationOptions = arguments[2];
    } else {
      validationOptions = arguments[1];
    }

    if (this.valid(validationOptions)) {
      this.cleanErrors();
    } else {
      this.handleErrors();
      return false;
    }

    switch(type) {
      case "static":
        return this._submitStatic();
      case "confirm":
        return this._submitConfirm(actionId);
      case "action":
        return this._submitAction(actionId);
      case "event":
        return this._submitEvent();
      default:
        return this._submit();
    }
  };

  Form.prototype.revertAction = function() {
    // If a user cancels an action - what to do
  };

  Form.prototype.reset = function() {
    this.$form.trigger('reset');
  };

  Form.prototype.abort = function() {
    this.$form.trigger('form:abort');

    if (this._abort) {
      this._abort();
      this._abort = null;
    }
  };

  Form.prototype.bulkUpdate = function(group, value) {
    if (group) {
      this.$form.find('[name^="' + group + '"]').prop('checked', value);
    } else {
      this.$form.find('.js-bulk-ids').prop('checked', value);
    }
  };

  Form.prototype.handleMessages = function(resp) {
    var $resp = $(resp),
        $messages;

    $messages = $resp.
      filter('script[data-message]').
      add($resp.find('script[data-message]'));

    _.each($messages, function(msg) {
      $('body').append(msg);
    })
  };

  Form.prototype.initFields = function($scope) {
    var self = this;

    if (!$scope) {
      $scope = this.$form;
    }

    self.fields.length = 0;
    _.each($scope.find('[data-field]'), function(field) {
      self.fields.push(Field.create($(field)));
    });
  };

  Form.prototype.initValidator = function() {
    this.validatorName = this.$form.data('validator');

    var options = this.$form.data('validator-options');
    this.validatorOptions = options ? AGN.Lib.Helpers.objFromString(options) : null;
  };

  Form.prototype.updateHtml = function(resp) {
    var $newForm,
        $resp = $(resp);

    if (this.$form.attr('id')) {
      $newForm = $(resp).
        filter('#' + this.$form.attr('id')).
        add($resp.find('#' + this.$form.attr('id')));
    } else if (this.$form.attr('name')) {
      $newForm = $(resp).
        filter('[name="' + this.$form.attr('name') + '"]').
        add($resp.find('[name="' + this.$form.attr('name') + '"]'));
    } else {
      $newForm = $(resp).
        filter('form').
        add($resp.find('form'));
    }

    this.$form.html($newForm.html());

    this.handleMessages(resp);
    this.initFields();
    this.initValidator();

    AGN.Lib.Controller.init(this.$form);
    AGN.runAll(this.$form);

  };

  Form.prototype.loader = function() {
    var self = this;

    return {
      show: function() {
        self.controlsToDisable.collect();
        self.controlsToDisable.setDisabled(true);
        self.setLoaderShown(true);
      },

      hide: function() {
        self.controlsToDisable.setDisabled(false);
        self.setLoaderShown(false);
      }
    };
  };

  Form.prototype.setLoaderShown = function(shown) {
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
  };

  Form.prototype.cleanErrors = function($group) {
    if (!$group) { $group = this.$form.find('.js-form-error') }

    _.each($group, function(group) {
      var $group = $(group);

      $group.removeClass('has-alert has-feedback js-form-error');
      $group.find('.js-form-error-ind').remove();
      $group.find('.js-form-error-msg').remove();
    });
  };

  function appendFeedbackMessage($field, message) {
    var anchor = $field;
    var editor = $field.data('_editor');
    var emojioneArea = $field.data('emojioneArea');
    var showIcon = true;

    if (editor) {
      // For inputs managed by editors a feedback message has to be shown below an actual editor's element
      editor = editor.editor;
      if (editor.container) {
        showIcon = false;
        anchor = $(editor.container);
      }
    } else if (emojioneArea) {
      if (emojioneArea.editor) {
        showIcon = false;
        anchor = emojioneArea.editor.parent();
      }
    } else {
      if (anchor.is('select')) {
        showIcon = false;

        if (anchor.is('.input-group .input-group-controls .js-select')) {
          anchor = anchor.closest('.input-group');
        }
      }
    }

    if (showIcon) {
      anchor.after('<span class="icon icon-state-alert form-control-feedback js-form-error-ind"></span>');
    }
    anchor.after('<div class="form-control-feedback-message js-form-error-msg">' + message + '</div>');
  }

  Form.prototype.handleErrors = function() {
    var errorPos = [];

    this.cleanErrors();

    var errors = this.errors().filter(function (error) {
      return error.field && error.field.length > 0;
    });

    //print errors
    _.each(errors, function(error) {
      var $field = error.field;
      $field.parents('.form-group').addClass('has-alert has-feedback js-form-error');
      appendFeedbackMessage($field, error.msg);
    });

    //calculate errors positions
    _.each(this.errors(), function(error) {
      var $field = error.field;
      if ($field.data('select2')) {
        errorPos.push($field.parent().find('.select2-container').offset().top);
      } else if ($field.data('_editor') && $field.data('_editor').editor) {
        var editor = $field.data('_editor').editor;
        errorPos.push($(editor.container).offset().top);
      } else {
        errorPos.push($field.offset().top);
      }
    });

    var containerScroll = false;
    var $view = this.$form.closest('.modal');
    if (!$view.exists()) {
      $view = $('.tile-content[data-sizing="scroll"]');
      if ($view.exists()) {
        containerScroll = true;
      }
    }
    if (!$view.exists()) {
      $view = $(document);
    }

    if ($view.find('.popover.support-popover').exists()) {
      return;
    }

    var formPosition = this.$form.offset().top;
    var scrollTopPos;
    if(containerScroll) {
      scrollTopPos = _.sortBy(errorPos)[0] + $view.scrollTop() - $view.offset().top - 25;
    } else {
      scrollTopPos = _.sortBy(errorPos)[0] - formPosition - 25;
    }
    $view.scrollTop(scrollTopPos);
  };

  Form.prototype.cleanFieldError = function(field) {
    if (this.form[field]) {
      this.cleanFieldError$($(this.form[field]));
    }
  };

  Form.prototype.cleanFieldError$ = function($field) {
    var $group = $field.closest('.js-form-error');

    $group.removeClass('has-alert has-feedback js-form-error');
    $group.find('.js-form-error-ind').remove();
    $group.find('.js-form-error-msg').remove();
  };

  Form.prototype.showFieldError = function(field, message, disableScrolling) {
    if (this.form[field]) {
      this.showFieldError$($(this.form[field]), message, disableScrolling);
    }
  };

  Form.prototype.showFieldError$ = function($field, message, disableScrolling) {
    var $group = $field.parents('.form-group');

    $group.addClass('has-alert has-feedback js-form-error');
    $group.find('.js-form-error-ind').remove();
    $group.find('.js-form-error-msg').remove();

    appendFeedbackMessage($field, message);

    if (disableScrolling !== true) {
      var $view = this.$form.closest('[data-sizing=scroll], .modal').first();
      if ($view.exists()) {
        var $target = $(".has-alert").first();

        $view.animate({scrollTop: $target.offset().top - $view.offset().top + $view.scrollTop()});
      } else {
        $view = $(document);
        $view.scrollTop($field.offset().top - 25);
      }
    }
  };

  AGN.Lib.Form = Form;

})();
