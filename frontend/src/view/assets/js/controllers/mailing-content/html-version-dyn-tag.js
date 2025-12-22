(() => {
  const DynTag = AGN.Lib.MailingContent.DynTag;
  const Helpers = AGN.Lib.Helpers;
  const Def = AGN.Lib.MailingContent.Def;

  class HtmlVersionDynTag extends DynTag {

    constructor(data) {
     super(data);
    }

    // Override
    afterInitialWysiwygChangesApplied() {
      const currentBlock = Def.editor.currentContentBlock;
      if (window.Jodit) {
        Jodit.instances['content']?.events.on('finishedCleanHTMLWorker', () => setTimeout(() => {
          if (!Jodit.instances['content'].userInputHappened && !currentBlock.isWysiwygHtmlCleanerChangesApplied) {
            this.#applyWysiwygHtmlCleanerChanges()
          }
        }, 0));
      }
    }

    /**
     * GWUA-6254: After initial WYSIWYG editor changes (see initWysiwygChangesApplied flag),
     * WYSIWYG makes automatic HTML cleaner changes.
     * This may take some time depending on the size of the content.
     * These changes need to be applied in order to prevent false dirty state.
     */
    #applyWysiwygHtmlCleanerChanges() {
      const currentBlock = Def.editor.currentContentBlock;
      if (currentBlock.isWysiwygHtmlCleanerChangesApplied) { // initial WYSIWYG changes already marked as clean
        return;
      }
      Def.editor.applyLastChanges(true);
      this.initState.contentBlocks.find(block => block.id === currentBlock.id).content = currentBlock.content;
      currentBlock.isWysiwygHtmlCleanerChangesApplied = true;
    }

    // override
    get isDirty() {
      if (!super.isDirty) {
        return false;
      }
      const beforeClone = _.omit(this.initState, 'contentBlocks');
      const afterClone = _.omit(this.lightWeight, 'contentBlocks');
      return !_.isEqual(beforeClone, afterClone)
        || !this.#contentBlocksEquals(this.initState.contentBlocks, this.lightWeight.contentBlocks);
    }

    #contentBlocksEquals(before, after) {
      if (before.length !== after.length) {
        return false
      }
      return _.every(before, (b, i) => this.#contentBlockEquals(b, after[i]));
    }

    // GWUA-6254: The WYSIWYG editor makes automatic changes (e.g. puts content into an <html> template)
    // These changes needs to be ignored in order to prevent false positive dirty state
    #contentBlockEquals(first, second) {
      const firstClone = _.pick(first, ["id", "targetId"]);
      const secondClone = _.pick(second, ["id", "targetId"]);
      return _.isEqual(firstClone, secondClone) && this.#strEqualsIgnoreHtml(first.content, second.content);
    }

    #strEqualsIgnoreHtml(before, after) {
      if (!Helpers.isHtml(before) && Helpers.isHtml(after)) { // switch from HTML editor to WYSIWYG editor
        return _.isEqual(this.#toStringIgnoreHtml(before), this.#toStringIgnoreHtml(after));
      }
      const normalize = html => html
        .replace(/<body([^>]*)\sspellcheck="false"([^>]*)>/gi, '<body$1$2>') // ignore spellcheck added by jodit
        .replace(/\s+/g, ' ') // collapse all whitespace to single space (maybe not needed after ck editor removed)
        .replace(/>\s+</g, '><')                                             // remove spaces between tags
        .trim();
      return _.isEqual(normalize(before), normalize(after));
    }

    // Jodit will add default HTML template. So we need to mark this change as clean.
    createNewContentBlock(targetId = 0) {
      const block = super.createNewContentBlock(targetId);
      this.initState.contentBlocks.push(_.cloneDeep(block));
      return block;
    }

    #toStringIgnoreHtml(input) {
      return $('<div>').html(input).text().trim();
    }
  }

  AGN.Lib.MailingContent.HtmlVersionDynTag = HtmlVersionDynTag;
})();
