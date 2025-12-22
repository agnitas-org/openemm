(() => {

  class Upload {
    constructor($dropzone, $file, parentUpload) {
      const self = this;

      this.multiple = !$file.exists() || $file.is('[multiple]');
      this.selection = [];
      this.$dropzone = $dropzone;
      this.$file = $file;

      if ($dropzone.exists()) {
        $dropzone.data('agn:upload', this);
        new Dropzone($dropzone, {
          classDragOver: 'drag-over',
          onSelectFiles: function (files) {
            const instance = parentUpload ? parentUpload : self;
            instance.#drop(files);
          }
        });
      } else {
        $file.data('agn:upload', this);
        $file.on('change', () => {
          const instance = parentUpload ? parentUpload : self;
          instance.#drop($file.prop('files'));
        });
      }
    }

    static get($dropzone, $file) {
      if ($dropzone.exists()) {
        return $dropzone.data('agn:upload');
      }

      return $file.data('agn:upload');
    }

    reset() {
      this.selection = [];
      this.$file.val('');
    }

    removeSelection(index) {
      this.selection.splice(index, 1);
    }

    getSelection() {
      return this.selection.slice(0);
    }

    #drop(files) {
      if (!files || files.length <= 0) {
        return;
      }
      let added = false;

      if (this.multiple) {
        for (let i = 0; i < files.length; i++) {
          const file = files[i];

          if (this.#onDrop(file, this.selection.length)) {
            this.selection.push(file);
            added = true;
          }
        }
      } else {
        let file = files[0];
        // Replace previous selection in a single file mode.
        if (this.#onDrop(file, 0)) {
          this.selection = [file];
          added = true;
        }
      }

      if (added) {
        this.$dropzone.trigger('upload:dropped', [this.selection]);
      }
    }

    #onDrop(file, index) {
      const event = $.Event('upload:add');
      const $el = this.$dropzone.exists() ? this.$dropzone : this.$file;
      $el.trigger(event, {file: file, index: index});
      return !event.isDefaultPrevented();
    }
  }

  AGN.Lib.Upload = Upload;

})();
