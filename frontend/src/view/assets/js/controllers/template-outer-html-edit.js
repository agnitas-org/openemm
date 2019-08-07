AGN.Lib.Controller.new('template-outer-html-edit', function() {
    this.addAction({
        click: 'updatePreview'
    }, function() {
        var $previewForm = $('#layoutPreviewForm');

        $previewForm.find('input[name="outerHTML"]')
            .val($('#templateOuterHTML').val());
        $previewForm.submit();
    });

    this.addAction({
        click: 'saveOuterHTML'
    }, function() {
        $('#gridTemplateForm').submit();
    });
});
