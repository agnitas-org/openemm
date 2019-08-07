<%@ page language="java" contentType="text/html; charset=utf-8" import="java.util.Locale"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="CKEDITOR_PATH" value="${emm:ckEditorPath(pageContext.request)}" scope="page"/>

<script type="text/javascript">
    var baseUrl = "${pageContext.request.contextPath}";

    var agntagDialogPage = '<html:rewrite page="/${CKEDITOR_PATH}/plugins/emm/dialogs/emm-tags-dialog-content-frame.jsp;jsessionid=${pageContext.session.id}"/>';
    var agntagDialogTitle = '<bean:message key="htmled.agntagsWindowTitle" />';
    var agntagDialogTooltip = '<bean:message key="htmled.agntagsButtonTooltip" />';

    var isCKEditorActive = {};

    function toggleEditor(textAreaId, editorWidth, editorHeight, mailingId) {
        if (isEditorVisible(textAreaId)) {
            removeEditor(textAreaId);
        }
        else {
            createEditor(textAreaId, editorWidth, editorHeight, mailingId);
        }
    }

    function createEditor(textAreaId, editorWidth, editorHeight, mailingId) {
         createEditorExt(textAreaId, editorWidth, editorHeight, mailingId, false);
    }

    function createEditorExt(textAreaId, editorWidth, editorHeight, mailingId, fullPage, isResizeNotEnabled) {
      var imageBrowserUrl = !!mailingId ? '<html:rewrite page="/${CKEDITOR_PATH}/emm-image-browser.jsp?mailingID="/>' + mailingId : '';
      if (!isEditorVisible(textAreaId)) {
        CKEDITOR.replace(textAreaId, {
          customConfig: 'emm_config.js',
          fullPage: fullPage,
          toolbar: '${param.toolbarType}' ? '${param.toolbarType}' : 'EMM',
          width: editorWidth,
          height: editorHeight,
          language: '<%= ((Locale)session.getAttribute(org.apache.struts.Globals.LOCALE_KEY)).getLanguage() %>',
          baseHref: '<c:url value="/${CKEDITOR_PATH}/"/>',
          filebrowserImageBrowseUrl: imageBrowserUrl,
          filebrowserImageBrowseLinkUrl: imageBrowserUrl,
          filebrowserImageWindowWidth: '700',
          filebrowserImageWindowHeight: '600',
          resize_enabled: !isResizeNotEnabled,
          on: {
            toHtml: function (event) {
              if (fullPage && event.data) {
                if (repairFragment(event.data.dataValue)) {
                  // makes editor format the code and wrap it in full page tags
                  setTimeout(function() {
                    event.editor.updateElement();
                  }, 1);
                }
              }
            },
            blur: function (event) {
              event.editor.updateElement()
            }
          }
        });

        isCKEditorActive[textAreaId] = true;
      }
    }

    function repairFragment(fragment) {
      if (isFragmentCorrect(fragment)) {
        return false;
      }

      eliminateNonFragmentTags(fragment);

      return true;
    }

    function isFragmentCorrect(fragment) {
      switch (fragment.children.length) {
        case 0:
          // Fragment is empty.
          return true;

        case 1:
          // check for root element is html
          return fragment.children[0].name &&
            fragment.children[0].name.toLowerCase() === 'html';

        case 2:
          // check for root element is doctype
          return fragment.children[0].name &&
            fragment.children[0].name.toLowerCase() === '!doctype' &&
            fragment.children[1].name &&
            fragment.children[1].name.toLowerCase() === 'html';
      }

      return false;
    }

    function getNonBodyContentKeys() {
      var nonBodyContent = CKEDITOR.dtd.$nonBodyContent;

      //delete any element that has to be in body tag
      delete nonBodyContent['style'];

      return Object.keys(nonBodyContent).join('; ');
    }

    function eliminateNonFragmentTags(fragment) {
      var filter = new CKEDITOR.filter();

      filter.allow({
        $1: {
          // Use the ability to specify elements as an object.
          elements: CKEDITOR.dtd,
          attributes: true,
          styles: true,
          classes: true
        }
      });

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
        for (var textAreaId in isCKEditorActive) {
            if (isCKEditorActive[textAreaId]) {
                toggleEditor(textAreaId, 0, 0);
            }
        }
    }

    function openEditorsExist() {
        for (var textAreaId in isCKEditorActive) {
            if (isCKEditorActive[textAreaId]) {
                return true;
            }
        }
        return false;
    }
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/${CKEDITOR_PATH}/ckeditor.js"></script>
