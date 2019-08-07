AGN.Lib.Controller.new('admin-enabling', function() {

  this.addAction({
    'click': 'save-enabling'
  }, function(){
    prepareEnabledMailinglistsBeforeSaving();
    $('#EnablingForm').submit();
  });

  this.addAction({
    'click': 'toggle-checkboxes-on'
  }, function(){
    disabledCheckboxes(this.el.data("enabling-scope")).each(function () {
      $(this).prop('checked', true);
    });
  });

  this.addAction({
    'click': 'toggle-checkboxes-off'
  }, function(){
    enabledCheckboxes(this.el.data("enabling-scope")).each(function () {
      $(this).prop('checked', false);
    });
  });

  /**
   * Return selector of checked checkboxes with "data-enabling".
   * If scope == undefined returns all checked checkboxes.
   * If scope != undefined returns checked checkboxes with data-enabling=scope
   * @param scope in checkbox definition -> data-enabling=SCOPE
   * @returns {*|jQuery|HTMLElement} checked checkboxes selector
   */
  function enabledCheckboxes(scope){
    if (scope){
      return $("input:checkbox:checked[data-enabling="+scope+"]");
    } else {
      return $("input:checkbox:checked[data-enabling]");
    }
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

  /**
   * Form stores id of disabled mailinglists only, so before a form submitting
   * this method sets disabled mailinglists in form instead of enabled.
   */
  function prepareEnabledMailinglistsBeforeSaving() {
    $("input[name*='enabledMailinglist']").remove();

    disabledCheckboxes('mailinglist').each(function () {
      var mailinglistID = $(this).data("mailinglist");
      $('<input>').attr({
        type: 'hidden',
        name: 'enabledMailinglist[' + mailinglistID + ']',
        value: 'off'
      }).appendTo('#EnablingForm');
    });
  }

});
