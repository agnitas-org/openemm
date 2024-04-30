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
  <button type="button" class="btn btn-regular btn-primary" data-form-confirm="confirmActionId">Submits the form and expects a modal</button>
  ...
</form>
```

Returned by the server

```html
<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
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
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text">button.Cancel</span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
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

(function(){

  var Form =  AGN.Lib.Form,
      Confirm = AGN.Lib.Confirm,
      Action = AGN.Lib.Action,
      Page = AGN.Lib.Page;

  Action.new({'click':  '[data-confirm], .js-confirm'}, function() {
    var jqxhr;

    // get the ajax handle for the confirmation form
    jqxhr = $.get(this.el.attr('href'));
    jqxhr.done(function(resp) {
      var $resp = $(resp),
          $modal;

      $modal = $resp.
          filter('.modal').
          add($resp.find('.modal'));

      if ( $modal.length != 1 ) {
        Page.render(resp);
        return;
      }

      Confirm.create(resp).
        done(function(resp) {
          Page.render(resp);
        });
    });
    this.event.preventDefault();
  });

  $(document).on('click', '[data-confirm-positive], .js-confirm-positive', function() {
    var $this = $(this),
        confirm = Confirm.get($this),
        $form = Form.getWrapper($this);

    if (confirm) {
      var $saveChoice = confirm.$modal.find('[name="confirm-save-choice"]');
      if ($form.exists()) {
        // get the ajax handle for the confirmation form
        Form.get($form)
          .jqxhr()
          .done(function(resp) {
            // run positive callbacks
            confirm.positive(resp);
          });
      } else {
        if ($saveChoice.exists()) {
          confirm.positive({code: $this.data('confirm-positive'), saveChoice: $saveChoice[0].checked});
        } else {
          confirm.positive($this.data('confirm-positive'));
        }
      }
    }
  });

  $(document).on('click', '[data-confirm-negative], .js-confirm-negative', function() {
    var $this = $(this),
      confirm = Confirm.get($this);

    if (confirm) {
      var $saveChoice = confirm.$modal.find('[name="confirm-save-choice"]');
      if ($saveChoice.exists()) {
        confirm.negative({code: $this.data('confirm-negative'), saveChoice: $saveChoice[0].checked});
      } else {
        confirm.negative($this.data('confirm-negative'));
      }
    }
  });

})();
