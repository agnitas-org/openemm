/*doc
---
title: Confirms
name: confirm-directives
category: Javascripts - Confirms
---

Confirmation Modals are usually returned by the server to a `data-form-confirm` or a `data-confirm` request.

```html
<form action="/">
  ...
  <button type="button" class="btn btn-primary" data-form-confirm>Submits the form and expects a modal</button>
  ...
</form>
```

Returned by the server

```html
<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-bs-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                <h4 class="modal-title">
                    Header
                </h4>
            </div>

            <form action="/action">
                <input type="hidden" name="action" value="finalActionId">
                <div class="modal-body">
                  <p>Description</p>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-danger js-confirm-negative" data-bs-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">button.Cancel</span>
                        </button>
                        <button type="button" class="btn btn-primary js-confirm-positive" data-bs-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text">Confirm</span>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
```

If the user closes the overlay via the `js-confirm-negative` buttons, nothing will happen. If the user instead clicks on the `js-confirm-positive` button, the modal form will be submitted and the servers response will be passed back to the original form.

Request-Response Cycle:
-> user clicks `data-form-confirm="id"` button
-> original form gets submitted with confirmation action id set
-> server responds with a confirm modal
-> user clicks `js-confirm-positive` button
-> confirmation form gets submitted
-> response data is passed to the original form (updated view)
*/

(() => {

  const Form =  AGN.Lib.Form;
  const Confirm = AGN.Lib.Confirm;
  const Page = AGN.Lib.Page;

  AGN.Lib.Action.new({'click':  '[data-confirm], .js-confirm'}, function() {
    // get the ajax handle for the confirmation form
    $.get(this.el.attr('href')).done(resp => {
      const $modal = $(resp).all('.modal');

      if ( $modal.length !== 1 ) {
        Page.render(resp);
        return;
      }

      Confirm.create(resp).done(resp => Page.render(resp));
    });
    this.event.preventDefault();
  });

  $(document).on('click', '[data-confirm-positive], .js-confirm-positive', function() {
    const $this = $(this);
    const confirm = Confirm.get($this);
    const $form = Form.getWrapper($this);

    if (confirm) {
      if ($form.exists()) {
        const form = Form.get($form);
        if ($this.is('[data-url]')) {
          form.setActionOnce($this.data('url'));
        }

        // get the ajax handle for the confirmation form
        form.jqxhr().done(resp => confirm.positive(resp));
      } else {
        const $choice = confirm.$modal.find('[name="confirm-choice"]');
        if ($choice.exists()) {
          confirm.positive($choice.val());
        } else {
          confirm.positive($this.data('confirm-positive'));
        }
      }
    }
  });

  $(document).on('click', '[data-confirm-negative], .js-confirm-negative', function() {
    const $this = $(this);
    Confirm.get($this)?.negative($this.data('confirm-negative'));
  });

})();