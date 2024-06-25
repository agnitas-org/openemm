AGN.Lib.Controller.new('temp-file', function() {

  const Form = AGN.Lib.Form;
  let modalAlreadyRequested;

  this.addAction({'upload:add': 'show-upload-file-form'}, function() {
    if (modalAlreadyRequested) {
      return;
    }
    modalAlreadyRequested = true;

    $.ajax({
      url: AGN.url("/upload/new.action"),
      type: 'GET',
      async: false,
      success: resp => {
        AGN.Lib.Page.render(resp);
        $('.modal').on('modal:close', clearDropzone);

        $getUploadForm().on('upload:filesSet', (e, files) => {
          addFilesInfoToModal(files);
          modalAlreadyRequested = false;
        });
      }
    });
  });

  function addFilesInfoToModal(files) {
    if (files.length > 1) {
      addFilesTableToModal(files);
      return;
    }

    const $modalTitle = $('#file-names');
    $modalTitle.append(files[0].name);
  }

  function clearDropzone() {
    $('#files').val('');
    $('#dropzone').trigger('upload:reset');
  }

  this.addAction({click: 'save-file'}, function () {
    const form = Form.get(this.el);

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

  function addFilesTableToModal(files) {
    // rows + header + table-controls
    const tableHeight = (files.length + 2) * 40;
    const table = new AGN.Lib.Table(
      $(`<div class="js-data-table-body">`),
      getFilesTableColumnDefs(),
      getFilesDataForTable(files),
      {pagination: false}
    );

    const $tableWrapper = $(`<div class="col-12" style="height: ${tableHeight}px; max-height: 220px">`);
    $tableWrapper.append(table.$el);

    $getUploadForm().find('.row:first').prepend($tableWrapper);
  }

  function $getUploadForm() {
    return $('#upload-form');
  }
});
