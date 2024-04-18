<%@ page contentType="text/html; charset=utf-8" import="java.util.Locale" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="CKEDITOR_PATH" value="${emm:ckEditorPath(pageContext.request)}" scope="page"/>

<script type="text/javascript">
    const baseUrl = "${pageContext.request.contextPath}";
    const isCKEditorActive = {};

    function toggleEditor(textAreaId, editorWidth, editorHeight, mailingId) {
        if (isEditorVisible(textAreaId)) {
            removeEditor(textAreaId);
        } else {
            createEditorExt(textAreaId, editorWidth, editorHeight, mailingId);
        }
    }

    function createEditorExt(textAreaId, editorWidth, editorHeight, mailingId, fullPage, isResizeNotEnabled, allowExternalScript) {
        let imageBrowserUrl = !!mailingId ? '<c:url value="/wysiwyg/image-browser.action?mailingID="/>' + mailingId : '';
        if (window.isRedesignedUI) {
          imageBrowserUrl = !!mailingId ? '<c:url value="/wysiwyg/image-browserRedesigned.action?mailingID="/>' + mailingId : '';
        }
        if (!isEditorVisible(textAreaId)) {

            const config = {
                customConfig: 'emm_config.js',
                fullPage: fullPage,
                toolbar: '${param.toolbarType}' ? '${param.toolbarType}' : 'EMM',
                width: editorWidth,
                height: editorHeight,
                language: '${emm:getLocale(pageContext.request).language}',
                baseHref: '<c:url value="/${CKEDITOR_PATH}/"/>',
                filebrowserImageBrowseUrl: imageBrowserUrl,
                filebrowserImageBrowseLinkUrl: imageBrowserUrl,
                filebrowserImageWindowWidth: window.isRedesignedUI ? '1200' : '700',
                filebrowserImageWindowHeight: '600',
                resize_enabled: !isResizeNotEnabled,
                mailingId: mailingId,
                on: {
                    instanceReady: function (event) {
                        if (fullPage) {
                            const editor = event.editor,
                                rules = {
                                    elements: {
                                        html: function (element) {
                                            const attrs = element.attributes;
                                            switch (attrs['data-cke-editable']) {
                                                case 'true':
                                                    attrs.contenteditable = 'true';
                                                    break;
                                                case '1':
                                                    delete attrs.contenteditable;
                                                    break;
                                            }
                                        }
                                    }
                                };
                            delete editor.dataProcessor.htmlFilter.elementsRules.html; //remove html rule from defaultHtmlFilterRulesForAll
                            delete editor.dataProcessor.dataFilter.elementsRules.html; //remove html rule from defaultHtmlFilterRulesForAll

                            editor.dataProcessor.htmlFilter.addRules(rules);
                            editor.dataProcessor.dataFilter.addRules(rules);
                        }
                    },
                    toHtml: function (event) {
                        if (fullPage && event.data) {
                            if (repairFragment(event.data.dataValue, event.editor)) {
                                // makes editor format the code and wrap it in full page tags
                                setTimeout(function () {
                                    event.editor.updateElement();
                                }, 1);
                            }
                        }
                    },
                    blur: function (event) {
                        event.editor.updateElement();
                    },
                    save: function(e) {
                        if (window.isRedesignedUI) {
                          $(e.sender.element.$).trigger('ckeditor-save');
                        } else {
                          $(document).trigger('ckeditor-save');
                        }
                        e.cancel();
                    }
                }
            };

            if (allowExternalScript) {
                const elements = _.merge({}, CKEDITOR.dtd);
                delete elements['script'];

                config.allowedContent = {
                    $1: {
                        elements: elements,
                        attributes: true,
                        styles: true,
                        classes: true
                    },
                    script: {
                        attributes: '!src'
                    }
                };

                config.disallowedContent = 'iframe; *[on*];';
            }

            CKEDITOR.replace(textAreaId, config);

            isCKEditorActive[textAreaId] = true;
        }
    }

    function repairFragment(fragment, editor) {
        if (isFragmentCorrect(fragment)) {
            return false;
        }

        eliminateNonFragmentTags(fragment, editor);

        return true;
    }

    function isFragmentCorrect(fragment) {
        const contents = filterHtmlComment(fragment.children);

        switch (contents.length) {
            case 0:
                // Fragment is empty.
                return true;

            case 1:
                // check for root element is html
                return contents[0].name &&
                    contents[0].name.toLowerCase() === 'html';

            case 2:
                // check for root element is doctype
                return contents[0].name &&
                    contents[0].name.toLowerCase() === '!doctype' &&
                    contents[1].name &&
                    contents[1].name.toLowerCase() === 'html';
        }

        return false;
    }

    function filterHtmlComment(children) {
        return children.filter(function(el) {
            if (el.value) {
                return !el.value.startsWith('[if') && !el.value.startsWith('<![endif]') && !el.value.startsWith('{cke_protected}');
            } else {
                return true;
            }
        });
    }

    function getNonBodyContentKeys() {
        const nonBodyContent = CKEDITOR.dtd.$nonBodyContent;

        //delete any element that has to be in body tag
        delete nonBodyContent['style'];

        return Object.keys(nonBodyContent).join('; ');
    }

    function eliminateNonFragmentTags(fragment, editor) {
        const filter = new CKEDITOR.filter();

        filter.allow({
            $1: {
                // Use the ability to specify elements as an object.
                elements: CKEDITOR.dtd,
                attributes: true,
                styles: true,
                classes: true
            }
        });
        filter.editor = editor;

        // removes all non body tags
        filter.disallow(getNonBodyContentKeys());
        filter.applyTo(fragment);
    }

    function removeEditor(textAreaId) {
        if (isEditorVisible(textAreaId)) {
            CKEDITOR.instances[textAreaId].destroy();
            isCKEditorActive[textAreaId] = false;
        }
    }

    function isEditorVisible(textAreaId) {
        if (isCKEditorActive[textAreaId] == undefined) {
            isCKEditorActive[textAreaId] = false;
        }
        return isCKEditorActive[textAreaId];
    }

    function removeAllEditors() {
        for (const textAreaId in isCKEditorActive) {
            if (isCKEditorActive[textAreaId]) {
                toggleEditor(textAreaId, 0, 0);
            }
        }
    }

    function openEditorsExist() {
        for (const textAreaId in isCKEditorActive) {
            if (isCKEditorActive[textAreaId]) {
                return true;
            }
        }
        return false;
    }
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/${CKEDITOR_PATH}/ckeditor.js"></script>
