AGN.Lib.Controller.new('emm-activeness', function() {
  function include(states, id, value) {
    states[id] = value;
    updateSaveButtonState(states);
  }

  function exclude(states, id) {
    delete states[id];
    updateSaveButtonState(states);
  }

  function updateSaveButtonState(states) {
    var $saveButton = $('[data-action="save"]');
    var count = 0;

    for (var id in states) {
      if (states.hasOwnProperty(id)) {
        count++;
        break;
      }
    }

    if (count > 0) {
      $saveButton.removeClass('disabled');
    } else {
      $saveButton.addClass('disabled');
    }
  }

  this.addAction({
    click: 'save'
  }, function() {
    var $element = $(this.el);
    var $form = AGN.Lib.Form.get($element);
    var $controller = $element.closest('[data-controller]');
    var states = $controller.data('_states') || {};

    for (var id in states) {
      if (states.hasOwnProperty(id)) {
        $form.setValueOnce('activeness[' + id + ']', states[id]);
      }
    }

    $form.submit();
  });

  this.addAction({
    change: 'toggle-active'
  }, function() {
    var $element = $(this.el);
    var $controller = $element.closest('[data-controller]');
    var states = $controller.data('_states') || {};

    var itemId = $element.data('item-id');
    var state = $element.is(':checked');

    if (state == $element.data('initial-state')) {
      exclude(states, itemId);
    } else {
      include(states, itemId, state);
    }

    $controller.data('_states', states);
  });

  this.addAction({
    click: 'back'
  }, function() {
    history.back();
  });
});
