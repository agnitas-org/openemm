AGN.Lib.Controller.new('available-building-blocks', function () {

  let changedBlockStatuses;
  let blockStatuses;
  let pageForm;

  function init() {
    changedBlockStatuses = {};
    blockStatuses = {};
  }

  this.addDomInitializer('available-building-blocks', function () {
    init();
    pageForm = AGN.Lib.Form.get(this.el);

    if (this.config.changedBlockStatus) {
      changedBlockStatuses = JSON.parse(this.config.changedBlockStatus)
    }
  });

  this.addDomInitializer('available-building-blocks-table', function () {
    const containerList = this.config.divContainerList;

    _.each(containerList, function (block) {
      const id = block.id;
      const status = block.active;
      if (id in blockStatuses) {
        if (id in changedBlockStatuses) {
          $(`input:checkbox[data-block-id='${id}']`).prop('checked', !!changedBlockStatuses[id]);
        }
      } else {
        blockStatuses[id] = status;
      }
    });
  });

  this.addAction({click: 'search'}, function () {
    const $el = this.el;
    updateFormData();
    pageForm.setActionOnce($el.data("form-url"));
    pageForm.setResourceSelectorOnce($el.data('form-resource'));
    pageForm.submit();
  });

  this.addAction({click: 'save-changed-data'}, function () {
    const $el = this.el;
    updateFormData();
    pageForm.setActionOnce($el.data("form-url"));

    pageForm.jqxhr().done(resp => {
      AGN.Lib.JsonMessages(resp.popups);
      if (resp.success) {
        AGN.Lib.Confirm.get($el).positive();
      }
    });
  });

  this.addAction({change: 'change-activation'}, function () {
    const $checkbox = this.el;
    const divContainerId = $checkbox.data('block-id');

    if ($checkbox.is(':checked') == blockStatuses[divContainerId]) {
      if (divContainerId in changedBlockStatuses) {
        delete changedBlockStatuses[divContainerId];
      }
    } else {
      changedBlockStatuses[divContainerId] = $checkbox.is(':checked');
    }
  });

  this.addAction({change: 'activate-all'}, function () {
    const $checkboxes = $('input:checkbox[data-block-id]');
    $checkboxes.prop("checked", this.el.is(':checked')).change();
  });

  function updateFormData() {
    if (!!pageForm) {
      const jsonString = JSON.stringify(changedBlockStatuses,
        (key, value) => value !== null ? value : undefined);

      pageForm.setValue('changedBlockStatusJson', jsonString);
    }
  }
});
