CKEDITOR.plugins.add('emm',
{
    beforeInit: function(editor) {
        var dataProcessor = editor.dataProcessor;

        if (dataProcessor) {
            var toHtmlMethod = dataProcessor.toHtml;
            var toDataMethod = dataProcessor.toDataFormat;
            dataProcessor.toHtml = function(data) {
                data = data.replace(
                    /=\s*('|")(\S*|\s*)\[(agn[^\s\]\/]+)(\s*(?:[^'"\]]|'.*?'|".*?")*)([\/]?)]\s*\1/gi,
                    function(whole, equote, beforeTag, name, attributes, close) {
                        if (attributes) {
                            attributes = attributes.replace(
                                /\s*([^\s\]\/=]+)\s*(?:=\s*([^\s\]\/='"][^\s\]\/=]*|'.*?'|".*?"))/g,
                                function(whole, name, value) {
                                    // Remove quotation marks
                                    name = name.replace(/'"/g, '');
                                    if (value) {
                                        // Trim embracing quotation marks
                                        value = value.replace(/^('|")(.*?)\1$/, "$2");
                                        // Escape double quotation marks within
                                        value = value.replace(/"/g, '&quot;');
                                        // Remove single quotation marks
                                        value = value.replace(/'/g, '');
                                        // Embrace with single quotation marks
                                        return " " + name + "='" + value + "'";
                                    } else {
                                        return " " + name;
                                    }
                                }
                            );
                        } else {
                            attributes = '';
                        }

                        if (!close) {
                            close = '';
                        }

                        // Embrace with double quotation marks
                        return '="' + beforeTag + '[' + name + attributes + close + ']"';
                    }
                );
                arguments[0] = data;
                return toHtmlMethod.apply(this, arguments);
            };

            dataProcessor.toDataFormat = function () {
                var content = toDataMethod.apply(this, arguments);
                var substring = '<body></body>';
                if (content.indexOf(substring) != -1) {
                    content = "";
                }
                return content;
            };
        }
    },

    init: function(editor) {
        var pluginName = 'emm';

        CKEDITOR.dialog.add(pluginName, this.path + 'dialogs/emm.js');

        editor.addCommand(pluginName, new CKEDITOR.dialogCommand(pluginName));
        editor.ui.addButton('AGNTag',
            {
                label: agntagDialogTooltip,
                command: pluginName,
                icon: CKEDITOR.plugins.getPath(pluginName) + 'emm.gif'
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
