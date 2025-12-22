(() => {
  const DynContent = AGN.Lib.MailingContent.DynContent;
  const Def = AGN.Lib.MailingContent.Def;
  const Messages = AGN.Lib.Messages;
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;

  class DynTag {

    constructor(data) {
      this.id = data.id || 0;
      this.name = data.name;
      this.interestGroup = data.interestValue === undefined ? data.interestGroup : data.dynInterestGroup;
      this.contentBlocks = data.contentBlocks
        ? data.contentBlocks.map(block => new DynContent(block))
        : Object.entries(data.dynContent).map(entry => new DynContent(entry[1]));
      this.markAsClean();
    }

    get defaultContentBlock() {
      return {id: 0, index: 0, targetId: 0, content: ''}
    }

    // overridden in extended classes
    get isDirty() {
      return this.modified;
    }

    get modified() {
      const initiallyEmpty = !this.initState.contentBlocks.length;
      if (initiallyEmpty && this.isEmpty && this.contentBlocks.length === 1 && this.contentBlocks[0].targetId === 0) {
        return false; // default 'all targets' group added on the first edit
      }
      return !_.isEqual(this.initState, this.lightWeight);
    }

    get isHtmlVersionDynTag() {
      return this.name === 'HTML-Version';
    }

    get isHtmlDynTag() {
      return Def.htmlDyntagNames?.includes(this.name);
    }

    get lightWeight() {
      return {
        id: this.id,
        interestGroup: this.interestGroup,
        contentBlocks: this.contentBlocks.map(contentBlock => contentBlock.lightWeight),
      }
    }

    get $btn() {
      return $(`.dyn-tag[data-dyn-tag="${this.id}"]`);
    }

    get isEmpty() {
      return this.contentBlocks.every(block => !block.content);
    }

    get isValid() {
      const errors = this.#collectErrors();
      errors?.forEach(error => Messages.alert(error));
      return !errors?.length;
    }

    get templateOpts() {
      return {
        dynTagName: this.name,
        isFullHtmlTags: this.isHtmlVersionDynTag,
        showHTMLEditor: this.isHtmlDynTag,
        usedInSmsContent: Def.smsDyntagNames?.includes(this.name),
        isEditableMailing: Def.isEditableMailing || Def.isTextModulesPage,
        isContentGenerationAllowed: Def.isContentGenerationAllowed
      };
    }

    /**
     * After content is set, WYSIWYG makes some automatic changes (e.g. change umlauts to unicode).
     * These changes need to be applied in order to prevent false dirty state.
     */
    applyWysiwygInitialChanges() {
      const currentBlock = Def.editor.currentContentBlock;
      if (!currentBlock.initWysiwygChangesApplied) {
        Def.editor.applyLastChanges(true);
        if (this.initState.contentBlocks.length) {
          this.initState.contentBlocks.find(block => block.id === currentBlock.id).content = currentBlock.content;
        }
        currentBlock.initWysiwygChangesApplied = true;
        this.afterInitialWysiwygChangesApplied();
      }
    }

    afterInitialWysiwygChangesApplied() {
      // overridden in extended classes
    }

    markAsClean() { // reset dirty state
      this.initState = _.cloneDeep(this.lightWeight);
    }

    getContentBlockByTargetId(targetId) {
      return this.contentBlocks.filter(block => block.targetId === targetId)[0];
    }

    getContentBlockById(id) {
      return this.contentBlocks.filter(block => block.uniqueId === id)[0];
    }

    createNewContentBlock(targetId = 0) {
      const newContentBlock = new DynContent(this.defaultContentBlock);
      newContentBlock.index = this.contentBlocks.length;
      newContentBlock.targetId = targetId;
      this.contentBlocks.push(newContentBlock);
      this.recalculateIndexes();
      return newContentBlock;
    }

    hasEmptyContent() {
      return !this.contentBlocks.length || this.contentBlocks.every(c => c.content === '');
    }

    getUsedTargetGroups() {
      return this.contentBlocks.map(cb => cb.targetId);
    }

    changeTargetGroup(contentBlockId, newTargetGroupId) {
      const contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.targetId = newTargetGroupId;
      return contentBlock;
    }

    changeContent(contentBlockId, newContent) {
      const contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.content = newContent || '';
      return contentBlock;
    }

    changeOrder(newOrder) {
      const func = function (self) {
        return function (id) {
          return self.getContentBlockById(parseInt(id));
        };
      };
      this.contentBlocks = newOrder.map(func(this));
      this.recalculateIndexes();
    }

    remove(id) {
      const currentContentBlock = this.getContentBlockById(id);
      const index = this.contentBlocks.indexOf(currentContentBlock);
      if (index > -1) {
        this.contentBlocks.splice(index, 1);
        this.recalculateIndexes();
      }
    }

    recalculateIndexes() {
      this.contentBlocks.forEach((contentBlock, index) => contentBlock.index = index + 1);
    }

    #collectErrors() {
      const errors = [];
      const targetIds = this.contentBlocks.map(block => block.targetId);
      if (new Set(targetIds).size !== targetIds.length) { // check for duplications
        errors.push("mailing.validation.target_group_duplicated");
      }
      if (targetIds.includes(0) && _.last(targetIds) !== 0) { // check for 'all recipients' target at the very end
        errors.push("mailing.validation.all_recipients_not_last");
      }
      if (this.isHtmlDynTag && this.contentBlocks.find(content => this.#containsIllegalElements(content))) {
        errors.push("fields.error.illegal_script_element");
      }
      return errors;
    }

    #containsIllegalElements(content) {
      return new DOMParser().parseFromString(content, 'text/html').querySelector('script');
    }

    updatePreview() {
      this.$btn.html(Template.text('dyn-tag-preview', this.#getPreviewOpts()));
      Def.$dynTagsMobileSelect // update preview in mobile select
        .find(`option[data-id="${this.id}"]`)
        .attr('data-dyn-tag-data', JSON.stringify(this.#getPreviewOpts()));
    }

    #getPreviewOpts() {
      return {
        name: this.name,
        content: this.contentBlocks?.[0]?.content,
        hasEmptyContent: this.hasEmptyContent(),
        targets: this.getUsedTargetGroups()
          .map(targetId => this.#getTargetNameById(targetId))
          .join('; ')
      };
    }

    #getTargetNameById(targetId) {
      return (Def.availableTargetGroups.find(target => target.id === targetId)
        || Def.availableTargetGroups[0]).targetName;
    }

    markActive() {
      Select.get(Def.$dynTagsMobileSelect).selectValue(this.id);
      $('.dyn-tag').removeClass('active');
      $(`.dyn-tag[data-dyn-tag="${this.id}"]`).addClass('active');
    }

    static create(data) {
      const name = data.dynName === undefined ? data.name : data.dynName;
      return name === 'HTML-Version' ? new AGN.Lib.MailingContent.HtmlVersionDynTag(data) : new DynTag(data);
    }
  }

  AGN.Lib.MailingContent.DynTag = DynTag;
})();
