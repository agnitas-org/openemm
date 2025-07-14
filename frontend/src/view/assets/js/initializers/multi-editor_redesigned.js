/*doc
---
title: Multi Editor
name: multi-editor
category: Components - Multi Editor
---

Sometimes user can use different <a href="components_-_code_editors.html">code editors</a> in combination (for example, ace, wysiwyg, form-builder, ai generation).
In order to simplify the synchronization of the result between switching editors, you can use `[data-multi-editor]`.

Also using the internal `[data-enlarged-modal]` it is possible to enlarge the multi editor in a fullscreen modal.
The multi editor code will synchronize changes from the enlarged modal to the original one when collapsing or using the "Apply" button,
or will cancel changes if the modal is closed.
Use `[data-modal-set]` to pass some options to enlarged modal.

```htmlexample
<div class="tile" data-multi-editor>
    <div class="tile-header">
        <ul class="tile-title-controls gap-1">
            <li>
                <a href="#" class="btn btn-icon btn-secondary active" data-toggle-tab="#first-editor-tab">
                    <i class="icon icon-code"></i>
                </a>
            </li>
            <li>
                <a href="#" class="btn btn-icon btn-secondary" data-multi-editor-option="wysiwyg" data-toggle-tab="#second-editor-tab">
                    <i class="icon icon-font"></i>
                </a>
            </li>
            <li>
                <a href="#" class="btn btn-icon btn-icon--wide btn-secondary" data-toggle-tab="#third-editor-tab">
                    <i class="icon icon-table"></i>
                    <i class="icon icon-flask"></i>
                </a>
            </li>
        </ul>
        <div class="tile-controls">
            <a href="#" class="btn-enlarge" data-enlarged-modal data-modal-set="title: 'Title from [data-modal-set]'">
               <i class="icon icon-expand-arrows-alt"></i>
            </a>
        </div>
    </div>
    <div class="tile-body">
        <div id="first-editor-tab" class="bg-success p-3 h-100"><textarea id="first-editor" class="form-control">First editor here</textarea></div>
        <div id="second-editor-tab" class="bg-warning p-3 h-100"><textarea id="second-editor" class="form-control">Second editor here</textarea></div>
        <div id="third-editor-tab" class="bg-info p-3 h-100"><textarea id="third-editor" class="form-control">Third editor here</textarea></div>
    </div>
</div>
```
*/

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

      this.$target = $el.find('[data-enlarge-target]');
      if (!this.$target.exists()) {
        this.$target = this.$el;
      }
      this.$container = this.$target.parent();
      this._isQbEditor = this.$target.find('#queryBuilderRules').exists();

      this.#init();
    }

    static defaultOptions() {
      return {btnText: t('defaults.apply')};
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
      Editor.get(this.editor$)?.resize();
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
      _.each(this.$el.find('#queryBuilderRules'), qbInput => {
        const api = $(qbInput).parent().prop('queryBuilder');
        this.backup['queryBuilderRules'] = api.getRules({allow_invalid: true, skip_empty: false});
      });
    }

    showModal() {
      const template = this.$resizeBtn.data('enlarged-modal') || 'multi-editor-modal';
      const opts = Helpers.objFromString(this.$resizeBtn.data('modal-set'));
      this.$modal = Modal.fromTemplate(template, _.extend(MultiEditor.defaultOptions(), opts));
      this.$placeholder.replaceWith(this.$target);
      if (this._isQbEditor) {
        AGN.Lib.CoreInitializer.run('select', this.$target);
      }
      this.$applyBtn.on('click', () => this.resize());
      this.$modal.on('hide.bs.modal', () => this.hideModal()); // close or backdrop click
      this.$el.trigger('enlarged');
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
      this.$container.append(this.$target);
      if (this._isQbEditor) {
        AGN.Lib.CoreInitializer.run('select', this.$target);
      }
      this.toggleWysiwygIfSelected(true)
    }

    restore() {
      Object.entries(this.backup).forEach(([id, val]) => {
        const selector = `#${id}`;

        if (id === 'queryBuilderRules') {
          $(selector).parent().prop('queryBuilder')?.setRules(val);
        } else if (FormBuilder.isCreated(selector)) {
          FormBuilder.get(selector).setJson(val);
        } else {
          this.$el.find(selector).data('_editor')?.val(val);
        }
      });
    }

    apply() {
      this.applyChanges = true;
      this.toggleWysiwygIfSelected(false);
      Modal.getInstance(this.$modal).hide();
      this.$container.append(this.$target);
      if (this._isQbEditor) {
        AGN.Lib.CoreInitializer.run('select', this.$target);
      }
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
      return this.$target.closest('.modal').length > 0;
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

    // TODO check if resize btn still exists anywhere and remove after ux redesign finished
    updateResizeBtn() {
      if (!this.$resizeBtn.exists()) {
        return;
      }
      this.$resizeBtn.find('i')
        .toggleClass('icon-expand-arrows-alt', !this.enlarged)
        .toggleClass('icon-compress-arrows-alt', this.enlarged);
      AGN.Lib.Tooltip.remove(this.$resizeBtn);
      AGN.Lib.Tooltip.createTip(this.$resizeBtn, this.enlarged ? '' : t('defaults.enlargeEditor'));
    }
  }

  _.each($scope.find('[data-multi-editor]'), el => new MultiEditor($(el)));
})
