AGN.Lib.Controller.new('mailing-content-controller', function () {
  const LEAVE_QUESTION = t('defaults.leaveQuestion');

  const Template = AGN.Lib.Template;
  const Messages = AGN.Lib.Messages;
  const AutoSave = AGN.Lib.AutoSave;
  const Storage = AGN.Lib.Storage;
  const Select = AGN.Lib.Select;
  const Form = AGN.Lib.Form;

  let mailingContent;
  let config;
  
  let $dynTagSettings;
  let $dynTagEditor;
  let seenDynTags;

  this.addDomInitializer('mailing-content-initializer', function () {
    config = this.config;
    seenDynTags = [];
    $dynTagSettings = $('#dyn-tag-settings');
    mailingContent = new MailingContent(config.dynTags, config.targetGroupList, config.interestGroupList);
    selectLastImportedContentSource();
    AutoSave.initialize(`mailing/${config.mailingId}/content-blocks`, collectForStorage, isStorageAdded, restoreFromStorage);
    switchDynTag($('.dyn-tag:first').data('dyn-tag-id'));
  });

  this.addDomInitializer('gridTemplate-textContent-initializer', function () {
    config = this.config;
    seenDynTags = [];
    $dynTagSettings = $('#dyn-tag-settings');
    config.isEditableMailing = true;
    mailingContent = new MailingContent(this.config.dynTags, this.config.targetGroupList, [], []);
    switchDynTag($('.dyn-tag:first').data('dyn-tag-id'));
  });
  
  function collectForStorage() {
    triggerModify();
    return mailingContent.dynTags
      .filter(dynTag => dynTag.modified)
      .map(dynTag => dynTag.lightWeight);
  }

  function restoreFromStorage(storageDynTags) {
    storageDynTags.forEach(storageDynTag => {
      const currentDynTag = mailingContent.getDynTagById(storageDynTag.id)
      currentDynTag.interestGroup = storageDynTag.interestGroup;
      currentDynTag.contentBlocks = storageDynTag.contentBlocks.map(contentBlock => new DynContent(contentBlock));
    });
    $dynTagEditor.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
    switchDynTag(storageDynTags[0].id);
  }

  function isStorageAdded(storageDynTags) {
    return storageDynTags.some(storageDynTag => {
      const currentDynTag = mailingContent.getDynTagById(storageDynTag.id)
      return isStorageDynTagDiffers(currentDynTag, storageDynTags);
    });
  }

  function isStorageDynTagDiffers(currentDynTag, storageDynTag) {
    return storageDynTag.interestGroup !== currentDynTag.interestGroup
      || currentDynTag.contentBlocks.length !== storageDynTag.contentBlocks.length
      || currentDynTag.contentBlocks.some(currentBlock => {
        const storageBlock = storageDynTag.getContentBlockByTargetId(currentBlock.targetId);
        return !storageBlock || storageBlock.content !== currentBlock.content;
      });
  }

  function displayContentBadges() {
    $('#dyn-tag-list .dyn-tag').each((i, dynTag) => setContentBadge($(dynTag)))
  }

  function setContentBadge($dynTag) {
    const dynTagId = $dynTag.data('dyn-tag-id');
    const empty = mailingContent.getDynTagById(dynTagId).isEmpty;
    $dynTag.find('.status-badge')
      .toggleClass('mailing.status.has-content', !empty)
      .toggleClass('mailing.status.no-content', empty);
  }

  this.addAction({'editor:change': 'validate-on-change'}, function () {
    Form.get(this.el).validateField(this.el);
  });

  this.addAction({'editor:change': 'validate-sms-content'}, function () {
    Form.cleanFieldFeedback$(this.el);

    const errors = AGN.Lib.Validator.get('reject-not-allowed-chars').errors(this.el, {
      msgKey: 'error.mailing.smsSymbolsProhibited',
      chars: '#gsm-7-bit-chars'
    });

    _.each(errors, error => {
      Form.markField(this.el);
      Form.appendFeedbackMessage(this.el, error.msg);
    });
  });

  this.addAction({click: 'switch-dyn-tag'}, function () {
    let currentDynTag = $dynTagSettings.data('conf')?.dynTag;
    if (currentDynTag && !currentDynTag.isValid) {
      this.event.preventDefault();
      return;
    }
    triggerModify();
    switchDynTag(this.el.data('dyn-tag-id'));
  });

  function triggerModify(opts) {
    $dynTagEditor?.trigger('dynTags:modify', opts);
  }

  function hasNotAppliedAutomaticWysiwygChanges(conf, dynTag) {
    return conf.isFullHtmlTags
      && $('#tab-content-wysiwyg').is(":visible")
      && !seenDynTags.includes(dynTag.id);
  }

  /**
   * GWUA-6254: The wysiwyg editor makes automatic changes (e.g. puts content into an <html> template)
   * In order to prevent false dirty state, these changes need to applied
   */
  function preventDirtyAfterWysiwygInitChanges(conf, dynTag) {
    if (hasNotAppliedAutomaticWysiwygChanges(conf, dynTag)) {
      triggerModify({preventEditorHide: true});
      dynTag.markAsClean();
    }
    seenDynTags.push(dynTag.id);
  }

  function fixWysiwygDirty(conf, dynTag) {
    if (window.Jodit) {
      preventDirtyAfterWysiwygInitChanges(conf, dynTag);
    } else {
      CKEDITOR.instances['content']?.on('instanceReady', () => {
        setTimeout(() => {
          preventDirtyAfterWysiwygInitChanges(conf, dynTag)
        }, 0);
      });
    }
  }

  function switchDynTag(dynTagId) {
    const dynTag = mailingContent.getDynTagById(dynTagId);
    const conf = prepareContentEditorConf(dynTag);
    $dynTagEditor = Template.dom('content-editor-template', conf);
    $dynTagSettings.html($dynTagEditor);
    $dynTagSettings.data('conf', conf);
    AGN.Lib.Controller.init($dynTagSettings);
    AGN.runAll($dynTagSettings);
    displayContentBadges();
    fixWysiwygDirty(conf, dynTag);
  }

  function prepareContentEditorConf(dynTag) {
    return {
      dynTag: dynTag,
      targetGroups: _.cloneDeep(mailingContent.targetGroups),
      isFullHtmlTags: dynTag.name === 'HTML-Version',
      showHTMLEditor: dynTag.isHtmlDynTag,
      usedInSmsContent: config.smsDynNames?.length && config.smsDynNames.includes(dynTag.name),
      isEditableMailing: config.isEditableMailing,
      isContentGenerationAllowed: config.isContentGenerationAllowed
    };
  }

  this.addAction({click: 'save'}, function () {
    if (mailingContent.dynTags.some(dynTag => !dynTag.isValid)) {
      return;
    }
    if (isAiTextGeneratedAndNotApplied()) {
      AGN.Lib.Confirm
        .from('mailing-ai-text-generation-apply-question', {dynTagName: $dynTagSettings.data('conf')?.dynTag?.name})
        .then(applyAItextAndSave);
      return;
    }
    triggerModify(); // store last changes
    save(this.el.data('url'));
  });

  function getAiGeneratedText() {
    return $('[data-ai-result]').val()?.trim();
  }

  function isAiTextGeneratedAndNotApplied() {
    const $aiTextGenerationBlock = $('#tab-content-ai-text-generation');
    return $aiTextGenerationBlock.is(":visible") && getAiGeneratedText();
  }

  function applyAItextAndSave() {
    $dynTagEditor.trigger("apply-ai-text-on-save", [getAiGeneratedText()]);
    save($('[data-action="save"]').data('url'));
  }
  
  function save(url) {
    const dynTagsToSave = _.cloneDeep(mailingContent.dynTags.filter(dynTag => dynTag.modified));
    dynTagsToSave.forEach(dynTag => dynTag.contentBlocks.forEach(cb => {
      if (!cb.content) {
        dynTag.remove(cb.uniqueId);
      }
    })); // GWUA-6384: filter out empty content blocks

    $.post({
      url: url,
      contentType: 'application/json',
      data: JSON.stringify(dynTagsToSave)
    }).done(resp => {
      $dynTagEditor.trigger("tile:show");
      AGN.Lib.Page.render(resp)
    });
  }

  this.addAction({click: 'edit-content-modal'}, function () {
    triggerModify();
    if (mailingContent.modified && !window.confirm(LEAVE_QUESTION)) {
      return;
    }
    const showText = config.isEmailMediaTypeActive || config.isMailingGrid;


    // todo check usage and remove after ux redesign finished
    AGN.Lib.Modal.fromTemplate("modal-editor", {
      showText,
      showHtml: showText && config.mailFormat !== 0 && !config.isMailingGrid,
      showSms: config.isSmsMediaTypeActive
    });
  });

  $(window).on('beforeunload', function () {
    triggerModify({ preventEditorHide: true });
    if (mailingContent.modified) {
      AGN.Lib.Loader.hide();
      return LEAVE_QUESTION;
    }
  });

  const selectLastImportedContentSource = function () {
    const $select = $('#content-source-select');

    if (!$select) {
      return;
    }

    const storageKey = 'web-storage#last-content-source';
    const lastContentSource = Storage.get(storageKey);

    if (lastContentSource) {
      if (Select.get($select).hasOption(lastContentSource.id)) {
        $select.val(lastContentSource.id);
        $select.change();
      } else {
        Storage.delete(storageKey);
      }
    }
  }

  // TODO check usage and remove after ux redesign finished
  this.addAction({click: 'save-content'}, function () {
    let $contentForm = $('#content-form');
    if (!Form.get($contentForm).validate()) {
      return;
    }
    Form.get($('#frame-content-form'))
      .submit()
      .done((resp) => AGN.Lib.Page.render(resp));
  });

  class MailingContent {
    constructor(contentData, availableTargetGroups, interestGroups) {
      this.dynTags = Object.entries(contentData).map(function (entry) {
        return new DynTag(entry[1]);
      }, []);
      this.interestGroups = _.cloneDeep(interestGroups);
      this.interestGroups.unshift({column: '', shortname: t('mailing.default.interest_group_name')});
      this.targetGroups = _.cloneDeep(availableTargetGroups);
      this.targetGroups.unshift({id: 0, targetName: t('mailing.default.target_group_name')});

      this.targetGroupsMap = availableTargetGroups.reduce(function (map, targetGroup) {
        map[targetGroup.id] = targetGroup;
        return map;
      }, {});
    }

    get modified() {
      return this.dynTags.some(dynTag => dynTag.modified);
    }

    markAsClean() { // reset dirty state
      this.dynTags.forEach(dynTag => dynTag.markAsClean());
    }

    getDynTagById(id) {
      return this.dynTags.filter(dynTag => dynTag.id === id)[0];
    };

    setDynTag(dynTag) {
      const currentDynTag = this.getDynTagById(dynTag.id);
      const index = this.dynTags.indexOf(currentDynTag);
      if (index > -1) {
        this.dynTags[index] = dynTag.clone();
      }
    };

    getTargetGroupById(targetGroupId) {
      return this.targetGroupsMap[targetGroupId] || {
        id: 0,
        targetName: t('mailing.default.target_group_name')
      };
    };
  }

  class DynTag {
    constructor(dynTagData) {
      this.id = dynTagData.id || 0;
      this.name = dynTagData.dynName === undefined ? dynTagData.name : dynTagData.dynName;
      this.mailingId = dynTagData.mailingID === undefined ? dynTagData.mailingId : dynTagData.mailingID;

      this.interestGroup = this.interestGroup || '';
      this.interestGroup = dynTagData.interestValue === undefined ? dynTagData.interestGroup : dynTagData.dynInterestGroup;

      if (dynTagData.contentBlocks) {
        this.contentBlocks = dynTagData.contentBlocks.map(function (block) {
          return new DynContent(block);
        });
      } else {
        this.contentBlocks = Object.entries(dynTagData.dynContent).map(function (entry) {
          return new DynContent(entry[1]);
        });
      }
      this.markAsClean();
    }
    
    get defaultContentBlock() {
      return { id: 0, index: 0, targetId: 0, content: '' }
    }

    get modified() {
      const initiallyEmpty = !this.initState.contentBlocks.length;
      if (initiallyEmpty && this.isEmpty && this.contentBlocks.length === 1 && this.contentBlocks[0].targetId === 0) {
        return false; // default 'all targets' group added on the first edit
      }
      return !_.isEqual(this.initState, this.lightWeight);
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

    changeTargetGroup(contentBlockId, newTargetGroupId) {
      const contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.targetId = newTargetGroupId;
      return contentBlock;
    };
    
    changeContent(contentBlockId, newContent) {
      const contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.content = newContent || '';
      return contentBlock;
    };

    changeOrder(newOrder) {
      const func = function (self) {
        return function (id) {
          return self.getContentBlockById(parseInt(id));
        };
      };
      this.contentBlocks = newOrder.map(func(this));
      this.recalculateIndexes();
    };

    remove(id) {
      const currentContentBlock = this.getContentBlockById(id);
      const index = this.contentBlocks.indexOf(currentContentBlock);
      if (index > -1) {
        this.contentBlocks.splice(index, 1);
        this.recalculateIndexes();
      }
    };

    clone() {
      return new DynTag(this);
    };
    
    get isEmpty() {
      return this.contentBlocks.every(block => !block.content);
    }

    recalculateIndexes() {
      this.contentBlocks.forEach((contentBlock, index) => contentBlock.index = index + 1);
    };

    get isValid()  {
      const errors = this.collectDynTagErrors();
      errors?.forEach(error => Messages.alert(error));
      return !errors?.length;
    }

    collectDynTagErrors()  {
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

    get isHtmlDynTag() {
      return config.htmlDynTagNames?.includes(this.name);
    }

    #containsIllegalElements(content) {
      return new DOMParser().parseFromString(content, 'text/html').querySelector('script');
    }
    
    get lightWeight() {
      return {
        id: this.id,
        interestGroup: this.interestGroup,
        contentBlocks: this.contentBlocks.map(contentBlock => contentBlock.lightWeight),
      }
    }
  }

  class DynContent {
    constructor(dynContentData) {
      if (typeof DynContent.uniqueIdCounter === 'undefined' ) {
        DynContent.uniqueIdCounter = 0;
      }

      // id for backend frontend side
      this.uniqueId = ++DynContent.uniqueIdCounter;

      // id for backend side
      this.id = dynContentData.id;
      this.index = dynContentData.dynOrder === undefined ? dynContentData.index : dynContentData.dynOrder;
      this.content = dynContentData.dynContent === undefined ? dynContentData.content : dynContentData.dynContent;
      this.targetId = dynContentData.targetID === undefined ? dynContentData.targetId : dynContentData.targetID; 
    }
    
    get lightWeight() {
      const { id, content, targetId } = this;
      return { id, content, targetId };
    }
  }
});
