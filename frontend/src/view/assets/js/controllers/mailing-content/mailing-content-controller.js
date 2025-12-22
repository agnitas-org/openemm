AGN.Lib.Controller.new('mailing-content-controller', function () {

  const Def = AGN.Lib.MailingContent.Def;
  const BlocksManager = AGN.Lib.MailingContent.BlocksManager;
  const EmcTextModulesManager = AGN.Lib.MailingContent.EmcTextModulesManager;
  const Storage = AGN.Lib.Storage;
  const Select = AGN.Lib.Select;
  const Form = AGN.Lib.Form;

  const LEAVE_QUESTION = t('defaults.leaveQuestion');
  const LAST_CONTENT_SOURCE_STORAGE_KEY = 'web-storage#last-content-source';

  let blocksManager;
  let frameContentEditorStorageKey;

  this.addDomInitializer('mailing-content-initializer', function () {
    Def.htmlDyntagNames = this.config.htmlDynTagNames
    Def.smsDyntagNames = this.config.smsDynNames
    Def.isEditableMailing = this.config.isEditableMailing
    Def.isContentGenerationAllowed = this.config.isContentGenerationAllowed
    Def.availableTargetGroups = [{id: 0, targetName: t('mailing.default.target_group_name')}, ..._.cloneDeep(this.config.targetGroupList)];

    frameContentEditorStorageKey = `toggleFullScreenEditor:mailing:${this.config.mailingId}`;
    blocksManager = new BlocksManager(this.config);
    selectLastImportedContentSource();
  });

  this.addDomInitializer('gridTemplate-textContent-initializer', function () {
    Def.isContentGenerationAllowed = this.config.isContentGenerationAllowed
    Def.availableTargetGroups = [{id: 0, targetName: t('mailing.default.target_group_name')}, ..._.cloneDeep(this.config.targetGroupList)];
    Def.isTextModulesPage = true;

    this.config.interestGroupList = [];
    blocksManager = new EmcTextModulesManager(this.config);
  });

  this.addInitializer('frame-content-tab', function () {
    const editorId = Storage.get(frameContentEditorStorageKey);
    if (editorId) {
      $(`#${editorId}`).addClass('tile--full-screen');
      Storage.delete(frameContentEditorStorageKey);
    }
  });

  this.addDomInitializer('manage-targets-modal-initializer', () => blocksManager.editor.targetsManager.init());

  this.addAction({click: 'save'}, function () {
    const $contentBlocksTab = getContentBlocksTab$();
    if ((!$contentBlocksTab.exists() || $contentBlocksTab.hasClass('active')) && $('#dyn-tag-list').exists()) {
      blocksManager.save();
      return;
    }

    return saveFrameContent(this.el.closest('.tile'));
  });

  function getContentBlocksTab$() {
    return $('[data-toggle-tab="#content-blocks-tab"]');
  }

  function saveFrameContent($editorTile) {
    if ($editorTile.hasClass('tile--full-screen')) {
      Storage.set(frameContentEditorStorageKey, $editorTile.attr('id'));
    }
    blocksManager.editor?.$el?.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
    AGN.Lib.Loader.prevent();
    return Form.get($('#frame-content-tab')).submit();
  }

  this.addAction({keyup: 'search-content-blocks'}, $el=> blocksManager.search($el.val().toLowerCase()));

  this.addAction({click: 'delete-content-block'}, $el => blocksManager.editor.targetsManager.deleteContentBlock($el));

  this.addAction({click: 'add-content-block'}, () => blocksManager.editor.targetsManager.addTargetGroup());

  this.addAction({change: 'change-interest-group'}, $el => blocksManager.editor.currentDynTag.interestGroup = $el.val());

  this.addAction({change: 'change-target'}, $el => blocksManager.editor.targetsManager.changeTarget($el));

  this.addAction({change: 'switch-content-block'}, $el => blocksManager.switchContentBlock(parseInt($el.val())));

  this.addAction({click: 'switch-dyn-tag', change: 'switch-dyn-tag'}, switchDynTag);

  this.addAction({'editor:change': 'validate-on-change'}, $el=> Form.get($el).validateField($el));

  this.addAction({'editor:change': 'validate-sms-content'}, validateSmsContent);

  function switchDynTag() {
    const idToSwitch = parseInt(this.el.val());
    if (blocksManager.cantSwitchDynTag(idToSwitch)) {
      this.event.preventDefault();
      return;
    }
    blocksManager.editor.applyLastChanges();
    blocksManager.switchDynTag(idToSwitch);
  }

  function selectLastImportedContentSource() {
    const $select = $('#content-source-select');
    if (!$select.exists()) {
      return;
    }
    const lastContentSource = Storage.get(LAST_CONTENT_SOURCE_STORAGE_KEY);

    if (!lastContentSource) {
      return;
    }
    if (Select.get($select).hasOption(lastContentSource.id)) {
      $select.val(lastContentSource.id).change();
    } else {
      Storage.delete(LAST_CONTENT_SOURCE_STORAGE_KEY);
    }
  }

  function validateSmsContent() {
    Form.cleanFieldFeedback$(this.el);

    const errors = AGN.Lib.Validator.get('reject-not-allowed-chars').errors(this.el, {
      msgKey: 'error.mailing.smsSymbolsProhibited',
      chars: '#gsm-7-bit-chars'
    });

    _.each(errors, error => {
      Form.markField(this.el);
      Form.appendFeedbackMessage(this.el, error.msg);
    });
  }

  $(window).on('beforeunload', function () {
    blocksManager.editor?.applyLastChanges(true); // fix wysiwyg editor after 'changes not saved' dismiss
    if (blocksManager.isDirty) {
      AGN.Lib.Loader.hide();
      return LEAVE_QUESTION;
    }
  });
});
