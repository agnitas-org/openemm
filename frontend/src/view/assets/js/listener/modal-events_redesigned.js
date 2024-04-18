/*doc
---
title: Modals
name: modal
category: Javascripts - Modals
---

Modals can be triggered using the `data-modal` attribute. The attribute defines the template which should be used for the modal.

The modal template is looked up from `AGN.Opt.Templates['my-template-key']`, which automatically adds all script tags of the type "text/x-mustache-template" to the lookup table by using the `id` of the script tag as the template key.

A click on an element with the `data-dismiss="modal"` attribute will close the modal again.

```htmlexample
<a href="#" class="btn btn-regular btn-success" data-modal="simple-modal">Trigger simple modal</a>

<script id="simple-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">Title</h4>
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
<a href="#" class="btn btn-regular btn-success" data-modal="variable-modal" data-modal-set="title: 'Custom Title 1', content: true">Trigger Modal 1</a>

<a href="#" class="btn btn-regular btn-success" data-modal="variable-modal" data-modal-set="title: 'Custom Title 2', content: false">Trigger Modal 2</a>

<script id="variable-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close-icon close" data-dismiss="modal">
                        <i aria-hidden="true" class="icon icon-times-circle"></i>
                    </button>
                    <h4 class="modal-title">{{= title }}</h4>
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

(function() {
  var Action = AGN.Lib.Action,
      Modal  = AGN.Lib.Modal,
      Helpers = AGN.Lib.Helpers;

  Action.new({'click': '[data-modal]'}, function() {
    const template = this.el.data('modal');
    let opts = this.el.data('modal-set');
    opts = typeof opts === 'object' ? opts : Helpers.objFromString(opts);

    Modal.fromTemplate(template, opts);
  });

  $(document).on('hidden.bs.modal', '.modal', function() {
    var $modal = $(this);

    $modal.trigger('modal:close');

    setTimeout(function() {
      $modal.remove();
    }, 100);
  });

  $(document).on('shown.bs.modal', '.modal', function() {
    const $modals = $('.modal.show');
    if ($modals.length > 1) {
      fixMultipleModalsBackdropIssue($modals.last());
    }
  });

  function fixMultipleModalsBackdropIssue($modal) {
    $('.modal-backdrop.show').last().css('z-index', $modal.css('z-index'));
  }

  // Fix for CKEditor + Bootstrap Modal (IE is only affected) issue with dropdowns on the toolbar
  // TODO: recheck this issue with Bootstrap 5
  // $.fn.modal.Constructor.prototype.enforceFocus = function() {
  //   var self = this;
  //   $(document).on('focusin.modal', function (e) {
  //     // Adding additional condition '$(e.target.parentNode).hasClass('cke_contents cke_reset')' to
  //     // avoid setting focus back on the modal window.
  //     if (self.$element[0] !== e.target && !self.$element.has(e.target).length && $(e.target.parentNode).hasClass('cke_contents cke_reset')) {
  //       self.$element.focus()
  //     }
  //   })
  // };

})();
