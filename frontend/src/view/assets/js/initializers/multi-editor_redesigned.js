AGN.Lib.CoreInitializer.new('multi-editor', function ($scope = $(document)) {
  const Helpers = AGN.Lib.Helpers;
  const Modal = AGN.Lib.Modal;
  const FormBuilder = AGN.Lib.FormBuilder.FormBuilder;
  const Editor = AGN.Lib.Editor;

  class MultiEditor {
    constructor($el) {
      const multiEditor = $el.data('_multi_editor');
      if (multiEditor) {
        return multiEditor; // return the existing object
      }
      this.$el = $el;
      this.$container = $el.parent();
      this.#init();
    }

    #init() {
      this.$resizeBtn.on('click', () => this.resize());
      this.updateResizeBtn();
    }

    resize() {
      if (this.enlarged) {
        this.apply();
      } else {
        this.enlarge();
      }
      this.updateResizeBtn();
    }

    enlarge() {
      this.toggleWysiwygIfSelected(false);
      this.createBackup();
      this.showModal();
      this.toggleWysiwygIfSelected(true);
    }

    createBackup() {
      this.backup = {};
      _.each(this.editor$, textarea =>
        this.backup[$(textarea).attr('id')] = Editor.get($(textarea)).val());
      _.each(this.$el.find('.js-form-builder'), textarea =>
        this.backup[$(textarea).attr('id')] = FormBuilder.get(textarea).getJson());
    }

    showModal() {
      const template = this.$resizeBtn.data('enlarged-modal') || 'multi-editor-modal';
      const opts = Helpers.objFromString(this.$resizeBtn.data('modal-set'));
      this.$modal = Modal.fromTemplate(template, opts);
      this.$placeholder.replaceWith(this.$el);
      this.$applyBtn.on('click', () => this.resize());
      this.$modal.on('hide.bs.modal', () => this.hideModal()); // close or backdrop click
      this.$el.trigger('enlarged');
      Editor.get(this.editor$).resize();
    }

    hideModal() {
      if (!this.applyChanges) {
        this.cancel();
      }
      this.applyChanges = false
      this.updateResizeBtn();
    }

    cancel() {
      this.toggleWysiwygIfSelected(false)
      this.restore();
      this.$container.append(this.$el);
      this.toggleWysiwygIfSelected(true)
    }

    restore() {
      Object.entries(this.backup).forEach(([id, val]) => {
        if (FormBuilder.isCreated(`#${id}`)) {
          FormBuilder.get("#" + id).setJson(val);
        } else {
          this.$el.find(`#${id}`).data('_editor')?.val(val);
        }
      });
    }

    apply() {
      this.applyChanges = true;
      this.toggleWysiwygIfSelected(false);
      Modal.getInstance(this.$modal).hide();
      this.$container.append(this.$el);
      this.toggleWysiwygIfSelected(true);
    }
    
    toggleWysiwygIfSelected(show) {
      if (this.wysiwygSelected) {
        this.$el.trigger(show ? 'tile:show' : 'tile:hide');
      }
    }
    
    get editor$() {
      return this.$el.find('.js-editor');
    }
    
    get wysiwygSelected() {
      return this.$el.find('[data-multi-editor-option="wysiwyg"]').is('.active');
    }

    get enlarged() {
      return this.$el.closest('.modal').length > 0;
    }

    get $resizeBtn() {
      return this.$el.find('[data-enlarged-modal]');
    }
    
    get $applyBtn() {
      return this.$modal?.find('[data-apply-enlarged]');
    }
    
    get $placeholder() {
      return this.$modal?.find('.modal-body [data-placeholder]');
    }

    updateResizeBtn() {
      this.$resizeBtn.find('i')
        .toggleClass('icon-expand-arrows-alt', !this.enlarged)
        .toggleClass('icon-compress-arrows-alt', this.enlarged);
      AGN.Lib.Tooltip.remove(this.$resizeBtn);
      AGN.Lib.Tooltip.createTip(this.$resizeBtn, this.enlarged ? '' : t('defaults.enlargeEditor'));
    }
  }

  _.each($scope.find('[data-multi-editor]'), function (el) {
    new MultiEditor($(el));
  });
})
