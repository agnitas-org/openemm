(() => {

  const Def = AGN.Lib.MailingContent.Def;
  const DynTagEditor = AGN.Lib.MailingContent.DynTagEditor;
  const Storage = AGN.Lib.MailingContent.Storage;
  const DynTag = AGN.Lib.MailingContent.DynTag;
  const Template = AGN.Lib.Template;
  const Form = AGN.Lib.Form;

  class BlocksManager { // Mailing -> Content tab -> Content Blocks tab

    constructor(config) {
      this.config = config;
      this.dynTags = Object.entries(config.dynTags).map(entry => DynTag.create(entry[1]), []);
      this.interestGroups = [{column: '', shortname: t('mailing.default.interest_group_name')}, ..._.cloneDeep(config.interestGroupList)];
      this.drawDynTagsList();
      this.storage = new Storage(this);
      this.selectDynTagOnLoad();
    }

    get modified() {
      return this.dynTags.some(dynTag => dynTag.modified);
    }

    get isDirty() {
      return this.dynTags.some(dynTag => dynTag.isDirty);
    }

    get saveUrl() {
      return `/mailing/content/${this.config.mailingId}/save.action`;
    }

    drawDynTagsList() {
      Def.$dynTagsList.append(this.dynTags.map(this.generateDynTagBtn).join(''));
      this.dynTags.forEach(dynTag => dynTag.updatePreview());
    }

    generateDynTagBtn(dynTag) {
      return Template.text('dyn-tag-btn', {id: dynTag.id});
    }

    selectDynTagOnLoad() {
      Def.$dynTagsList.scrollTop(this.scrollTopBeforeSave || 0); // todo test scroll
      this.switchDynTag(this.editor?.currentDynTag?.id || this.dynTags?.[0]?.id || 0);
    }

    switchDynTagVisually(dynTagId) {
      const dynTag = this.getDynTagById(dynTagId);

      this.editor = new DynTagEditor(dynTag);

      this.displayContentBadges();
      dynTag.markActive();
    }

    cantSwitchDynTag(idToSwitch) {
      return !this.editor.currentDynTag?.isValid || this.editor.currentDynTag.id === idToSwitch;
    }

    switchDynTag(dynTagId) {
      if (!dynTagId) {
        return;
      }
      this.switchDynTagVisually(dynTagId);
    }

    switchContentBlock(targetId) {
      if (!this.editor.currentDynTag.isValid) {
        return;
      }
      const block = this.editor.currentDynTag.getContentBlockById(targetId);
      this.editor.selectContentBlock(block);
    }

    markAsClean() { // reset dirty state
      this.dynTags.forEach(dynTag => dynTag.markAsClean());
    }

    getDynTagById(id) {
      return this.dynTags.filter(dynTag => dynTag.id === id)[0];
    }

    search(searchStr) {
      Def.$dynTagsList.find('.dyn-tag').each(function () {
        const $block = $(this);
        const blockName = $block.find('small').text();
        $block.toggle(blockName.toLowerCase().includes(searchStr));
      });
      $('[data-blocks-not-found-msg]').toggle(!Def.$dynTagsList.find('.dyn-tag:visible').exists());
      if (searchStr.length > 0) {
        Def.$dynTagsList.scrollTop(0);
      }
    }

    setContentBadgesInContainer($container) {
      $container.find('.dyn-tag').each((i, dynTag) => this.setContentBadge($(dynTag)))
    }

    displayContentBadges() {
      const self = this;

      this.setContentBadgesInContainer(Def.$dynTagsList);

      Def.$dynTagsMobileSelect.on('select2:open', function () {
        const resultsContainer = $('#select2-mobile-dyn-tags-list-results')[0];
        const observer = new MutationObserver(() =>
          $(resultsContainer).children().each((i, el) => self.setContentBadgesInContainer($(el))));
        observer.observe(resultsContainer, {childList: true});
        Def.$dynTagsMobileSelect.on('select2:close', () => observer.disconnect());
      });
    }

    setContentBadge($dynTag) {
      const dynTagId = parseInt($dynTag.val());
      const empty = this.getDynTagById(dynTagId).isEmpty;
      $dynTag.find('.icon-file-alt').toggleClass('text-primary', !empty)
    }

    save() {
      if (this.dynTags.some(dynTag => !dynTag.isValid)) {
        return;
      }
      if (this.editor.aiEditor.isAiTextGeneratedAndNotApplied()) {
        return;
      }
      this.editor.applyLastChanges(true);
      this.saveDynTags();
    }

    saveDynTags() {
      this.scrollTopBeforeSave = Def.$dynTagsList.scrollTop();
      const data = JSON.stringify(this.#collectDynTagsToSave());
      $
        .post({url: AGN.url(this.saveUrl), contentType: 'application/json', data})
        .done(popups => {
          AGN.Lib.JsonMessages(popups, true);
          Form.get($('#preview-form')).submit(); // update mailing preview
          this.markAsClean();
        });
    }

    #collectDynTagsToSave() {
      const dynTagsToSave = _.cloneDeep(this.dynTags.filter(dynTag => dynTag.modified));
      dynTagsToSave.forEach(dynTag => dynTag.contentBlocks.forEach(cb => {
        if (!cb.content) {
          dynTag.remove(cb.uniqueId); // GWUA-6384: filter out empty content blocks
        }
      }));
      return dynTagsToSave;
    }
  }

  AGN.Lib.MailingContent.BlocksManager = BlocksManager;
})();
