AGN.Lib.Controller.new('messenger-settings', function() {
  var attachment = null;

  this.addDomInitializer('messenger-settings', function() {
    updateContentLengthIndicator();

    var $content = $('#messageContent');
    var editor = $content.data('emojioneArea');

    if (editor) {
      editor.on('change input', _.throttle(function() {
        updateContentLengthIndicator(this.getText().length);
      }, 100));
    } else {
      $content.on('change input', _.throttle(function() {
        updateContentLengthIndicator();
      }, 100));
    }

    attachment = this.config.attachment;
  });

  function updateContentLengthIndicator(length) {
    if (length == null) {
      length = $('#messageContent').val().length;
    }

    $('#contentSize').text(t('fields.content.charactersEntered', length));
  }

  function isImageMimeType(mime) {
    if (mime) {
      return mime == 'image' || mime.startsWith('image/');
    }

    return false;
  }

  function preloadAttachment() {
    var deferred = $.Deferred();

    if ($('#typeBinary').is(':checked')) {
      var files = $('#file').prop('files');
      if (files && files.length) {
        var file = files[0];

        if (isImageMimeType(file.type)) {
          var reader = new FileReader();
          reader.onload = function(e) {
            AGN.Lib.Loader.hide();
            deferred.resolve({type: 'image'}, e.target.result);
          };
          reader.onerror = reader.onabort = function() {
            AGN.Lib.Loader.hide();
          };
          AGN.Lib.Loader.show();
          reader.readAsDataURL(file);
        } else {
          deferred.resolve({
            type: 'custom',
            name: file.name,
            size: AGN.Lib.Helpers.formatBytes(file.size, 'IEC'),
            ext: getFilenameExtension(file.name).toUpperCase()
          });
        }
      } else {
        if (attachment && attachment.size > 0) {
          if (isImageMimeType(attachment.mime)) {
            deferred.resolve({type: 'image'}, attachment.attachmentLink);
          } else {
            deferred.resolve({
              type: 'custom',
              name: attachment.filename,
              size: AGN.Lib.Helpers.formatBytes(attachment.size, 'IEC'),
              ext: (attachment.extension || '').toUpperCase()
            });
          }
        } else {
          deferred.resolve(false);
        }
      }
    } else if ($('#typeUrl').is(':checked')) {
      var url = $.trim($('#url').val());
      if (url) {
        deferred.resolve({type: 'image'}, url);
      } else {
        deferred.resolve(false);
      }
    } else {
      deferred.resolve(false);
    }

    return deferred.promise();
  }

  function getFilenameExtension(name) {
    if (name) {
      var pos = name.lastIndexOf('.');
      if (pos >= 0) {
        var ext = name.substring(pos + 1, name.length);
        if (ext.length > 5) {
          return ext.substr(0, 3) + '…';
        } else {
          return ext;
        }
      }
    }

    return '';
  }

  this.addAction({
    click: 'preview'
  }, function() {
    preloadAttachment()
      .done(function(attachment, value) {
        var text = $('#messageContent').val();
        var date = new Date();
        var time = AGN.Lib.Helpers.pad(date.getHours(), 2) + ':' + AGN.Lib.Helpers.pad(date.getMinutes(), 2);

        if (text && $.trim(text) || attachment) {
          var $modal = AGN.Lib.Modal.createFromTemplate({content: text, time: time, attachment: attachment}, 'preview-modal');

          if (attachment && attachment.type == 'image' && value) {
            $modal.find('#attachment-image').prop('src', value);
          }
        } else {
          AGN.Lib.Messages(t('defaults.warning'), t('error.content.empty'), 'warning');
        }
      });
  });
});
