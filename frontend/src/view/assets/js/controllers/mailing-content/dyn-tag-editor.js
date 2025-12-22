(() => {
  const TargetsManager = AGN.Lib.MailingContent.TargetsManager;
  const AiEditor = AGN.Lib.MailingContent.AiEditor;
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  const Messaging = AGN.Lib.Messaging;
  const Def = AGN.Lib.MailingContent.Def;

  class DynTagEditor {

    constructor(dynTag) {
      Def.editor = this;

      if (!Def.isTextModulesPage) {
        Messaging.resubscribe('wysiwyg-shown', () => this.applyWysiwygInitialChanges());
      }

      this.$el = Template.dom('content-editor-template', dynTag.templateOpts);
      Def.$dynTagSettings.html(this.$el).on('tile:show', () => this.resizeAceIfShown());
      this.currentDynTag = dynTag;
      this.currentContentBlock = undefined;
      this.currentDynTag.currentContentBlock = this.currentContentBlock;

      this.selectContentBlock(this.currentDynTag.contentBlocks[0] || this.currentDynTag.createNewContentBlock());
      AGN.runAll(this.$el);
      this.#initInterestGroup();

      this.targetsManager = new TargetsManager(this);
      this.aiEditor = new AiEditor(this);
      AGN.Lib.Controller.init(Def.$dynTagSettings); // "ai-text-generation" controller init
    }

    get $interestGroup() {
      return $('[data-action="change-interest-group"]');
    }

    get aceEditor() {
      return AGN.Lib.Editor.get($('#content'));
    }

    get isAceTabActive() {
      return this.$activeTab.is(this.$aceTab);
    }

    get isWysiwygTabActive() {
      return this.$activeTab.is(this.$wysiwygTab);
    }

    get $aceTab() {
      return $('#tab-content-html');
    }

    get $wysiwygTab() {
      return $('#tab-content-wysiwyg');
    }

    get $activeTab() {
      return $(Def.$dynTagSettings.find('[data-toggle-tab].active').data('toggle-tab'));
    }

    resizeAceIfShown() {
      if (this.isAceTabActive) {
        this.aceEditor?.resize();
      }
    }

    applyWysiwygInitialChanges() {
      if (window.Jodit) {
        setTimeout(() => this.currentDynTag.applyWysiwygInitialChanges(), 200);
      } else {
        CKEDITOR.instances['content']?.on('instanceReady', () => setTimeout(() => {
          this.currentDynTag.applyWysiwygInitialChanges()
        }, 200));
      }
    }

    #initInterestGroup() {
      AGN.Lib.CoreInitializer.run('select', this.$interestGroup.val(this.currentDynTag.interestGroup));
    }

    applyLastChanges(preventEditorHide) {
      this.setContentFromEditorToBlock();
      if (!preventEditorHide) {
        this.$el.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
      }
    }

    selectContentBlock(contentBlock) {
      this.setContentFromEditorToBlock();
      this.setEditorsContent(contentBlock === undefined ? '' : contentBlock.content);
      this.currentContentBlock = contentBlock;
      this.updateTargetsOptions();
    }

    updateTargetsOptions() {
      const $targets = Template.dom('mailing-content-select-template', {dynTag: this.currentDynTag});
      Select.get($('[data-action="switch-content-block"]')).replaceWith($targets);
      $('#content-block-index').text(this.currentContentBlock.index);
      if (this.currentContentBlock) {
        Select.get($targets).selectValue(this.currentContentBlock.uniqueId);
      }
    }

    setEditorsContent(content) {
      if (!this.$activeTab.exists()) { // sometimes last chosen editor is hidden, so we need to select alternative
        $(Def.$dynTagSettings.find('[data-toggle-tab]:first').addClass('active').data('toggle-tab'));
      }
      this.$el.trigger('tile:hide');
      if (this.isWysiwygTabActive) {
        this.$el.find('.js-wysiwyg').val(content);
      } else {
        this.aceEditor.val(content);
      }
      this.$el.trigger('tile:show');
    }

    getContentFromEditors() {
      const $wysiwygEditorBlock = $('#tab-content-wysiwyg');
      const $htmlEditorBlock = $('#contentEditor');
      const $aiEditorBlock = $('#tab-content-ai-text-generation');

      if ($aiEditorBlock.is(":visible")) {
        return $wysiwygEditorBlock.exists()
          ? $('#content').val()
          : this.aceEditor.val();
      }
      if ($wysiwygEditorBlock.is(":visible")) {
        if (window.Jodit) {
          return Jodit.instances["content"].value.replace(/[\r\n]?\s*<style[^>]*class=["']?jodit[^>]*>[\s\S]*?<\/style>/gi, ''); // filter out Jodit UI elements
        } else {
          return CKEDITOR.instances['content'].getData();
        }
      }
      if ($htmlEditorBlock.is(":visible")) {
        return this.aceEditor.val();
      }
    }

    setContentFromEditorToBlock() {
      if (!this.currentContentBlock) {
        return;
      }
      const content = this.getContentFromEditors();
      if (content !== undefined) {
        this.currentDynTag.changeContent(this.currentContentBlock.uniqueId, content);
        this.currentDynTag.updatePreview();
      }
    }
  }

  AGN.Lib.MailingContent.DynTagEditor = DynTagEditor;
})();
