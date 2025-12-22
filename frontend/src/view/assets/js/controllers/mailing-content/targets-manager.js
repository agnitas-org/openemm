(() => {
  const Template = AGN.Lib.Template;
  const Select = AGN.Lib.Select;
  const TARGET_ROW_SELECTOR = '.target-row';

  class TargetsManager {

    constructor(editor) {
      this.editor = editor;
    }

    get sortableOptions() {
      return {
        scroll: false,
        handle: ".target-row-handle",
        cursor: "move",
        items: `${TARGET_ROW_SELECTOR}:not(:has(option[value="0"][selected]))`,
        stop: this.updateSortable
      }
    }

    init() {
      this.$targetsOrder = $('#targets-order').sortable(this.sortableOptions);
      this.editor.currentDynTag.contentBlocks.forEach(block => this.addTargetRow(block));
      this.updateSortable();
      this.controlAddTargetBtn();
    }

    changeTarget($el) {
      this.editor.currentDynTag.changeTargetGroup(this.getContentBlockIdFromTargetRow($el), parseInt($el.val()))
      this.disableUsedTargetOptionsInOrder();
      this.moveAllRecipientsTargetToEnd();
      this.editor.updateTargetsOptions();
      this.editor.currentDynTag.updatePreview();
    }

    updateSortable() {
      this.updateTargetsOrder();
      this.editor.updateTargetsOptions();
      this.disableUsedTargetOptionsInOrder();
      this.moveAllRecipientsTargetToEnd();
    }

    updateSortableTargetIndex($targetRow) {
      const index = this.editor.currentDynTag.getContentBlockById(parseInt($targetRow.attr('id'))).index;
      $targetRow.find('.input-group-text').text(index);
    }

    updateTargetsOrder() {
      const newOrder = this.$targetsOrder.find(TARGET_ROW_SELECTOR).toArray().map(row => $(row).attr('id'));
      this.editor.currentDynTag.changeOrder(newOrder);
      this.$targetsOrder.find(TARGET_ROW_SELECTOR).each((i, targetRow) => this.updateSortableTargetIndex($(targetRow)))
    }

    deleteContentBlock($el) {
      const $toRemove = $el.closest(TARGET_ROW_SELECTOR);
      const blockIdToRemove = this.getContentBlockIdFromTargetRow($toRemove);

      if (this.isContentBlockUnderEdit(blockIdToRemove)) {
        this.editor.currentContentBlock = undefined;
        this.editor.selectContentBlock(this.getContentBlockToReplaceRemoved($toRemove));
      }
      this.editor.currentDynTag.remove(blockIdToRemove);
      $toRemove.remove();
      this.updateSortable();
      this.editor.currentDynTag.updatePreview();
      this.controlAddTargetBtn();
    }

    getContentBlockIdFromTargetRow($el) {
      return parseInt($el.closest(TARGET_ROW_SELECTOR).attr('id'));
    }

    getContentBlockToReplaceRemoved($toRemove) {
      const $target = $toRemove.next(TARGET_ROW_SELECTOR).length
        ? $toRemove.next(TARGET_ROW_SELECTOR)
        : $toRemove.prev(TARGET_ROW_SELECTOR);
      $target.find('.input-group-text').addClass('active').removeClass('disabled');
      return this.editor.currentDynTag.getContentBlockById(this.getContentBlockIdFromTargetRow($target));
    }

    controlAddTargetBtn() {
      const targetsSelect = Select.get(this.$targetsOrder.find('select:first'));
      $('[data-action="add-content-block"]').toggle(this.getUsedTargets().length < targetsSelect.$options.length); // hide 'add target' button if all targets in use
    }

    getUsedTargets() {
      return this.$targetsOrder.find('select').map((i, select) => $(select).val())?.toArray();
    }

    disableUsedTargetOptionsInOrder() {
      const usedTargets = this.getUsedTargets();
      _.each(this.$targetsOrder.find('select'), el => Select.get($(el)).disableOptions(usedTargets));
    }

    addTargetGroup() {
      const contentBlock = this.editor.currentDynTag.createNewContentBlock();
      const usedTargets = this.getUsedTargets();
      const targetSelect = Select.get(this.addTargetRow(contentBlock).find('select'));
      targetSelect.disableOptions(usedTargets, true);                     // disable already selected targets in a newly created target
      this.disableUsedTargetOptionsInOrder();                             // disable added target group in already existing target selects
      contentBlock.targetId = parseInt(targetSelect.getSelectedValue());
      this.moveAllRecipientsTargetToEnd();
      this.editor.updateTargetsOptions();
      this.editor.currentDynTag.updatePreview();
      this.controlAddTargetBtn();
      return contentBlock;
    }

    moveAllRecipientsTargetToEnd() {
      this.$targetsOrder
        .find('select option[value="0"]:selected')
        .closest(TARGET_ROW_SELECTOR)
        .appendTo(this.$targetsOrder);
      this.updateTargetsOrder();
    }

    addTargetRow(contentBlock) {
      const $targetRow = Template.dom('mailing-content-target-template', {
        ...contentBlock,
        isCurrent: this.isContentBlockUnderEdit(contentBlock.uniqueId)
      });
      this.$targetsOrder.append($targetRow);
      this.addUpdateSelectedAttrOnChangeHandler($targetRow);
      AGN.Lib.CoreInitializer.run('select', $targetRow);
      return $targetRow;
    }

    addUpdateSelectedAttrOnChangeHandler($targetRow) { // select2 doesn't do this by default
      $targetRow.find('select').on('change, change.select2', function() {
        const value = $(this).val();
        $(this).find('option').each((i, opt) => $(opt).attr('selected', $(opt).val() === value ? 'selected' : null));
      });
    }

    isContentBlockUnderEdit(contentBlockId) {
      return this.editor.currentContentBlock.uniqueId === contentBlockId;
    }
  }

  AGN.Lib.MailingContent.TargetsManager = TargetsManager;
})();
