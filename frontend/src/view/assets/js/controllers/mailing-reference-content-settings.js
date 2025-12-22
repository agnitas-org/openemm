AGN.Lib.Controller.new('reference-content-settings', function() {
  let $container;
  let isAvailable = false;
  let isEnabled = false;
  let initialItems = [];
  let $form;

  function selectKeyColumn() {
    const $select = $('#referenceTableSelect');
    const $option = $select.children('option:selected');
    $('#keyColumnText').val($option.exists() ? $option.data('key-column') : '');
  }

  function createItem(index, value) {
    return AGN.Lib.Template.dom('mailing-reference-content-item', {
      name: getAddonText(index),
      value: value,
      disabled: !isEnabled
    });
  }

  function getAddonText(index) {
    return 'Item' + index;
  }

  function addItem(value) {
    const $item = createItem(getRows$().length + 1, value);
    $container.append($item);
  }

  function renderItems(items) {
    if (!isEnabled) {
      addItem('');
      return;
    }
     _.each(items, value => addItem(value));
  }

  function getRows$() {
    return $('#reference-content-items .row');
  }
  this.addDomInitializer('reference-content-settings', function() {
    $container = $('#reference-content-items');
    $form = $('#mailingSettingsForm');
    selectKeyColumn();

    isAvailable = this.config.isAvailable;
    isEnabled = this.config.isEnabled;
    renderItems(this.config.items);
    initialItems = getRows$();
    controlSelectDisplaying();
    $form.dirty('setAsClean');
    $form.dirty('refreshEvents');
  });

  function controlSelectDisplaying() {
    $('#referenceContentEnabled').on('change', function() {
      $('#referenceTableSelect').prop('disabled', !$(this).prop('checked'));
    });
  }

  this.addAction({change: 'select-reference-table'}, function() {
    selectKeyColumn();
  });

  this.addAction({click: 'add-reference-content-item'}, function() {
    replaceNewButtonWithDeleteButton();
    addItem('');
    $form.dirty('setAsDirty');
  });

  function replaceNewButtonWithDeleteButton() {
    const newBtn = $container.find('[data-action="add-reference-content-item"]');
    newBtn.after(_.template(AGN.Opt.Templates['mailing-reference-content-delete-btn'])({disabled: !isEnabled}));
    newBtn.remove();
  }

  function isItemsChanged() {
    return (initialItems.length !== getRows$().length)
      || (initialItems.some(value => !getRows$().includes(value)));
  }

  function updateTextOfAddons() {
    getRows$().each((index, row) => {
      $(row).find('.input-group-text').text(getAddonText(index + 1));
    })
  }

  this.addAction({click: 'delete-reference-content-item'}, function() {
    this.el.closest('.row').remove();
    updateTextOfAddons();
    if (!isItemsChanged()) {
      $form.dirty('setAsDirty'); // TODO check and check submit values on save
    }
  });
});
