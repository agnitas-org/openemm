(() => {
  const AutoSave = AGN.Lib.AutoSave;
  const DynContent = AGN.Lib.MailingContent.DynContent;

  class Storage {

    constructor(blocksManager) {
      this.blocksManager = blocksManager;
      AutoSave.initialize(`mailing/${this.blocksManager.config.mailingId}/content-blocks`,
        () => this.#collectForStorage(),
        dynTags => this.#isStorageAdded(dynTags),
        dynTags => this.#restoreFromStorage(dynTags));
    }

    #collectForStorage() {
      this.blocksManager.editor.applyLastChanges();
      return this.blocksManager.dynTags
        .filter(dynTag => dynTag.modified)
        .map(dynTag => dynTag.lightWeight);
    }

    #restoreFromStorage(storageDynTags) {
      storageDynTags.forEach(storageDynTag => {
        const currentDynTag = this.blocksManager.getDynTagById(storageDynTag.id)
        currentDynTag.interestGroup = storageDynTag.interestGroup;
        currentDynTag.contentBlocks = storageDynTag.contentBlocks.map(contentBlock => new DynContent(contentBlock));
      });
      this.blocksManager.editor.$el.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
      this.blocksManager.switchDynTag(storageDynTags[0].id);
    }

    #isStorageAdded(storageDynTags) {
      return storageDynTags.some(storageDynTag => {
        const currentDynTag = this.blocksManager.getDynTagById(storageDynTag.id)
        return this.#isStorageDynTagDiffers(currentDynTag, storageDynTags);
      });
    }

    #isStorageDynTagDiffers(currentDynTag, storageDynTag) {
      return storageDynTag.interestGroup !== currentDynTag.interestGroup
        || currentDynTag.contentBlocks.length !== storageDynTag.contentBlocks.length
        || currentDynTag.contentBlocks.some(currentBlock => {
          const storageBlock = storageDynTag.getContentBlockByTargetId(currentBlock.targetId);
          return !storageBlock || storageBlock.content !== currentBlock.content;
        });
    }
  }

  AGN.Lib.MailingContent.Storage = Storage;
})();
