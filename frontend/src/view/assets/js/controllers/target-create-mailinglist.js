AGN.Lib.Controller.new('target-create-mailinglist', function () {

    this.addAction({click: 'select-mediatype'}, function() {
        var $form = this.el.closest('form');
        var $okButton = $form.find('#createMailinglistOkBtn');

        if (!$form.find('input:checked[name="mediatypes"]').length) {
            $okButton.attr('disabled', 'disabled');
        } else {
            $okButton.removeAttr('disabled');
        }
    });
});
