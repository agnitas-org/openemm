AGN.Lib.Controller.new('blacklist-list', function () {
  var $blacklistSaveElement;

  this.addDomInitializer('blacklist-list-init', function () {
    $blacklistSaveElement = this.el;
  });

  this.addAction({
    click: 'saveBlacklist'
  }, function () {
    var $element = this.el;
    var form = AGN.Lib.Form.get($element);
    var saveActionUrl = $element.data('url');

    $.ajax(saveActionUrl, {
      type: 'POST',
      data: {
        email: $blacklistSaveElement.find('#new-entry-email').val(),
        reason: $blacklistSaveElement.find('#new-entry-reason').val()
      },
      success: function (resp) {
        if (resp.success === true) {
          form.submit().done(function() {
            AGN.Lib.JsonMessages(resp.popups, true);
          });
        } else {
          AGN.Lib.JsonMessages(resp.popups, true);
        }
      }
    });
  });

});

AGN.Lib.Controller.new('edit-modal-blacklist-list', function () {
  this.addAction({
    click: 'saveChanges'
  }, function () {
    var $element = this.el;
    var updateForm = AGN.Lib.Form.get($element);
    updateForm.submit().done(function(resp) {
      if(resp.success === true) {
        AGN.Lib.Form.get($('#blacklistListView')).submit().done(function() {
          AGN.Lib.JsonMessages(resp.popups, true);
        });
        AGN.Lib.Modal.getWrapper($element).modal('hide');
      } else {
        AGN.Lib.JsonMessages(resp.popups, true);
      }
    });
  });
});

