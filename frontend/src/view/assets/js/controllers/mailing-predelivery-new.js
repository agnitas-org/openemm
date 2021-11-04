AGN.Lib.Controller.new('mailing-predelivery-new', function () {

  var providers = {
    'aol': 'aolonelinespam',
    'yahoo': 'yahoospam',
    'gmailnew': 'gmailnewspam',
    'ol2003': 'ol2003spam'
  }

  this.addAction({change: 'adjustSpamProvider'}, function () {
    var $el = $(this.el);

    var spamProvider = providers[$el.attr('name')];

    // guard clause - no spamProvider supported
    if (!spamProvider) {
      return;
    }

    var $spanProvider = $('[name="' + spamProvider + '"]');
    if ($el.prop('checked')) {
      $spanProvider.prop('disabled', false);
    } else {
      $spanProvider.prop('disabled', true);
      $spanProvider.prop('checked', false);
    }

  });

  this.addInitializer('readjustSpamProviders', function () {
    _.each(providers, function (spamProvider, provider) {
      var $provider = $('[name=' + provider + ']')

      // guard clause - no provider found
      if ($provider.length === 0) {
        return;
      }

      var $spamProvider = $('[name="' + spamProvider + '"]');

      if ($provider.prop('checked')) {
        $spamProvider.prop('disabled', false);
      } else {
        $spamProvider.prop('disabled', true);
        $spamProvider.prop('checked', false);
      }
    });
  });

  function toggleCheckboxes($el, isChecked) {
    var checkboxes = $el.closest('.list-group').find('input[type="checkbox"]:enabled')
    checkboxes.prop('checked', isChecked);
    checkboxes.trigger('change');
  }

  this.addAction({click: 'toggle-checkboxes-on'}, function() {
    toggleCheckboxes($(this.el), true);
  });

  this.addAction({click: 'toggle-checkboxes-off'}, function() {
    toggleCheckboxes($(this.el),false);
  });
});
