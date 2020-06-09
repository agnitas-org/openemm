AGN.Lib.Controller.new('trackable-link', function() {
  var self = this;

  this.addAction({
    'click': 'link-details-delete-link'
  }, function(){
    var linkId = this.el.data('link-id');

    $('#linkPropertyTable').find('#linkProperty_' + linkId).remove();
  });

  this.addAction({
    'click': 'link-details-delete-all-links'
  }, function(){
    $('#linkPropertyTable').find('tbody tr').remove();
  });


  this.addAction({
    'click': 'link-details-add-extension'
  }, function(){

    var cTemplate,
        count,
        $links = $('#linkPropertyTable').find('tr[data-link-id]');

    if ( $links.length != 0 ) {
      count = Math.max.apply(Math, _.map($links, function(link) {
        return parseInt($(link).data('link-id'));
      })) + 1;
    } else  {
      count = $('#linkPropertyTable').find('tbody tr').length + 1 ;
    }

    cTemplate = AGN.Lib.Template.text('link-table-row', {
      count: count,
      linkName: '',
      linkValue: ''
    });

    $('#linkPropertyTable').find('tbody').append(cTemplate);

  });

  this.addAction({
    'change': 'link-details-trackable'
  }, function() {
    self.runInitializer('trackableAction');
  });

  this.addInitializer('trackableAction', function($scope) {
    var $trigger = $('[data-action="link-details-trackable"] :selected');
    $('#linkAction').prop('disabled', $trigger.val() == 0);
  })


});
