
CKEDITOR.dialog.add( 'showProtectedDialog', function( editor ) {

	return {
		title: 'Edit Protected Source',
		minWidth: 300,
		minHeight: 60,
		contents: [
			{
			id: 'info',
			label: 'Edit Protected Source',
			accessKey: 'I',
			elements: [
				{
				type: 'text',
				id: 'txtProtectedSource',
				label: 'Value',
				required: true,
				validate: function() {
					if ( !this.getValue() ) {
						alert( 'The value cannot be empty' );
						return false;
					}
					return true;
				},
				setup: function(widget) {
					this.setValue(widget.data.protectedValue);
				},
				commit: function(widget) {
					widget.setData('protectedValue', this.getValue());
				}
			}
			]
		}
		]
	};
} );