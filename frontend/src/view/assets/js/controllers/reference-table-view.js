AGN.Lib.Controller.new('reference-table-settings-modal', function() {

  $(document).on('shown.bs.modal', '.modal[reference-table-settings-modal]', function() {
    var modalInputs = $(this).find('input[data-associated-with], select[data-associated-with]');
    modalInputs.each(function() {
      var input = $(this);
      var associatedWithField = $(input.data('associated-with'));
      if(input.is(':checkbox')){
        input.prop('checked', associatedWithField.val() === 'true')
      } else {
        input.val(associatedWithField.val());
      }
    });
  });

  this.addAction({
    click: 'apply-button'
  }, function() {
    var $button = $(this.el);
    var agnForm = AGN.Lib.Form.get($button);
    var valid = agnForm.validate();
    if(valid) {
      var modalInputs = $button.closest('.modal').find('input[data-associated-with], select[data-associated-with]');
      modalInputs.each(function() {
        var input = $(this);
        var associatedWithField = $(input.data('associated-with'));
        if (input.is(':checkbox')) {
          associatedWithField.val(input.prop('checked'))
        } else {
          associatedWithField.val(input.val());
        }
      });
      AGN.Lib.Modal.getWrapper($button).modal('hide');
    }
  });
});