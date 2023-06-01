AGN.Lib.Controller.new("mailing-styles-new", function () {
    function updatePreview() {
        const $mailingForm = $('form#mailingStylesForm');
        const $previewForm = $('form#mailingPreviewForm');

        const $sources = $mailingForm.find('input[name^="styles["]');

        $.each($sources, function (index, source) {
            const $source = $(source);
            const name = $source.attr('name');
            const $destination = $previewForm.find('input[name="' + name + '"]');

            $destination.val($source.val());
        });

        $previewForm.submit();
    }

    this.addAction({submission: 'updatePreview'}, function () {
        updatePreview();
    });

    this.addAction({click: 'updateMailingPreview'}, function () {
        updatePreview();
    });

    this.addAction({click: 'saveMailingStyles'}, function () {
        $('form#mailingStylesForm').submit();
    });
});
