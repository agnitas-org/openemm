// todo: GWUA-4517: rename file and controller - remove "-new" suffix
AGN.Lib.Controller.new('emm-activeness', function() {
  function include(states, id, value) {
    states[id] = value;
    updateSaveButtonState(states);
  }

  function exclude(states, id) {
    delete states[id];
    updateSaveButtonState(states);
  }

  function excludeAll(states) {
    for (var id in states) {
      delete states[id];
    }
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

    if ($form && $form.form) {
      for (var id in states) {
        if (states.hasOwnProperty(id)) {
          $form.setValueOnce('activeness[' + id + ']', states[id]);
        }
      }

      $form.submit();
    } else {
      var table = AGN.Lib.Table.get($element);
      if (table) {
        var rowsData = [];

        $.ajax($element.data('form-url'), {
          method: 'POST',
          traditional: false,
          data: {'activeness': states}
        }).done(function(resp) {
          if (resp && resp.success === true) {
            rowsData = saveStates(table, states);
          } else {
            rowsData = resetStates(table, states);
          }
          AGN.Lib.JsonMessages(resp.popups);
        }).fail(function() {
          rowsData = resetStates(table, states);
          AGN.Lib.Messages(t("Error"), t("defaults.error"), "alert");
        }).always(function(){
          table.api.updateRowData({update: rowsData});
          excludeAll(states);
        });
      }
    }
  });

  function saveStates(table, states) {
    applyStates(table, states, false);
  }

  function resetStates(table, states) {
    applyStates(table, states, true);
  }

  function applyStates(table, states, invert) {
    var rows = [];
    table.api.forEachNodeAfterFilterAndSort(function (rowNode, index) {
      var id = rowNode.data.id;
      if (states.hasOwnProperty(id)) {
        var state = invert ? !states[id] : states[id];
        rowNode.data.activeStatus = state ? 'ACTIVE' : 'INACTIVE';
        rows.push(rowNode.data);
      }
    });

    return rows;
  }

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
});
