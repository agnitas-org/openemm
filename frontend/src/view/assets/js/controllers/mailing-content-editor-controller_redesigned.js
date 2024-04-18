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

  function fixEnlargedEditorScroll() {
    $('#enlargeable-settings').on('enlarged', function (ev) {
      $(ev.target).closest('.modal-body').css('overflow', 'hidden')
    })
  }

  function addEditorListener($editor) {
    $editor.on("dynTags:modify", function () {
      getEditorsContent();
      $editor.trigger("tile:hide"); // destroy current editors. wysiwyg-events.js
    });
  }

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
      var editor = CKEDITOR.instances['content'];
      if (editor.status === 'ready') {
        editor.setData(content)
      } else {
        editor.on("instanceReady", function (event) {
          event.editor.setData(content);
        });
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
      if($("body").hasClass("dark-theme")) {
        aceEditor.setTheme("ace/theme/idle_fingers");
      }
      aceEditor.setValue(content, cursorPos);
    }

    if ($aiTextGenerationBlock.is(":visible")) {
      // when user selects another content block and 'Text generation' tab was opened, we should set of content to WYSIWYG and HTML tab
      $aiTextGenerationBlock.closest('form').find('.js-wysiwyg').val(content);
    }
  };

  var getEditorsContent = function() {
    var selectedContentBlockId = getSelectedContentBlockId();

    if (selectedContentBlockId > 0) {
      var $wysiwygEditorBlock = $('#tab-content-wysiwyg');
      var $htmlEditorBlock = $('#contentEditor');
      var content;

      if ($wysiwygEditorBlock.is(":visible")) {
        var wysiwygEditor = CKEDITOR.instances['content'];
        content = wysiwygEditor.getData();
      }

      if ($htmlEditorBlock.is(":visible")) {
        var htmlEditor = ace.edit("contentEditor");
        if($("body").hasClass("dark-theme")) {
          htmlEditor.setTheme("ace/theme/idle_fingers");
        }
        content = htmlEditor.getValue();
      }

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
    if (this?.data?.programmatic) { // options can be disabled with select lib select_redesigned.js
      return;
    }
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
      const targetsSelect = Select.get($(el));
      targetsSelect.disableOptions(selectedTargets);
    });
    Select.get($targetGroups).disableOptions(selectedTargets, true);
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
    $targetsOrder.append(createTargetRow$(targetId, contentBlock));
    selectContentBlock(contentBlock);
    scrollToTargetsBottom();
    moveAllRecipientsTargetToEnd();
    disableUsedTargetOptionsInOrder();
  }
});
