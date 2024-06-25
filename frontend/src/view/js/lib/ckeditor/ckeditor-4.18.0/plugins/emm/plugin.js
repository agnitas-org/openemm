(function () {
    var imagesMapCache;
    CKEDITOR.plugins.add('emm', {
        beforeInit: function (editor) {
            var dataProcessor = editor.dataProcessor;

            if (dataProcessor) {
                var toHtmlMethod = dataProcessor.toHtml;
                var toDataMethod = dataProcessor.toDataFormat;
                dataProcessor.toHtml = function (data) {
                    data = replaceAgnImageTags(editor, data);
                    arguments[0] = AGN.Lib.Helpers.escapeAgnTags(data);
                    return toHtmlMethod.apply(this, arguments);
                };

                dataProcessor.toDataFormat = function () {
                    var content = toDataMethod.apply(this, arguments);
                    var emptyTemplate = '<html>\n<head>\n\t<title></title>\n</head>\n<body></body>\n</html>\n';

                    if (content === emptyTemplate) {
                        return "";
                    }

                    content = processUrls(editor, content);
                    return AGN.Lib.Helpers.unescapeAgnTags(content);
                };
            }
        },

        init: function (editor) {
            var commandName = 'showAgnTags';

            editor.addCommand(commandName, {
                exec: function (editor) {
                    AGN.Lib.Confirm.request(AGN.url(window.isRedesignedUI ? '/wysiwyg/dialogs/agn-tagsRedesigned.action' : '/wysiwyg/dialogs/agn-tags.action')).done(function (code) {
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
                    a: function (element) {
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
            prepareImagesMapToConfig(editor);
        }
    });

    function replaceAgnImageTags(editor, data) {
        var imagesUrlsMap = editor.config.emmMailingImages;
        if(!imagesUrlsMap) {
            return data;
        }
        var imgTags = data.match(/\[agnIMAGE\s+name=(?:(?:'.+\.\w+')|(?:".+\.\w+"))\]/g);
        if(!imgTags) {
            return data;
        }
        imgTags.forEach(function (tagText) {
            var imgName = tagText.match(/name=['"](.+\.\w+)['"]/)[1];
            if(imgName) {
                var imageUrl = imagesUrlsMap[imgName];
                if(imageUrl) {
                    data = data.replace(tagText, imageUrl);
                }
            }
        });

        return data;
    }

    function processUrls(editor, content) {
        var imagesUrlsMap = editor.config.emmMailingImages;
        if(!imagesUrlsMap) {
            return content;
        }
        var urls = content.match(/(?:https?:\/\/)[\w\/\-.~:?#@!$&'()*+,;=]+/g);
        if(!urls) {
            return content;
        }
        var urlsImagesNamesMap = {};
        Object.keys(imagesUrlsMap).forEach(function (key) {
            urlsImagesNamesMap[imagesUrlsMap[key]] = key;
        });
        urls.forEach(function (url) {
            var normalizedUrl = url.replace(/(\/mediapool_element)(\/\d+)?(\/\d+\/\d+\/0\/\d+)(\.\w+)?/, function (match, $1, $2, $3) {
                return $1 + $3;
            });
            var imageName = urlsImagesNamesMap[normalizedUrl];
            if(imageName) {
                content = content.replace(url, '[agnIMAGE name=\'' + imageName + '\']');
            }
        });

        return content.replaceAll('&amp;', '&');
    }

    function prepareImagesMapToConfig(editor) {
        if(imagesMapCache) {
            editor.config.emmMailingImages = imagesMapCache;
            return;
        }
        var mailingId = editor.config.mailingId || -1;
        $.ajax({
            url: AGN.url('/wysiwyg/images/names-urls.action'),
            type: 'POST',
            async: false,
            data: {mi: mailingId},
            success: function(json) {
                imagesMapCache = json;
                editor.config.emmMailingImages = json;
            }
        });
    }
})();
