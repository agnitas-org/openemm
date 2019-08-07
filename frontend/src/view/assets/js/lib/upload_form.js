(function(){

  var UploadForm,
      Form = AGN.Lib.Form;

  UploadForm = function($form) {
    Form.apply(this, [$form]);

    // necessary for preventing adding a redundant field to form data.
    this.isMultipart = false;

    this.count = 0;
    this.fileData = [];

    this.$form.fileupload({
      progressInterval: 100,
      bitrateInterval: 500,
      dataType: 'string',
      autoupload: false,
      dropzone: this.$form.find('.dropzone'),
      add: _.bind(this.fileUploadAdd, this),
      progress: _.bind(this.fileUploadProgress, this),
      done:  _.bind(this.fileUploadDone, this)
    });

  };

  UploadForm.prototype = Object.create(Form.prototype);
  UploadForm.prototype.constructor = UploadForm;

  UploadForm.prototype.fileUploadAdd = function(e, data) {
    var self = this;
    var config = this._uploadConfig().add;

    data.count = (this.count += 1);
    data.filename = data.files[0].name;
    data.size = data.files[0].size;
    if (/^image/.test(data.files[0].type)){
      var reader = new FileReader();
      reader.readAsDataURL(data.files[0]);

      reader.onloadend = function(){
        data.preview = this.result;

        template = _.template(config.template, data);

        config.container.removeClass('hidden');
        data.context = $(template).appendTo(config.target);
        AGN.runAll(data.context);
        self.initFields();
      }

    } else {
      data.preview = undefined;

      template = _.template(config.template, data);

      config.container.removeClass('hidden');
      data.context = $(template).appendTo(config.target);
      AGN.runAll(data.context);
      self.initFields();
    }

    this.fileData = this.fileData.concat(data.files);
  };

  UploadForm.prototype.fileUploadProgress = function(e, data) {
    var config = this._uploadConfig(),
        currentProgress = data.progress();

    data.currentProgress = parseInt(100 * currentProgress.loaded / currentProgress.total);

    template = _.template(config.progress.template, data);

    config.add.container.addClass('hidden');
    config.progress.container.removeClass('hidden');
    config.progress.target.html(template);
  };

  UploadForm.prototype.fileUploadDone = function(e, data) {
    this.reset();
  };

  UploadForm.prototype.jqxhr = function() {
    var jqxhr;

    jqhxr = this.$form.fileupload('send', {
      paramName: this._fileParamName(),
      dataType: 'text',
      files: this.fileData,
      form: this.$form,
      formData: this.data(),
      loader: this.loader()
    });

    this._jqxhr = jqhxr;

    return jqhxr;
  };

  UploadForm.prototype.reset = function() {
    var uploadConfig = this._uploadConfig();

    this.$form.trigger('reset');
    this.count = 0;
    this._dataNextRequest = {};
    this.fileData = [];

    uploadConfig.progress.target.empty();
    uploadConfig.progress.container.addClass('hidden');

    uploadConfig.add.target.empty();
    uploadConfig.add.container.addClass('hidden');
  };

  UploadForm.prototype.abort = function() {
    var uploadConfig = this._uploadConfig();

    this._jqxhr.abort();

    uploadConfig.progress.target.empty();
    uploadConfig.progress.container.addClass('hidden');
    uploadConfig.add.container.removeClass('hidden');
  };

  // determine the Names for the fileParams
  UploadForm.prototype._fileParamName = function() {
    var fieldName = this._getFileField().attr('name'),
        frameworkType = this._getFileField().data('upload'),
        hasMultipleFiles = this._getFileField().attr('multiple');

    if(hasMultipleFiles) {
      return _.map(this.fileData, function(file, index) {
        // todo: remove this clause when will be removed corresponding line in upload_image.jsp;
        if(frameworkType === 'mvc') {
          return fieldName.replace(/\[\]/, '');
        }

        return fieldName.replace(/\[\]/, '[' + (index + 1) + ']');
      })
    }

    return fieldName;
  };

  UploadForm.prototype._getFileField = function() {
    return this.$form.find('[data-upload]');
  };

  UploadForm.prototype._uploadConfig = function() {
    return {
      add: {
        container: this.$form.find('[data-upload-add]'),
        target: this.$form.find('[data-upload-add-template]'),
        template: AGN.Opt.Templates[this.$form.find('[data-upload-add-template]').data('upload-add-template')]
      },
      progress: {
        container: this.$form.find('[data-upload-progress]'),
        target: this.$form.find('[data-upload-progress-template]'),
        template: AGN.Opt.Templates[this.$form.find('[data-upload-progress-template]').data('upload-progress-template')]
      }
    }
  };

  UploadForm.prototype._submit = function() {
    var jqxhr,
        self = this;

    this.setLoaderShown(true);
    jqxhr = this.jqxhr();

    jqxhr.done(function(resp) {
      self.setLoaderShown(false);
      self.updateHtml(resp);
    });

    return jqxhr;
  };

  UploadForm.prototype.updateHtml = function(resp) {
    AGN.Lib.Page.render(resp);
  };

  AGN.Lib.UploadForm = UploadForm;
  AGN.Opt.Forms['upload'] = UploadForm;

})();
