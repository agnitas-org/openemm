// todo check usage and remove after ux redesign finished
AGN.Lib.Controller.new('mailing-content-editor-controller', function () {
  // libs
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  
  // constants
  const EVENT_NAMESPACE = '.mailingContentNamespace';
  const TARGET_ROW_SELECTOR = '.target-row';
  const ID_PREFIX = 'content_entry_';
  const EDIT_BTN_SELECTOR = '[data-action="edit-content-for-target"]';
  
  // configuration
  let currentDynTag;

  // ui elements
  let $targetsOrder;
  let $targetGroups;
  let $interestGroups;

  this.addDomInitializer('mailing-content-editor-initializer', function ($editor) {
    const config = $('#dyn-tag-settings').data('conf');
    currentDynTag = config.dynTag;

    $targetsOrder = $('#targets-order');
    $targetGroups = $('#target_groups');
    $interestGroups = $('#interest_groups');

    initSortable();
    initDynContent(currentDynTag);
    initInterestGroup(currentDynTag.interestGroup);
    fixEnlargedEditorScroll();
    addEditorListener($editor);
  });


  function fixEnlargedEditorScroll() {
    $('#enlargeable-settings').on('enlarged', function (ev) {
      $(ev.target).closest('.modal-body').css('overflow', 'hidden')
    })
  }

  function addEditorListener($editor) {
    $editor.on("dynTags:modify", function (e, opts) {
      getEditorsContent();
      if (!opts?.preventEditorHide) {
        $editor.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
      }
    });
    $editor.on("apply-ai-text-on-save", function (e, content) {
      const selectedContentBlockId = getSelectedContentBlockId();
      if (selectedContentBlockId > 0) {
        currentDynTag.changeContent(selectedContentBlockId, content);
      }
    });
  }

  function disabledCurrentEditBtn() {
    $targetsOrder.find(EDIT_BTN_SELECTOR).removeClass('disabled');
    let $selectedRow = $(`${TARGET_ROW_SELECTOR}.selected`);
    $selectedRow = $selectedRow.exists() ? $selectedRow : $(`${TARGET_ROW_SELECTOR}:first`);
    $selectedRow.find(EDIT_BTN_SELECTOR).addClass('disabled');
  }

  this.addAction({click: 'edit-content-for-target'}, function () {
    if (!currentDynTag.isValid) {
      return;
    }
    const $current = $(this.el);
    const contentBlockId = getElementId($current);
    const contentBlock = currentDynTag.getContentBlockById(contentBlockId);
    selectContentBlock(contentBlock);
  });

  this.addAction({click: 'deleteContentEntry'}, function () {
    this.event.stopPropagation();
    var $current = $(this.el).parent();
    var id = getElementId($current);
    var $nextSelected = $current.next(TARGET_ROW_SELECTOR);
    $nextSelected = $nextSelected.length > 0 ? $nextSelected : $current.prev(TARGET_ROW_SELECTOR);
    currentDynTag.remove(id);
    $current.remove();

    if (currentDynTag.contentBlocks.length > 0) {
      // picks one of the neighbours
      var nextSelectedId = getElementId($nextSelected);
      var nextSelected = currentDynTag.getContentBlockById(nextSelectedId);
      selectContentBlock(nextSelected);
    } else {
      selectContentBlock();
    }
    disableUsedTargetOptionsInOrder();
  });

  this.addAction({click: 'addDynContent'}, createNewTag);

  function updateTargetsOrder($targetsOrder) {
    const newOrder = $targetsOrder.sortable('toArray');
    const newOrderIds = newOrder.map(function (id) {
      return id.replace(ID_PREFIX, '');
    });
    currentDynTag.changeOrder(newOrderIds);
  }

  function initSortable () {
    $targetsOrder.sortable({
      delay: 10,
      scroll: true,
      scrollSensitivity: 30,
      scrollSpeed: 5,
      handle: ".target-row-handle",
      cursor: "move",
      items: TARGET_ROW_SELECTOR,
      stop: (event) => updateTargetsOrder($(event.target))
    });
  }

  var setEditorsContent = function (content, selectAll) {
    const $wysiwygEditorBlock = $('#tab-content-wysiwyg');
    const $htmlEditorBlock = $('#contentEditor');
    const $aiTextGenerationBlock = $('#tab-content-ai-text-generation');

    if ($wysiwygEditorBlock.is(":visible")) {
      if (window.Jodit) {
        Jodit.instances['content'].value = content;
      } else {
        var editor = CKEDITOR.instances['content'];
        if (editor.status === 'ready') {
          editor.setData(content)
        } else {
          editor.on("instanceReady", function (event) {
            event.editor.setData(content);
          });
        }
      }
    }
    if ($htmlEditorBlock.is(":visible")) {
      var cursorPos;
      if(selectAll === false) {
        cursorPos = 1; // move cursor to the end
      } else {
        cursorPos = null; // select all
      }
      var aceEditor = ace.edit("contentEditor");
      if(AGN.Lib.Helpers.isDarkTheme()) {
        aceEditor.setTheme("ace/theme/idle_fingers");
      }
      aceEditor.setValue(content, cursorPos);
    }
    if ($aiTextGenerationBlock.is(":visible")) {
      // when user selects another content block and 'Text generation' tab was opened, we should set of content to WYSIWYG and HTML tab
      $('#multi-editor').find('.js-wysiwyg').val(content);
    }
  };

  function getContentFromEditors() {
    const $wysiwygEditorBlock = $('#tab-content-wysiwyg');
    const $htmlEditorBlock = $('#contentEditor');
    const $aiEditorBlock = $('#tab-content-ai-text-generation');
    
    if ($aiEditorBlock.is(":visible")) {
      return $wysiwygEditorBlock.exists()
        ? $('#content').val()
        : ace.edit("contentEditor").getValue();
    }
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

  var getEditorsContent = function() {
    const selectedContentBlockId = getSelectedContentBlockId();

    if (selectedContentBlockId > 0) {
      const content = getContentFromEditors();

      if (content !== undefined) {
        currentDynTag.changeContent(selectedContentBlockId, content);
      }
    }
  };

  function createTargetRow$(targetId, contentBlock) {
    const $row = Template.dom('mailing-target-content-template', {targetId, id: ID_PREFIX + contentBlock.uniqueId});
    AGN.Lib.CoreInitializer.run('select', $row);
    return $row;
  }

  function initDynContent (dynTag) {
    dynTag.contentBlocks.reverse().forEach(function (contentBlock) {
      $targetsOrder.prepend(createTargetRow$(contentBlock.targetId, contentBlock));
    });
    if (currentDynTag.contentBlocks.length > 0) {
      selectContentBlock(currentDynTag.contentBlocks[0], false);
    } else {
      createNewTag();
    }
    disableUsedTargetOptionsInOrder();
    scrollToTargetsBottom();
    moveAllRecipientsTargetToEnd();
  }

  function initInterestGroup (key) {
    $interestGroups.val(key).trigger("change");
    $interestGroups.off('change' + EVENT_NAMESPACE);
    $interestGroups.on('change' + EVENT_NAMESPACE, event => currentDynTag.interestGroup = $(event.target).val())
  }

  var getSelectedContentBlock = function(){
    return $targetsOrder.find(TARGET_ROW_SELECTOR + '.selected');
  };

  var getSelectedContentBlockId = function () {
    var $selectedDynTag = getSelectedContentBlock();
    if ($selectedDynTag.length > 0) {
      return getElementId($selectedDynTag);
    }
  };

  var markSelected = function (contentBlockId) {
    $targetsOrder.children().removeClass('selected');
    const $clickedBlock = findElementById(contentBlockId, $targetsOrder);
    $clickedBlock.addClass('selected');
  };

  var findElementById = function(id, $parent) {
    $parent = $parent ? $parent : $(document);
    return $parent.find('#' + ID_PREFIX + id)
  };

  function getElementId($el) {
    return parseInt($el.closest(TARGET_ROW_SELECTOR).attr('id').replace(ID_PREFIX, ''));
  }

  this.addAction({'change': 'change-target'}, function() {
    currentDynTag.changeTargetGroup(getElementId(this.el), parseInt(this.el.val()))
    disableUsedTargetOptionsInOrder();
  });

  function selectContentBlock(contentBlock, selectAll) {
    getEditorsContent();

    if (contentBlock !== undefined) {
      markSelected(contentBlock.uniqueId);
      setEditorsContent(contentBlock.content, selectAll);
    } else {
      setEditorsContent('');
    }
    disabledCurrentEditBtn();
    scrollToTargetsBottom();
  }

  function getSelectedTargets() {
    return $('#targets-order')
      .find('select')
      .map((i, select) => $(select).val())?.toArray();
  }

  function disableUsedTargetOptionsInOrder() {
    const selectedTargets = getSelectedTargets();
    $('#targets-order-box').find('select').each(function (i, el) {
      Select.get($(el)).disableOptions(selectedTargets);
    });
    Select.get($targetGroups).disableOptions(selectedTargets, true);
    Select.get($targetGroups).selectFirstValue();
  }

  function moveAllRecipientsTargetToEnd() {
    $targetsOrder
      .find('select option[value="0"]:selected')
      .closest(TARGET_ROW_SELECTOR)
      .appendTo($targetsOrder);
    updateTargetsOrder($targetsOrder);
  }

  function scrollToTargetsBottom() {
    $targetsOrder.animate({scrollTop: $targetsOrder[0].scrollHeight}, 100);
  }

  function createNewTag() {
    const targetId = parseInt($targetGroups.val());
    const contentBlock = currentDynTag.createNewContentBlock(targetId);
    if (!$targetsOrder.find(TARGET_ROW_SELECTOR).exists()) {
      contentBlock.content = getContentFromEditors();
    }
    $targetsOrder.append(createTargetRow$(targetId, contentBlock));
    selectContentBlock(contentBlock);
    scrollToTargetsBottom();
    moveAllRecipientsTargetToEnd();
    disableUsedTargetOptionsInOrder();
  }
});
