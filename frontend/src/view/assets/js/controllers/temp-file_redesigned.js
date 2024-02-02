AGN.Lib.Controller.new('temp-file', function() {

  let modalAlreadyRequested;

  this.addAction({'upload:add': 'show-upload-file-form'}, function() {
    if (modalAlreadyRequested) {
      return;
    }
    modalAlreadyRequested = true;
    $
      .get(AGN.url("/upload/new.action"))
      .done((resp) => drawUploadModal(resp, this.el.data("upload-selection")));
  });

  function drawUploadModal(modalHtml, files) {
    AGN.Lib.Page.render(modalHtml);
    addFilesInfoToModal(files.map(file => file.name));
    modalAlreadyRequested = false;
    $('.modal').on('modal:close', clearDropzone)
  }

  function addFilesInfoToModal(fileNames) {
    const $modalTitle = $('#file-names');
    if (fileNames.length > 1) {
      addFilesTableToModal();
      return;
    }
    $modalTitle.append(fileNames);
  }

  function clearDropzone() {
    $('#files').val('');
    $('#upload-file').trigger('upload:reset');
  }

  this.addAction({click: 'save-file'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    form.setValue('files', Array.from($('#files')[0].files));

    const jqxhr = form.submit();
    if (jqxhr) {
      jqxhr.done(resp => {
        AGN.Lib.Modal.getWrapper(form.get$()).modal('hide');
        AGN.Lib.JsonMessages(resp.popups);
      });
      $('.modal').hide(); // don't destroy just hide, because it's form used by the progress bar
    }
  });

  function getFilesTableColumnDefs() {
    return [
      {
        field: 'name',
        headerName: t('defaults.name'),
        filter: false
      },
      {
        field: 'size',
        headerName: t('defaults.size'),
        filter: false,
        width: 50
      }
    ];
  }

  function getFilesDataForTable(files) {
    return files.map(({name, size}) => ({name, size: AGN.Lib.Helpers.formatBytes(size)}));
  }

  function addFilesTableToModal() {
    const files = Array.from($('#files')[0].files);
    // rows + header + table-controls
    const tableHeight = (files.length + 2) * 40;
    const table = new AGN.Lib.Table(
      $(`<div class="col-12" style="height: ${tableHeight}px; max-height: 220px">`),
      getFilesTableColumnDefs(),
      getFilesDataForTable(files),
      {pagination: false}
    );
    $('#upload-form').find('.row:first').prepend(table.$el);
  }
});
