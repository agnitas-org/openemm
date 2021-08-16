/*
 *  "showprotected" CKEditor plugin
 *  
 *  Created by Matthew Lieder (https://github.com/IGx89)
 *  
 *  Licensed under the MIT, GPL, LGPL and MPL licenses
 *  
 *  Icon courtesy of famfamfam: http://www.famfamfam.com/lab/icons/mini/
 */

// TODO: configuration settings
// TODO: improve copy/paste behavior (tooltip is wrong after paste)

CKEDITOR.plugins.add( 'showprotected', {
	requires: 'dialog,fakeobjects',

	onLoad: function() {
		// CKEDITOR.addCss("span.cke_protected { background-color: #00ff00; border: solid 1px #aaaaaa; border-radius: 2px; }");
	},

	init: function( editor ) {
		CKEDITOR.dialog.add( 'showProtectedDialog', this.path + 'dialogs/protected.js' );

		editor.widgets.add( 'protected-placeholder', {
			// Widget code.
			dialog: 'showProtectedDialog',
			pathName: 'Protected placeholder',

			downcast: function() {
				return new CKEDITOR.htmlParser.text(this.data.protectedValue);
			},

			init: function() {
				// Note that placeholder markup characters are stripped for the name.
				this.setData('protectedValue', this.element.getText());
			},

			data: function() {
				this.element.setText(this.data.protectedValue);
			},

			getLabel: function() {
				return this.editor.lang.widget.label.replace( /%1/, this.data.protectedValue + ' ' + this.pathName );
			}
		} );

		editor.on( 'doubleclick', function( evt ) {
			var element = evt.data.element;

			if (element.is('span.cke_protected')) {
				evt.data.dialog = 'showProtectedDialog';
			}
		} );
	},

	afterInit: function( editor ) {
		// Register a filter to displaying placeholders after mode change.

		var dataProcessor = editor.dataProcessor,
			dataFilter = dataProcessor && dataProcessor.dataFilter;

		if ( dataFilter ) {
			dataFilter.addRules( {
				comment: function(commentText) {
					if (commentText.startsWith(CKEDITOR.plugins.showprotected.protectedSourceMarker)) {
						var rawValue = CKEDITOR.plugins.showprotected.decodeProtectedSource(commentText);

						if (editor.config.fullPage) {
							if (rawValue.startsWith('<meta') || rawValue.startsWith('<script') || rawValue.startsWith('<!--')) {
								return null;
							}
						}

						var widgetTextSpan = new CKEDITOR.htmlParser.element( 'span', {'class': 'cke_placeholder'});

						widgetTextSpan.add( new CKEDITOR.htmlParser.text(_.escape(rawValue)) );

						return editor.widgets.wrapElement(widgetTextSpan, 'protected-placeholder');
					}
					
					return null;
				}
			} );
		}
	}
} );

/**
 * Set of showprotected plugin's helpers.
 *
 * @class
 * @singleton
 */
CKEDITOR.plugins.showprotected = {
		
	protectedSourceMarker: '{cke_protected}',
		
	decodeProtectedSource: function( protectedSource ) {
		if(protectedSource.indexOf('%3C!--') == 0) {
			return decodeURIComponent(protectedSource).replace( /<!--\{cke_protected\}([\s\S]+?)-->/g, function( match, data ) {
                return decodeURIComponent( data );
			} );
		} else {
			return decodeURIComponent(protectedSource.substr(CKEDITOR.plugins.showprotected.protectedSourceMarker.length));
		}
	}

};
