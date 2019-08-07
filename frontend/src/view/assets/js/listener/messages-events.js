(function(){

  var Action   = AGN.Lib.Action,
      Messages = AGN.Lib.Messages;

  Action.new({'click': '[data-msg]'}, function() {
    var head    = this.el.data('msg'),
        content = this.el.data('msg-content'),
        type    = this.el.data('msg-type');

    if (this.el.data('msg-system') === 'system') {
      var $scope = $(document);
      if ($scope.find('.notification-info').is(':visible')) {
        return false;
      } else {
        Messages(head, content, type);
      }
    } else {
      Messages(head, content, type);
    }
  });

  // prevent navigation on message action links
  $(document).on('click',
    'a[data-msg]',
    function(e) {
      e.preventDefault();
  });

})();
