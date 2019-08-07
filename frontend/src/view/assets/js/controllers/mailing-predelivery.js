AGN.Lib.Controller.new('mailing-predelivery', function() {
  var self = this;

  this.addAction({'change': 'adjustSpamProvider'},function() {
    var spamProvider;

    if ( this.el.attr('name') == "aol" ) {
      spamProvider = "aolonelinespam";
    } else {
      spamProvider = this.el.attr('name') + "spam";
    }

    if ( this.el.prop('checked')) {
      $('[name="' + spamProvider + '"]').prop('disabled', false);
    } else {
      $('[name="' + spamProvider + '"]').prop('disabled', true);
      $('[name="' + spamProvider + '"]').prop('checked', false);
    }

  });

  this.addAction({'click': 'toggleCheckboxesOn'}, function() {
    this.el.parents('.list-group').find('input[type="checkbox"]').
      prop('checked', true).
      trigger('change');
  });

  this.addAction({'click': 'toggleCheckboxesOff'}, function() {
    this.el.parents('.list-group').find('input[type="checkbox"]').
      prop('checked', false).
      trigger('change');
  });

  this.addInitializer('readjustSpamProviders', function() {
    var providers = {
      'aol': 'aolonelinespam',
      'yahoo': 'yahoospam',
      'gmailnew': 'gmailnewspam',
      'ol2003': 'ol2003spam'
    };

    _.each(providers, function(spamProvider, provider) {
      var $provider = $('[name=' + provider + ']')

      // guard clause - no provider found
      if ( $provider.length == 0 ) {
        return;
      }

      if ( $provider.prop('checked')) {
        $('[name="' + spamProvider + '"]').prop('disabled', false);
      } else {
        $('[name="' + spamProvider + '"]').prop('disabled', true);
        $('[name="' + spamProvider + '"]').prop('checked', false);
      }
    });
  });


});
