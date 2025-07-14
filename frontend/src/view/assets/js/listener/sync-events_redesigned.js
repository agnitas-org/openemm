/*doc
---
title: Active Sync
name: sync
category: Components - Active Sync
---

The `data-sync-from` and `[data-sync-to]` directives listen for change or click events on the element and transfer the data from one element to another. This can be especially helpful for syncing data from a large edit view in an overlay to a smaller element on the view.

All valid jquery selectors can be used, but it's preferable to use ids instead of multitarget selectors like classes - e.g. `[data-sync-from="#source"]`.

It is also possible to sync several elements at once - simply chain the selectors in both directives: `[data-sync-form="#sourceOne, #sourceTwo"]` and `[data-sync-to="#targetOne, #targetTwo"]`. This would sync the data from sourceOne to targetOne and from sourceTwo to targetTwo.

```htmlexample
<div class="form-column-3">
    <div>
        <label class="form-label" for="syncFromInput">Sync from here</label>
        <input type="text" name="" value="sync me" id="syncFromInput" class="form-control">
    </div>
    <div>
        <label class="form-label" for="syncToInput">To here</label>
        <input type="text" name="" value="" id="syncToInput" class="form-control" placeholder="to here">
    </div>
    <div class="d-flex">
        <button type="button" class="btn btn-primary mt-auto" id="sync-btn" data-sync-from="#syncFromInput" data-sync-to="#syncToInput">
            Sync
        </button>
    </div>
</div>
```
*/

(() => {

  const FormBuilder = AGN.Lib.FormBuilder.FormBuilder;
  const Editor = AGN.Lib.Editor;

  AGN.Lib.Action.new({'click change': '[data-sync-from]'}, function () {
    const sources = this.el.data('sync-from').split(/,\s?/);
    const targets = this.el.data('sync-to').split(/,\s?/);

    _.each(sources, (source, index) => {
      const $source = $(source);
      const $target = $(targets[index]);
      const val = $source.val();
      const id = $target.attr('id');

      if (window.Jodit?.instances[id]) {
        Jodit.instances[id].value = val;
      } else if (window.CKEDITOR && CKEDITOR.instances[id]) {
        CKEDITOR.instances[id].setData(val);
      } else if (Editor.exists($target)) {
        Editor.get($target).val(val);
      } else if ($target.data('select2')) {
        $target.select2('val', val);
      } else if (FormBuilder.isCreated(`#${id}`)) {
        FormBuilder.get(`#${id}`).setJson(FormBuilder.get(source).getJson());
      } else if ($source?.prop('type')?.trim() === 'checkbox') {
        $target.val($source.prop('checked'));
      } else {
        $target.val(val);
      }
    })
  });

})();