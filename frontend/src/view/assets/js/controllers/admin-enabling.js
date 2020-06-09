AGN.Lib.Controller.new('admin-enabling', function() {

  this.addAction({
    'click': 'save-approved-mailing-lists'
  }, function(){
    var $element = this.el;
    var $form = AGN.Lib.Form.getWrapper($element);
    prepareDisabledMailinglistsBeforeSaving($form);
    var formObj = AGN.Lib.Form.get($form);
    formObj.submit().done(function(resp) {
      if(resp.success === true) {
        AGN.Lib.JsonMessages(resp.popups, true);
        AGN.Lib.Modal.getWrapper($element).modal('hide');
      } else {
        AGN.Lib.JsonMessages(resp.popups, true);
      }
    });
  });

  function prepareDisabledMailinglistsBeforeSaving($form) {
    $("input[name*='disabledMailinglistsIds']").remove();

    disabledCheckboxes('mailinglist').each(function () {
      $('<input>').attr({
        type: 'hidden',
        name: 'disabledMailinglistsIds',
        value: $(this).data("mailinglist"),
      }).appendTo($form);
    });
  }

  /**
   * Return selector of unchecked checkboxes with "data-enabling".
   * If scope == undefined returns all unchecked checkboxes.
   * If scope != undefined returns unchecked checkboxes with data-enabling=scope
   * @param scope in checkbox definition -> data-enabling=SCOPE
   * @returns {*|jQuery|HTMLElement} unchecked checkboxes selector
   */
  function disabledCheckboxes(scope){
    if (scope){
      return $("input:checkbox:not(:checked)[data-enabling="+scope+"]");
    } else {
      return $("input:checkbox:not(:checked)[data-enabling]");
    }
  }

});
