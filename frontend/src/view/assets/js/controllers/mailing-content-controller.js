AGN.Lib.Controller.new('mailing-content-controller', function () {
  var Confirm = AGN.Lib.Confirm;
  var Template = AGN.Lib.Template;

  var mailingContent;
  var preparedTableEntryTemplate;
  var saveUrl;

  var $tableBody;

  this.addDomInitializer('mailing-content-initializer', function () {
    $tableBody = $('#table_body');
    preparedTableEntryTemplate = Template.prepare('mailing-content-table-entry-template');

    saveUrl = this.config.saveUrl;
    var dynTagsMap = this.config.dynTagsMap;
    var targetGroups = this.config.targetGroupList;
    var interestGroups = this.config.interestGroupList;
    var dynTagNames = this.config.dynTagNames;

    mailingContent = new MailingContent(dynTagsMap, targetGroups, interestGroups, dynTagNames);
    initTableContent();
  });

  this.addAction({click: 'createContentEditorModal'}, function () {
    var dynTag = mailingContent.getDynTagById(parseInt(this.el.data('dyn-name-id')));
    var isHtmlContentBlock = mailingContent.isHtmlContentBlock(dynTag.name);

    var promise = Confirm.createFromTemplate({
      dynTag: dynTag.clone(),
      targetGroups: _.cloneDeep(mailingContent.targetGroups),
      interestGroups: _.cloneDeep(mailingContent.interestGroups),
      saveUrl: _.cloneDeep(saveUrl),
      DynTagObject: DynTag,
      isFullHtmlTags: dynTag.name == 'HTML-Version',
      showHTMLEditor: isHtmlContentBlock
    }, 'content-editor-template');

    promise.done(function (dynBlock) {
      mailingContent.setDynTag(dynBlock);
      replaceTableContent(dynBlock);
      updatePreview();
    });
  });

  var updatePreview = function () {
    var $previewForm = $('#preview');
    $previewForm.submit();
  };

  var initTableContent = function() {
    mailingContent.dynTags.forEach(function (dynTag) {
      $tableBody.append(getFilledTemplate(dynTag));
    })
  };

  var replaceTableContent = function(dynTag) {
    var html = getFilledTemplate(dynTag);
    $tableBody.find('[data-dyn-name-id="' + dynTag.id + '"]').replaceWith(html);
  };

  var getFilledTemplate = function(dynTag) {
    var targetGroups = dynTag.contentBlocks.map(function (contentBlock) {
      return getTargetGroupName(contentBlock);
    }, []);

    var contents = dynTag.contentBlocks.map(function (contentBlock) {
      return contentBlock.content.length > 35 ? contentBlock.content.substring(0, 35) + '...' : contentBlock.content;
    }, []);

    return preparedTableEntryTemplate({id: dynTag.id, name: dynTag.name, targetGroups: targetGroups, contents: contents});
  };

  var getTargetGroupName = function (dynContent) {
    var targetGroupName = t('mailing.default.target_group_name');
    if (dynContent.targetId > 0) {
      var targetGroup = mailingContent.getTargetGroupById(dynContent.targetId);
      var description = targetGroup.deleted ? t('mailing.default.target_group_deleted') : targetGroup.id;
      targetGroupName = targetGroup.targetName + ' (' + description + ')';
    }

    return targetGroupName;
  };

  var MailingContent = function MailingContent(contentData, availableTargetGroups, interestGroups, dynTagNames) {
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

    this.dynTagNames = dynTagNames;

    this.isHtmlContentBlock = function(dynTagName) {
      if (typeof dynTagNames != "undefined" && dynTagNames != null && dynTagNames.length != null && dynTagNames.length > 0) {
          return dynTagNames.includes(dynTagName);
      }
      return false;
    };

    this.getDynTagById = function (id) {
      var filteredBlocks = this.dynTags.filter(function (block) {
        return block.id === id;
      });
      return filteredBlocks[0];
    };

    this.setDynTag = function (dynTag) {
      var currentDynTag = this.getDynTagById(dynTag.id);
      var index = this.dynTags.indexOf(currentDynTag);
      if (index > -1) {
        this.dynTags[index] = dynTag.clone();
      }
    };

    this.getTargetGroupById = function (targetGroupId) {
      return this.targetGroupsMap[targetGroupId] || {
        id: 0,
        targetName: t('mailing.default.target_group_name')
      };
    };
  };

  var DynTag = function (dynTagData) {
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

    this.contentBlocks.__proto__.swap = function (indexFrom, indexTo) {
      var temp = this[indexFrom];
      this[indexFrom] = this[indexTo];
      this[indexTo] = temp;
      return this;
    };

    var defaultContentBlock = {
      id: 0,
      index: 0,
      targetId: 0,
      content: ''
    };

    this.getContentBlockByTargetId = function (targetId) {
      var filteredBlocks = this.contentBlocks.filter(function (block) {
        return block.targetId === targetId;
      });
      return filteredBlocks[0];
    };

    this.getContentBlockById = function (id) {
      var filteredBlocks = this.contentBlocks.filter(function (block) {
        return block.uniqueId === id;
      });
      return filteredBlocks[0];
    };

    this.createNewContentBlock = function () {
      var newContentBlock = new DynContent(defaultContentBlock);
      newContentBlock.index = this.contentBlocks.length;
      this.contentBlocks.push(newContentBlock);
      recalculateIndexes(this.contentBlocks);
      return newContentBlock;
    };

    this.moveUpContentBlock = function (contentBlockId) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      if (contentBlock.index > 1) {
        this.contentBlocks.swap(contentBlock.index - 1, contentBlock.index - 2);
        recalculateIndexes(this.contentBlocks);
      }
    };

    this.moveDownContentBlock = function (contentBlockId) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      if (contentBlock.index < this.contentBlocks.length) {
        this.contentBlocks.swap(contentBlock.index - 1, contentBlock.index);
        recalculateIndexes(this.contentBlocks);
      }
    };

    this.setFirstContentBlock = function (contentBlockId) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      if (contentBlock.index > 1) {
        var removedContentBlock = this.contentBlocks.splice(contentBlock.index - 1, 1);
        this.contentBlocks = removedContentBlock.concat(this.contentBlocks);
        recalculateIndexes(this.contentBlocks);
      }
    };

    this.setLastContentBlock = function (contentBlockId) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      if (contentBlock.index < this.contentBlocks.length) {
        var removedContentBlock = this.contentBlocks.splice(contentBlock.index - 1, 1);
        this.contentBlocks = this.contentBlocks.concat(removedContentBlock);
        recalculateIndexes(this.contentBlocks);
      }
    };

    this.changeTargetGroup = function (contentBlockId, newTargetGroupId) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.targetId = newTargetGroupId;
      return contentBlock;
    };

    this.changeInterestGroup = function(interestGroupKey) {
      this.interestGroup = interestGroupKey;
    };

    this.changeContent = function (contentBlockId, newContent) {
      var contentBlock = this.getContentBlockById(contentBlockId);
      contentBlock.content = newContent || '';
      return contentBlock;
    };

    this.changeOrder = function (newOrder) {
      var func = function (self) {
        return function (id) {
          return self.getContentBlockById(parseInt(id));
        };
      };
      this.contentBlocks = newOrder.map(func(this));
      recalculateIndexes(this.contentBlocks);
    };

    this.remove = function(id) {
      var currentContentBlock = this.getContentBlockById(id);
      var index = this.contentBlocks.indexOf(currentContentBlock);
      if (index > -1) {
        this.contentBlocks.splice(index, 1);
        recalculateIndexes(this.contentBlocks);
      }
    };

    this.clone = function () {
      return new DynTag(this);
    };

    var recalculateIndexes = function (contentBlocks) {
      contentBlocks.forEach(function (contentBlock, index) {
        contentBlock.index = index + 1;
      });
    };
  };

  var DynContent = function (dynContentData) {
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
});
