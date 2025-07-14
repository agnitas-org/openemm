AGN.Lib.Controller.new('mailing-content-controller-new-ux', function () {
  const LEAVE_QUESTION = t('defaults.leaveQuestion');

  const Template = AGN.Lib.Template;
  const Messages = AGN.Lib.Messages;
  const AutoSave = AGN.Lib.AutoSave;
  const Storage = AGN.Lib.Storage;
  const Select = AGN.Lib.Select;
  const Form = AGN.Lib.Form;

  let mailingContent;
  let config;
  let editorStorageKey;
  let $dynTagSettings;
  let $dynTagEditor;

  this.addDomInitializer('mailing-content-initializer-new-ux', function () {
    config = this.config;
    $dynTagSettings = $('#dyn-tag-settings');
    editorStorageKey = `toggleFullScreenEditor:mailing:${config.mailingId}`;
    mailingContent = new MailingContent(config.dynTags, config.targetGroupList, config.interestGroupList);
    selectLastImportedContentSource();
    AutoSave.initialize(`mailing/${config.mailingId}/content-blocks`, collectForStorage, isStorageAdded, restoreFromStorage);
    switchDynTag(parseInt($('.dyn-tag:first').val()));
  });

  this.addDomInitializer('gridTemplate-textContent-initializer-new-ux', function () {
    config = this.config;
    $dynTagSettings = $('#dyn-tag-settings');
    config.isEditableMailing = true;
    mailingContent = new MailingContent(this.config.dynTags, this.config.targetGroupList, [], []);
    switchDynTag(parseInt($('.dyn-tag:first').val()));
  });

  this.addInitializer('frame-content-tab', function () {
    const editorId = Storage.get(editorStorageKey);
    if (editorId) {
      $(`#${editorId}`).addClass('tile--full-screen');
      Storage.delete(editorStorageKey);
    }
  });

  this.addAction({keyup: 'search-content-blocks'}, function () {
    const searchStr = this.el.val().toLowerCase();
    const $list = $('#dyn-tag-list .list-group');

    $list.find('.dyn-tag').each(function () {
      const $block = $(this);
      const blockName = $block.find('small').text();
      $block.toggle(blockName.toLowerCase().includes(searchStr));
    });
    $('[data-blocks-not-found-msg]').toggle(!$list.find('.dyn-tag:visible').exists());
    if (searchStr.length > 0) {
      $list.scrollTop(0);
    }
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

  function setContentBadgesInContainer($container) {
    $container.find('.dyn-tag').each((i, dynTag) => setContentBadge($(dynTag)))
  }

  function displayContentBadges() {
    setContentBadgesInContainer($('#dyn-tag-list'));

    const $mobileDynTags = $('#mobile-dyn-tags-list');
    $mobileDynTags.on('select2:open', function () {
      const resultsContainer = $('#select2-mobile-dyn-tags-list-results')[0];
      const observer = new MutationObserver(() =>
        $(resultsContainer).children().each((i, el) => setContentBadgesInContainer($(el))));
      observer.observe(resultsContainer, {childList: true});
      $mobileDynTags.on('select2:close', () => observer.disconnect());
    });
  }

  function setContentBadge($dynTag) {
    const dynTagId = parseInt($dynTag.val());
    const empty = mailingContent.getDynTagById(dynTagId).isEmpty;
    $dynTag.find('.icon-file-alt').toggleClass('text-primary', !empty)
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

  this.addAction({
    click: 'switch-dyn-tag',
    change: 'switch-dyn-tag'
  }, function () {
    const idToSwitch = parseInt(this.el.val());
    const currentDynTag = $dynTagSettings.data('conf')?.dynTag;
    if (cantSwitchDynTag(currentDynTag, idToSwitch)) {
      this.event.preventDefault();
      return;
    }
    $('.dyn-tag').removeClass('active');
    this.el.addClass('active');
    triggerModify();
    switchDynTag(idToSwitch);
  });

  function cantSwitchDynTag(currentDynTag, idToSwitch) {
    return !currentDynTag?.isValid || currentDynTag.id === idToSwitch;
  }

  function triggerModify(opts) {
    $dynTagEditor?.trigger('dynTags:modify', opts);
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
  }

  function prepareContentEditorConf(dynTag) {
    return {
      dynTag,
      targetGroups: _.cloneDeep(mailingContent.targetGroups),
      isFullHtmlTags: dynTag.name === 'HTML-Version',
      showHTMLEditor: dynTag.isHtmlDynTag,
      usedInSmsContent: config.smsDynNames?.length && config.smsDynNames.includes(dynTag.name),
      isEditableMailing: config.isEditableMailing,
      isContentGenerationAllowed: config.isContentGenerationAllowed
    };
  }

  function frameContentTabActive() {
    return $('[data-toggle-tab="#frame-content-tab"]').hasClass('active');
  }

  this.addAction({click: 'save'}, function () {
    if (frameContentTabActive()) {
      const $editorTile = this.el.closest('.tile');
      if ($editorTile.hasClass('tile--full-screen')) {
        Storage.set(editorStorageKey, $editorTile.attr('id'));
      }
      $dynTagEditor.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
      AGN.Lib.Loader.prevent();
      return Form.get($('#frame-content-tab')).submit();
    }
    if (mailingContent.dynTags.some(dynTag => !dynTag.isValid)) {
      return;
    }
    triggerModify(); // store last changes
    save(this.el.data('url'));
  });
  
  function save(url) {
    const dynTagsToSave = mailingContent.dynTags.filter(dynTag => dynTag.modified);
    $.post({
      url: url,
      contentType: 'application/json',
      data: JSON.stringify(dynTagsToSave)
    }).done(resp => {
      $dynTagEditor.trigger("tile:show");
      AGN.Lib.Page.render(resp);
    });
  }

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
      this.initState = _.cloneDeep(this.lightWeight); // for dirty state check
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
