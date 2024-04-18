AGN.Lib.Controller.new('target-group-view', function ($scope) {

  const Form = AGN.Lib.Form;

  AGN.Lib.Action.new({'qb:invalidrules': '#targetgroup-querybuilder'}, function () {
    AGN.Lib.Messages.alert('querybuilder.errors.general');
  });

  this.addDomInitializer('target-group-view', function () {
    if (this.config.errorPositionDetails) {
      handleEqlErrorDetails(this.config.errorPositionDetails);
    }
  });

  this.addAction({change: 'toggle-editor-tab'}, function () {
    const $el = this.el;
    const isChecked = $el.is(':checked');
    const form = Form.get($el);

    if (!form.validate({skip_empty: true})) {
      $el.prop('checked', !isChecked);
      return false;
    }

    form.setValueOnce('viewFormat', isChecked ? 'EQL' : 'QUERY_BUILDER');
    form.submit('', {skip_empty: true});
  });

  this.addAction({submission: 'save-target'}, function() {
    Form.get(this.el)
      .submit(this.el.data('submit-type'))
      .done(resp => {
        if (typeof resp == 'object' && !resp.success) {
          handleEqlErrorDetails(resp.data);
          AGN.Lib.JsonMessages(resp.popups);
        }
      });
  });

  function handleEqlErrorDetails(details) {
    const eqlEditor = AGN.Lib.Editor.get($('#eql')).editor;

    eqlEditor.focus();
    eqlEditor.gotoLine(details.line, details.column);
  }
});
