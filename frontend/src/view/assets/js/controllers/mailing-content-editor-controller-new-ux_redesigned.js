AGN.Lib.Controller.new('mailing-content-editor-controller-new-ux', function () {
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;

  let currentDynTag;
  let currentContentBlock;

  this.addDomInitializer('mailing-content-editor-initializer-new-ux', function ($editor) {
    const $editorTile = $('#dyn-tag-settings');
    const config = $editorTile.data('conf');
    currentDynTag = config.dynTag;
    currentContentBlock = undefined;

    selectContentBlock(currentDynTag.contentBlocks[0] || currentDynTag.createNewContentBlock());
    initInterestGroup();
    addEditorListener($editor);
  });

  this.addAction({change: 'switch-content-block'}, function () {
    if (!currentDynTag.isValid) {
      return;
    }
    const block = currentDynTag.getContentBlockById(parseInt(this.el.val()));
    selectContentBlock(block);
  });

  this.addAction({change: 'change-interest-group'}, el => currentDynTag.interestGroup = $(el).val());

  function initInterestGroup() {
    AGN.Lib.CoreInitializer.run('select', $('[data-action="change-interest-group"]')
      .val(currentDynTag.interestGroup));
  }

  function addEditorListener($editor) {
    $editor.on("dynTags:modify", function (e, opts) {
      setContentFromEditorToBlock();
      if (!opts?.preventEditorHide) {
        $editor.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
      }
    });
  }

  function selectContentBlock(contentBlock) {
    setContentFromEditorToBlock();
    setEditorsContent(contentBlock === undefined ? '' : contentBlock.content);
    currentContentBlock = contentBlock;
    updateContentBlockSelectOptions();
  }

  function updateContentBlockSelectOptions() {
    const $contentBlocks = Template.dom('mailing-content-select-template', {dynTag: currentDynTag});
    Select.get($('[data-action="switch-content-block"]')).replaceWith($contentBlocks);
    $('#content-block-index').text(currentContentBlock.index);
    if (currentContentBlock) {
      Select.get($contentBlocks).selectValue(currentContentBlock.uniqueId);
    }
  }

  function setEditorsContent(content) {
    if ($('#editor-switch').prop('checked') === true) {
      setAceEditorContent(content);
    } else {
      setWysiwygEditorContent(content);
    }
  }

  function setWysiwygEditorContent(content) {
    if (window.Jodit) {
      Jodit.instances['content'].value = content;
    } else {
      const editor = CKEDITOR.instances['content'];
      if (editor.status === 'ready') {
        editor.setData(content)
      } else {
        editor.on("instanceReady", function (event) {
          event.editor.setData(content);
        });
      }
    }
  }

  function setAceEditorContent(content) {
    const aceEditor = ace.edit("contentEditor");
    if (AGN.Lib.Helpers.isDarkTheme()) {
      aceEditor.setTheme("ace/theme/idle_fingers");
    }
    aceEditor.setValue(content, 1);
  }

  function getContentFromEditors() {
    const $wysiwygEditorBlock = $('#tab-content-wysiwyg');
    const $htmlEditorBlock = $('#contentEditor');

    if ($wysiwygEditorBlock.is(":visible")) {
      if (window.Jodit) {
        return Jodit.instances['content'].value;
      } else {
        return CKEDITOR.instances['content'].getData();
      }
    }
    if ($htmlEditorBlock.is(":visible")) {
      return ace.edit("contentEditor").getValue();
    }
  }

  function setContentFromEditorToBlock() {
    if (!currentContentBlock) {
      return;
    }
    const content = getContentFromEditors();
    if (content !== undefined) {
      currentDynTag.changeContent(currentContentBlock.uniqueId, content);
    }
  }


  const TARGET_ROW_SELECTOR = '.target-row';
  const SORTABLE_OPTIONS = {
    scroll: false,
    handle: ".target-row-handle",
    cursor: "move",
    items: `${TARGET_ROW_SELECTOR}:not(:has(option[value="0"][selected]))`,
    stop: updateSortable
  }
  let $targetsOrder;

  this.addDomInitializer('manage-targets-modal-initializer', function () {
    $targetsOrder = $('#targets-order').sortable(SORTABLE_OPTIONS);
    currentDynTag.contentBlocks.forEach(block => addTargetRow(block));
    updateSortable();
  });

  this.addAction({click: 'delete-content-block'}, function () {
    const $toRemove = $(this.el).closest(TARGET_ROW_SELECTOR);
    const blockIdToRemove = getContentBlockIdFromTargetRow($toRemove);

    if (isContentBlockUnderEdit(blockIdToRemove)) {
      currentContentBlock = undefined;
      selectContentBlock(getContentBlockToReplaceRemoved($toRemove));
    }
    currentDynTag.remove(blockIdToRemove);
    $toRemove.remove();
    updateSortable();
  });

  this.addAction({click: 'add-content-block'}, addTargetGroup);

  function getContentBlockToReplaceRemoved($toRemove) {
    const $target = $toRemove.next(TARGET_ROW_SELECTOR).length
      ? $toRemove.next(TARGET_ROW_SELECTOR)
      : $toRemove.prev(TARGET_ROW_SELECTOR);
    $target.find('.input-group-text').addClass('active').removeClass('disabled');
    return currentDynTag.getContentBlockById(getContentBlockIdFromTargetRow($target));
  }

  function isContentBlockUnderEdit(contentBlockId) {
    return currentContentBlock.uniqueId === contentBlockId;
  }

  function addTargetRow(contentBlock) {
    const $targetRow = Template.dom('mailing-content-target-template', {
      ...contentBlock,
      isCurrent: isContentBlockUnderEdit(contentBlock.uniqueId)
    });
    $targetsOrder.append($targetRow);
    addUpdateSelectedAttrOnChangeHandler($targetRow);
    AGN.Lib.CoreInitializer.run('select', $targetRow);
    return $targetRow;
  }

  function addUpdateSelectedAttrOnChangeHandler($targetRow) { // select2 doesn't do this by default
    $targetRow.find('select').on('change, change.select2', function() {
      const value = $(this).val();
      $(this).find('option').each((i, opt) => $(opt).attr('selected', $(opt).val() === value ? 'selected' : null));
    });
  }

  function updateSortable() {
    updateTargetsOrder();
    updateContentBlockSelectOptions();
    disableUsedTargetOptionsInOrder();
    moveAllRecipientsTargetToEnd();
  }

  function updateSortableTargetIndex($targetRow) {
    const index = currentDynTag.getContentBlockById(parseInt($targetRow.attr('id'))).index;
    $targetRow.find('.input-group-text').text(index);
  }

  function updateTargetsOrder() {
    const newOrder = $targetsOrder.find(TARGET_ROW_SELECTOR).toArray().map(row => $(row).attr('id'));
    currentDynTag.changeOrder(newOrder);
    $targetsOrder.find(TARGET_ROW_SELECTOR).each((i, targetRow) => updateSortableTargetIndex($(targetRow)))
  }

  function moveAllRecipientsTargetToEnd() {
    $targetsOrder
      .find('select option[value="0"]:selected')
      .closest(TARGET_ROW_SELECTOR)
      .appendTo($targetsOrder);
    updateTargetsOrder();
  }

  function getContentBlockIdFromTargetRow($el) {
    return parseInt($el.closest(TARGET_ROW_SELECTOR).attr('id'));
  }

  this.addAction({'change': 'change-target'}, function() {
    currentDynTag.changeTargetGroup(getContentBlockIdFromTargetRow(this.el), parseInt(this.el.val()))
    disableUsedTargetOptionsInOrder();
    moveAllRecipientsTargetToEnd();
    updateContentBlockSelectOptions();
  });

  function disableUsedTargetOptionsInOrder() {
    const $selects = $targetsOrder.find('select');
    const usedTargets = $selects.map((i, select) => $(select).val())?.toArray();
    _.each($selects, el => Select.get($(el)).disableOptions(usedTargets));
    return usedTargets;
  }

  function addTargetGroup() {
    const contentBlock = currentDynTag.createNewContentBlock();
    const usedTargets = disableUsedTargetOptionsInOrder();
    const $targetSelect = addTargetRow(contentBlock).find('select');
    Select.get($targetSelect).disableOptions(usedTargets, true);
    contentBlock.targetId = parseInt($targetSelect.val());
    moveAllRecipientsTargetToEnd();
    updateContentBlockSelectOptions();
    return contentBlock;
  }
});
