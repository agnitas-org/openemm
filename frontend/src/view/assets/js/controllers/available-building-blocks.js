AGN.Lib.Controller.new('available-building-blocks', function() {

  var changedBlockStatuses;
  var blockStatuses;
  var pageForm;

  function init() {
    changedBlockStatuses = {};
    blockStatuses = {};
  }

  this.addDomInitializer('available-building-blocks', function() {
    init();
    var element = this.el;
    pageForm = AGN.Lib.Form.get(element);

    if (this.config.changedBlockStatus) {
        changedBlockStatuses = JSON.parse(this.config.changedBlockStatus)
    }
  });

  this.addDomInitializer('available-building-blocks-table', function() {
    var containerList = this.config.divContainerList;

    _.each(containerList, function(block) {
      var id = block.id;
      var status = block.active;
      if(id in blockStatuses) {
        if (id in changedBlockStatuses) {
            $('input:checkbox[data-block-id=' + id + ']').prop('checked', !!changedBlockStatuses[id]);
        }
      } else {
          blockStatuses[id] = status;
      }
    });

    updateTableControls();
  });

  this.addAction({click: 'update-changed-data'}, function(){
      var element = this.el;
      updateTableControls();
      updateFormData();
      pageForm.setActionOnce(element.data("form-url"));
      pageForm.setResourceSelectorOnce(element.data('form-resource'));
      pageForm.submit();
  });

  this.addAction({change: 'change-activation'}, function () {
    var checkbox = this.el;
    var divContainerId = checkbox.data('block-id');
    var checked = checkbox.is(':checked');

    if(checked == blockStatuses[divContainerId]){
        if (divContainerId in changedBlockStatuses) {
            delete changedBlockStatuses[divContainerId];
        }
    } else {
        changedBlockStatuses[divContainerId] = checked;
    }
  });

  this.addAction({change: 'activate-all'}, function () {
      var activeAll = this.el;
      var $checkboxes = $('input:checkbox[data-block-id]');

      $checkboxes.prop("checked", activeAll.is(':checked'));
      $checkboxes.change();
    });

  function updateTableControls () {
      var controls = $(pageForm.form).find('.table-controls .well');
      controls.first().html(controls.last().html());
  }

  this.updateTableControls = updateTableControls;

  function updateFormData () {
      if (!!pageForm) {
          pageForm.setValue('changedBlockStatusJson',
              JSON.stringify(changedBlockStatuses, function (key, value) {
                  if(value != null) {
                      return value;
                  }
              }));
      }
  }

  this.updateFormData = updateFormData;
});
