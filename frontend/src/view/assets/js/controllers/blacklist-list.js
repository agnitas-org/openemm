AGN.Lib.Controller.new('blacklist-list', function () {
  var Confirm = AGN.Lib.Confirm;
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
    var blacklist = {
      email: $blacklistSaveElement.find('#new-blacklist').val()
    };

    $.ajax(saveActionUrl, {
      type: 'POST',
      data: blacklist ,
      success: function (responseMessages) {
        AGN.Lib.Page.render(responseMessages);
        form.submit();
      }
    });
  });

  this.addAction({click: 'blacklist-delete'}, function() {
    Confirm.get(this.el).promise().done(function(response) {
      AGN.Lib.Page.render(response);
      $('#blacklistListView').submit();
    });
  });
});
