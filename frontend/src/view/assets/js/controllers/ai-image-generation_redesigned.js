AGN.Lib.Controller.new('ai-image-generation', function () {

  let $generatedImage;
  let $remailingGenerationsText;
  let $generateBtn;
  let $saveBtn;
  let $importantObjectsTable;
  let $filtersTable;

  this.addDomInitializer('ai-image-generation', function () {
    $generatedImage = $('#ai-generated-image');
    $remailingGenerationsText = $('#remaining-generations-text')
    $generateBtn = $('#generate-btn');
    $saveBtn = $('#save-btn');
    $importantObjectsTable = $('#important-objects-table');
    $filtersTable = $('#filters-table');

    updateRemainingGenerationsText(this.config.remainingGenerations);
  });

  this.addAction({click: 'generate'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    setImportantObjectsToForm(form);
    setImageGenerationFiltersToForm(form);

    form.jqxhr().done(resp => {
      if (resp.success) {
        updateRemainingGenerationsText(resp.data.remainingGenerationsCount);
        displayImage(resp.data.url);
        $saveBtn.removeClass('hidden');
      } else {
        AGN.Lib.JsonMessages(resp.popups);
      }
    });
  });

  function displayImage(src) {
    $generatedImage.attr('src', src);
    $generatedImage.parent().removeClass('hidden');
    $generatedImage.parent().prev().addClass('hidden'); // hides info notification
  }

  function updateRemainingGenerationsText(count) {
    if (count <= 0) {
      $remailingGenerationsText.text(t(`mediapool.imageGeneration.limitExceeded`));
      $generateBtn.parent().remove();
    } else {
      $remailingGenerationsText.text(t(`mediapool.imageGeneration.remainingGenerations`, count));
    }
  }

  this.addAction({click: 'save'}, function () {
    const url = $('#ai-generated-image').attr('src');
    $.post(AGN.url('/mediapool/generated-image/save.action'), {url})
      .done(resp => AGN.Lib.Page.render(resp));
  });

  this.addAction({change: 'use-important-objects'}, function () {
    toggleTableState($importantObjectsTable, !this.el.is(':checked'));
  });

  this.addAction({change: 'use-filters'}, function () {
    toggleTableState($filtersTable, !this.el.is(':checked'));
  });

  function toggleTableState($table, disable) {
    const $inputs = $table.find(':input');
    $inputs.prop('disabled', disable);
    $inputs.prev().toggleClass('input-group-text--disabled', disable);
    $table.find('.btn').toggleClass('disabled', disable);
    $inputs.filter('.js-colorpicker').spectrum(disable ? 'disable' : 'enable');
  }

  function setImportantObjectsToForm(form) {
    const table = AGN.Lib.InputTable.get($importantObjectsTable);
    table.collect().forEach(function (object, index) {
      Object.keys(object).forEach(property => {
        form.setValueOnce(`importantObjects[${index}].${property}`, object[property]);
      });
    });
  }

  function setImageGenerationFiltersToForm(form) {
    const table = AGN.Lib.InputTable.get($filtersTable);
    const groupedFilters = _.groupBy(table.collect(), 'name');

    _.forEach(groupedFilters, function (filterValues, filterName) {
      const valuesArray = _.map(filterValues, 'value');
      form.setValueOnce(`filters[${filterName}]`, valuesArray);
    });
  }

});
