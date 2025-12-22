AGN.Lib.Controller.new('temp-file', function () {

  const Form = AGN.Lib.Form;
  let modalAlreadyRequested;

  this.addAction({'upload:add': 'show-upload-file-form'}, function () {
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
      }
    });
  });

  this.addAction({'upload:dropped': 'addFilesToModal'}, function () {
    addFilesInfoToModal(this.data);
    modalAlreadyRequested = false;
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
        filter: false,
        suppressMovable: true,
        cellRenderer: 'NotEscapedStringCellRenderer'
      },
      {
        field: 'size',
        headerName: t('defaults.size'),
        filter: false,
        suppressMovable: true,
        width: 50,
        cellRenderer: 'NotEscapedStringCellRenderer'
      }
    ];
  }

  function getFilesDataForTable(files) {
    return files.map(({name, size}) => ({name, size: AGN.Lib.Helpers.formatBytes(size)}));
  }

  function addFilesTableToModal(files) {
    const table = new AGN.Lib.Table(
      AGN.Lib.Template.dom('js-table-wrapper'),
      getFilesTableColumnDefs(),
      getFilesDataForTable(files),
      {pagination: false}
    );

    const $tableWrapper = $('<div class="col-12">').append(table.$el);
    table.$el.css({
      '--ag-rows-count': files.length,
      'max-height': '235px'
    });
    $getUploadForm().find('.row:first').prepend($tableWrapper);
  }

  function $getUploadForm() {
    return $('#upload-form');
  }
});
