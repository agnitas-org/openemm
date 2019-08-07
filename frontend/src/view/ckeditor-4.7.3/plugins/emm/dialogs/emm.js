( function(){
	var exampleDialog = function(editor){
		return {
			title : agntagDialogTitle,
			minWidth : 320,
			minHeight : 160,
			buttons: [CKEDITOR.dialog.okButton, CKEDITOR.dialog.cancelButton],
            onOk:function() { editor.insertHtml(document.getElementById('agntags-dialog-frame').contentWindow.getResultValue()) },
			onLoad:function() {},
			onShow:function() {},
			onHide:function() {},
			onCancel: function() {},
			resizable: CKEDITOR.DIALOG_RESIZE_NONE,
			contents: [{
                id: 'Agn-tag-page',
                label: '',
                accessKey: '',
                elements:[{
                    type:'html',
                    id:'agnTags',
                    html: '<div style="width:300px;height:155px;"><iframe id="agntags-dialog-frame" scrolling="no" src="' + agntagDialogPage +
                          '" border="0" frameBorder="0" allowTransparency="true" style="width:300px; height:155px;"/></div>'}]
            }]
        }
    };

	CKEDITOR.dialog.add('emm', function(editor) {
		return exampleDialog(editor);
	});

})();