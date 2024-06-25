AGN.Lib.Controller.new('target-group-view', function ($scope) {

  var Action = AGN.Lib.Action,
    Form = AGN.Lib.Form,
    Editor = AGN.Lib.Editor;

  Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function() {
      AGN.Lib.Messages(t('defaults.error'), t('querybuilder.errors.general'), 'alert');
  });

  this.addDomInitializer('target-group-view', function () {
    var $el = $(this.el);

    $('[data-toggle-tab]').on('click', function (e) {
      //skip empty rule to proper validation while toggling between tabs
      var isValid = Form.get($el).validate({skip_empty: true});
      if (!isValid) {
        e.preventDefault();
        return false;
      }
    });

    if (this.config.errorPositionDetails) {
      handleEqlErrorDetails(this.config.errorPositionDetails);
    }
  });

  this.addAction({click: 'switch-tab-viewEQL'}, function() {
      var element = this.el,
        form = AGN.Lib.Form.get($(element));

      form.setValueOnce('viewFormat', 'EQL');
      form.submit('', {skip_empty: true});
  });

  this.addAction({click: 'switch-tab-viewQB'}, function() {
    var element = this.el,
      form = AGN.Lib.Form.get($(element));

    form.setValueOnce('viewFormat', 'QUERY_BUILDER');
    const jqxhr = form.submit('', {skip_empty: true});
    jqxhr.done(function(resp) {
      handleFormSaveResponse(resp);
      AGN.Lib.Tab.show($('#eql-editor-tab-trigger'));
      $('#eql-alert').remove();
      $('#eql').before(getNotificationMessage(resp.popups.alert[0]));
    });
  });

  this.addAction({submission: 'save-target'}, function() {
    Form
      .get(this.el)
      .submit(this.el.data('submit-type'))
      .done(handleFormSaveResponse);
  });

  function handleFormSaveResponse(resp) {
    if (resp.success) {
      return;
    }
    handleEqlErrorDetails(resp.data);
    AGN.Lib.JsonMessages(resp.popups);
  }

  function handleEqlErrorDetails(details) {
    const eqlEditor = Editor.get($('#eql')).editor;

    eqlEditor.focus();
    eqlEditor.gotoLine(details.line, details.column);
  }

  function getNotificationMessage(msg) {
    return '<ul = id="eql-alert">' +
      '         <div class="tile">' +
      '             <li class="tile-notification tile-notification-alert">' + msg + '</li>' +
      '         </div>' +
      '    </ul>'
  }
})
