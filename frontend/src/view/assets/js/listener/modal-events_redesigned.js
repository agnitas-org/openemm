/*doc
---
title: Modals
name: modal
category: Components - Modals
---

Modals can be triggered using the `data-modal` attribute. The attribute defines the template which should be used for the modal.

The modal template is looked up from `AGN.Opt.Templates['my-template-key']`, which automatically adds all script tags of the type "text/x-mustache-template" to the lookup table by using the `id` of the script tag as the template key.

A click on an element with the `data-bs-dismiss="modal"` attribute will close the modal again.

```htmlexample
<a href="#" class="btn btn-primary" data-modal="simple-modal">Trigger simple modal</a>

<script id="simple-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                  <h1 class="modal-title">Title</h1>

                  <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                  </button>
                </div>
                <div class="modal-body">
                    Modal Content
                </div>
            </div>
        </div>
    </div>
</script>
```
*/

/*doc
---
title: Modals with variable content
name: modal_01_variable
parent: modal
---

For a modal with custom content, you can use mustache syntax on the template . The variables are read from the `data-modal-set` attribute on the trigger

```htmlexample
<a href="#" class="btn btn-primary" data-modal="variable-modal" data-modal-set="title: 'Custom Title 1', content: true">Trigger Modal 1</a>

<a href="#" class="btn btn-primary" data-modal="variable-modal" data-modal-set="title: 'Custom Title 2', content: false">Trigger Modal 2</a>

<script id="variable-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                  <h1 class="modal-title">{{= title }}</h1>

                  <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                  </button>
                </div>

                {{ if (content) { }}
                  <div class="modal-body">
                      Modal Content
                  </div>
                {{ } }}
            </div>
        </div>
    </div>
</script>
```
*/

(() => {

  AGN.Lib.Action.new({click: '[data-modal]'}, function() {
    const template = this.el.data('modal');
    let opts = this.el.data('modal-set');
    opts = typeof opts === 'object' ? opts : AGN.Lib.Helpers.objFromString(opts);

    AGN.Lib.Modal.fromTemplate(template, opts);
  });

  $(document).on('hidden.bs.modal', '.modal', function() {
    const $modal = $(this);

    $modal.trigger('modal:close');
    setTimeout(() => $modal.remove(), 100);
  });

  $(document).on('shown.bs.modal', '.modal', function() {
    setTimeout(() => $(this).trigger('modal:open'), 0);

    const $modals = $('.modal.show');
    if ($modals.length > 1) {
      fixMultipleModalsBackdropIssue($modals.last());
    }
  });

  function fixMultipleModalsBackdropIssue($modal) {
    $('.modal-backdrop.show').last().css('z-index', $modal.css('z-index'));
  }

})();
