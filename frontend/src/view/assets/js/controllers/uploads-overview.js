AGN.Lib.Controller.new('uploads-overview', function () {

  const Form = AGN.Lib.Form;

  function putFileProbesToForm(files) {
    const formData = new FormData();
    $.each(files, function (i, file) {
      const probe = new File(
        [file.slice(0, 10240)],
        file.name,
        {type: file.type}
      );
      formData.append("probes", probe);
    });
    return formData;
  }

  this.addAction({'upload:dropped': 'show-upload-form-modal'}, function () {
    const files = this.data;
    $
      .post({
        url: AGN.url('/upload/new.action'),
        data: putFileProbesToForm(files),
        processData: false,
        contentType: false,
      })
      .done(resp => {
        AGN.Lib.Page.render(resp);
        if ($(resp).all('.modal').exists()) {
          $('.modal').on('modal:close', clearDropzone);
          addFilesInfoToModal(files);
        } else {
          clearDropzone();
        }
      });
  });

  function addFilesInfoToModal(files) {
    if (files.length > 1) {
      addFilesTableToModal(files);
      return;
    }
    $('#file-names').append(files[0].name);
  }

  function clearDropzone() {
    $('#files').val('');
    AGN.Lib.Upload.get($('#dropzone')).reset();
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

  this.addAction({'table-column-manager:add': 'update-columns'}, function () {
    const form = AGN.Lib.Form.get(this.el);
    form.setValueOnce('additionalColumnNames', this.data.columns);
    form.setValueOnce('inEditColumnsMode', true);
    form.submit();
  });

  this.addAction({'table-column-manager:apply': 'update-columns'}, function () {
    const additionalColumnNames = this.data.columns;

    $.ajax({
      type: 'POST',
      url: AGN.url('/upload/list/setSelectedFields.action'),
      traditional: true,
      data: {additionalColumnNames}
    }).done(resp => {
      if (resp.success) {
        AGN.Lib.WebStorage.extend('upload-overview', {'fields': additionalColumnNames});
      }
      AGN.Lib.JsonMessages(resp.popups);
    });
  });


  function $getUploadForm() {
    return $('#upload-form');
  }
});
