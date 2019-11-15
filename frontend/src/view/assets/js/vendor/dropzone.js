/**
 * Source: https://github.com/buuum/Dropzone
 *
 * MIT License
 *
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
(function() {
  var Dropzone;

  Dropzone = (function() {
    function Dropzone($elements, $options) {
      var defaults;
      if ($options == null) {
        $options = [];
      }
      defaults = {
        script: null,
        ajaxQueue: false,
        autoUpload: false,
        classDragOver: 'is-dragover',
        onUploadingFiles: function(count, total) {},
        onSelectFiles: function(files, element) {},
        onSuccessFile: function(file, response, element) {},
        onComplete: function(files, element) {},
        onFileError: function(jqXHR, element) {},
        onFileFail: function(jqXHR, element) {}
      };
      this.options = $.extend(defaults, $options);
      this.files = [];
      $.each($($elements), (function(_this) {
        return function(i, el) {
          var element, file;
          element = $(el);
          file = element.find('input[type=file]');
          element.data('pos', i);
          file.on('change', function(e) {
            var droppedFiles;
            droppedFiles = $(e.currentTarget).prop("files");
            _this.options.onSelectFiles(droppedFiles, element);
            _this.files[element.data('pos')] = e.target.files;
            if (_this.options.autoUpload) {
              return _this.submit(element, e.target.files);
            }
          });
          return _this.initDrag(element);
        };
      })(this));
    }

    Dropzone.prototype.initDrag = function(box) {
      var droppedFiles;
      if (this.isAdvancedUpload()) {
        droppedFiles = false;
        return box.on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
          e.preventDefault();
          e.stopPropagation();
        }).on('dragover dragenter', (function(_this) {
          return function() {
            box.addClass(_this.options.classDragOver);
          };
        })(this)).on('dragleave dragend drop', (function(_this) {
          return function() {
            box.removeClass(_this.options.classDragOver);
          };
        })(this)).on('drop', (function(_this) {
          return function(e) {
            droppedFiles = e.originalEvent.dataTransfer.files;
            _this.options.onSelectFiles(droppedFiles, box);
            if (_this.options.autoUpload) {
              return _this.submit(box, droppedFiles);
            } else {
              return _this.files[box.data('pos')] = droppedFiles;
            }
          };
        })(this));
      }
    };

    Dropzone.prototype.startUpload = function(box) {
      var files;
      files = this.files[box.data('pos')];
      if (files) {
        return this.submit(box, files);
      }
    };

    Dropzone.prototype.submit = function(box, droppedFiles) {
      var ajax, file, inputname, j, key, len, myFormData, results, total_files, upload_files;
      if (box.hasClass('is-uploading')) {
        return false;
      }
      box.addClass('is-uploading');
      total_files = droppedFiles.length;
      upload_files = 1;
      inputname = box.find('input[type=file]').attr('name');
      this.options.onUploadingFiles(upload_files, total_files);
      results = [];
      for (key = j = 0, len = droppedFiles.length; j < len; key = ++j) {
        file = droppedFiles[key];
        myFormData = new FormData();
        myFormData.append(inputname + "[]", file);
        myFormData.append('position', key);
        ajax = this.options.ajaxQueue ? $.ajaxQueue : $.ajax;
        results.push(ajax({
          url: this.options.script,
          type: 'POST',
          data: myFormData,
          processData: false,
          contentType: false,
          cache: false,
          complete: (function(_this) {
            return function() {
              upload_files++;
              if (upload_files >= total_files + 1) {
                _this.options.onComplete(droppedFiles, box);
                _this.removeFiles(box);
                return box.removeClass('is-uploading');
              } else {
                return _this.options.onUploadingFiles(upload_files, total_files);
              }
            };
          })(this),
          success: (function(_this) {
            return function(data) {
              _this.options.onSuccessFile(file, data, box);
            };
          })(this),
          fail: (function(_this) {
            return function(jqXHR) {
              _this.options.onFileFail(jqXHR, box);
            };
          })(this),
          error: (function(_this) {
            return function(jqXHR) {
              _this.options.onFileError(jqXHR, box);
            };
          })(this)
        }));
      }
      return results;
    };

    Dropzone.prototype.removeFiles = function(box) {
      return this.files[box.data('pos')] = null;
    };

    Dropzone.prototype.isAdvancedUpload = function() {
      var div;
      div = document.createElement('div');
      return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window;
    };

    Dropzone.prototype.isFormData = function() {
      return 'FormData' in window;
    };

    Dropzone.prototype.isFileReader = function() {
      return 'FileReader' in window;
    };

    Dropzone.prototype.isDraggable = function() {
      var div;
      div = document.createElement('div');
      return ('draggable' in div) || ('ondragstart' in div && 'ondrop' in div);
    };

    return Dropzone;

  })();

  if (typeof module === 'undefined') {
    window.Dropzone = Dropzone;
  } else {
    module.exports = Dropzone;
  }

  return Dropzone;
}).call(this);
