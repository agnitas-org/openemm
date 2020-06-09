CKEDITOR.plugins.add('emm', {
    beforeInit: function(editor) {
        var dataProcessor = editor.dataProcessor;

        if (dataProcessor) {
            var toHtmlMethod = dataProcessor.toHtml;
            var toDataMethod = dataProcessor.toDataFormat;
            dataProcessor.toHtml = function(data) {
                data = data.replace(
                    //regex for searching agntags in HTML file. Note that agn tags can be within html-tag's attribute
                    /\[agn[A-Z0-9]+(?:[^'"\]]|'.*?'|".*?")+]/g,
                    function(whole) {
                      return _.escape(whole);
                    }
                );
                arguments[0] = data;
                return toHtmlMethod.apply(this, arguments);
            };

            dataProcessor.toDataFormat = function () {
                var content = toDataMethod.apply(this, arguments);
                var substring = '<body></body>';

                if (content.indexOf(substring) !== -1) {
                    return "";
                } else {
                  return content.replace(
                    //regex for searching agntags in HTML file. Note that agn tags can be within html-tag's attribute
                    /\[agn[A-Z0-9]+(?:(?!&#39;|&quot;)[^'"\]]|&#39;.*?&#39;|&quot;.*?&quot;)*]/g,
                    function(whole) {
                      return _.unescape(whole);
                    }
                  );
                }
            };
        }
    },

    init: function(editor) {
        var commandName = 'showAgnTags';

        editor.addCommand(commandName, {
            exec: function(editor) {
                AGN.Lib.Confirm.request(AGN.url('/wysiwyg/dialogs/agn-tags.action')).done(function(code) {
                    if (code) {
                        editor.insertHtml(code);
                    }
                });
            }
        });

        editor.ui.addButton('AGNTag', {
            label: t('wysiwyg.dialogs.agn_tags.tooltip'),
            command: commandName,
            icon: this.path + 'emm.gif'
        });

        var HREF = "href";
        var filteringRule = {
            elements: {
                a: function(element) {
                    for (var attr in element.attributes) {
                        if (attr.length > HREF.length && attr.lastIndexOf(HREF) >= 0) {
                            if (attr.indexOf('data-cke-') < 0) {
                                delete element.attributes[attr];
                            }
                        }
                    }
                }
            }
        };
        editor.dataProcessor.dataFilter.addRules(filteringRule);
        editor.dataProcessor.htmlFilter.addRules(filteringRule);

        // adding callback for each tag
        editor.filter.addElementCallback(function (element) {
            var ignoredTags = editor.config.ignoredTags;
            if (Array.isArray(ignoredTags) && ignoredTags.indexOf(element.name) > -1) {
              return CKEDITOR.FILTER_SKIP_TREE;
            }
        });
    }
});
