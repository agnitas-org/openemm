AGN.Lib.Controller.new('trackable-link', function() {
  var self = this;

  this.addAction({
    'click': 'delete-link'
  }, function(){
    var linkId = this.el.data('link-id');

    $('#linkPropertyTable').find('#linkProperty_' + linkId).remove();
  });

  this.addAction({
    'click': 'delete-all-links'
  }, function(){
    $('#linkPropertyTable').find('tbody tr').remove();
  });


  this.addAction({
    'click': 'add-extension'
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

    cTemplate = _.template(AGN.Opt.Templates['link-table-row'], {
      count: count,
      linkName: '',
      linkValue: ''
    });

    $('#linkPropertyTable').find('tbody').append(cTemplate);

  });

  this.addAction({
    'change': 'trackable'
  }, function() {
    self.runInitializer('trackableAction');
  });

  this.addInitializer('trackableAction', function($scope) {
    var $trigger = $('[data-action="trackable"] :selected');

    if ( $trigger.val() == 0 ) {
      $('#linkAction').prop('disabled', true);
    } else {
      $('#linkAction').prop('disabled', false);
    }

  })


});
