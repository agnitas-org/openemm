/*doc
---
title: Active Sync Directive
name: sync-directives
parent: directives
---

The `data-sync-from` and `data-sync-to` directives listen for change or click events on the element and transfer the data from one element to another. This can be especially helpful for syncing data from a large edit view in an overlay to a smaller element on the view.

All valid jquery selectors can be used, but it's preferable to use ids instead of multitarget selectors like classes - e.g. `data-sync-from="#source"`.

It is also possible to sync several elements at once - simply chain the selectors in both directives: `data-sync-form="#sourceOne, #sourceTwo"` and `data-sync-to="#targetOne, #targetTwo"`. This would sync the data from sourceOne to targetOne and from sourceTwo to targetTwo.

```htmlexample
<div class="form-group">
    <div class="col-sm-4">
        <label class="control-label">
            Sync from here
        </label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
          <div class="input-group-controls">
            <input type="text" name="" value="sync me" id="syncFromInput" class="form-control">
          </div>
          <div class="input-group-btn">
            <button type="button" class="btn btn-regular btn-primary"
                    data-sync-from="#syncFromInput" data-sync-to="#syncToInput">
                Sync
            </button>
          </div>
        </div>
    </div>
</div>

<div class="form-group">
    <div class="col-sm-8 col-sm-push-4">
        <input type="text" name="" value="" id="syncToInput" class="form-control">
    </div>
</div>
```
*/



(function(){

  var Action = AGN.Lib.Action;

  Action.new({'click change': '[data-sync-from]'}, function() {
    var sources = this.el.data('sync-from').split(/,\s?/),
        targets = this.el.data('sync-to').split(/,\s?/);

    _.each(sources, function(source, index) {
      var $source = $(source),
          $target = $(targets[index]),
          val = $source.val(),
          id = $target.attr('id'),
          editor = $target.data('_editor');

      if (window.CKEDITOR && CKEDITOR.instances[id]) {
        CKEDITOR.instances[id].setData(val);
      } else if (editor) {
         editor.val(val);
      } else {
        $target.val(val);
      }

    })


  });

  // prevent navigation on full sync action links
  $(document).on('click',
    'a[data-sync-from]',
    function(e) {
      e.preventDefault();
  });

})();
