<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingWizardForm" type="org.agnitas.web.MailingWizardForm"--%>

<c:set var="ACTION_TEXTMODULE" value="<%= MailingWizardAction.ACTION_TEXTMODULE %>"/>
<c:set var="ACTION_TEXTMODULE_ADD" value="<%= MailingWizardAction.ACTION_TEXTMODULE_ADD %>"/>
<c:set var="ACTION_TEXTMODULE_SAVE" value="<%= MailingWizardAction.ACTION_TEXTMODULE_SAVE %>"/>
<c:set var="ACTION_FINISH" value="<%= MailingWizardAction.ACTION_FINISH %>" />

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp">
    <jsp:param name="toolbarType" value="${emm:isCKEditorTrimmed(pageContext.request) ? 'Classic' : 'EMM'}"/>
</jsp:include>

<c:set var="MessageMoveUp"><bean:message key="mailing.content.moveUp"/></c:set>
<c:set var="MessageMoveDown"><bean:message key="mailing.content.moveDown" /></c:set>

<agn:agnForm action="/mwTextmodule" id="wizard-step-8" data-form="resource">
    <html:hidden property="action" value="${ACTION_TEXTMODULE}"/>
    <html:hidden property="contentID"/>
    <html:hidden property="keepForward" value="${not empty workflowId and workflowId gt 0 ? true : false}"/>

    <div class="col-md-10 col-md-push-1 col-lg-8 col-lg-push-2">
        <div class="tile">
            <div class="tile-header">
                <h2 class="headline">
                    <i class="icon icon-file-o"></i>
                    <bean:message key="mailing.Wizard" />
                </h2>
                <ul class="tile-header-actions">
                    <li class="">
                        <ul class="pagination">
                            <li>
                                <a href="#" data-form-action="previous">
                                    <i class="icon icon-angle-left"></i>
                                    <bean:message key="button.Back" />
                                </a>
                            </li>
                            <li class="disabled"><span>1</span></li>
                            <li class="disabled"><span>2</span></li>
                            <li class="disabled"><span>3</span></li>
                            <li class="disabled"><span>4</span></li>
                            <li class="disabled"><span>5</span></li>
                            <li class="disabled"><span>6</span></li>
                            <li class="disabled"><span>7</span></li>
                            <li class="active"><span>8</span></li>
                            <li class="disabled"><span>9</span></li>
                            <li class="disabled"><span>10</span></li>
                            <li class="disabled"><span>11</span></li>
                            <li>
                                <a href="#" data-form-action="${ACTION_TEXTMODULE}">
                                    <bean:message key="button.Proceed" />
                                    <i class="icon icon-angle-right"></i>
                                </a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
            <div class="tile-notification tile-notification-info">
                <bean:message key="mailing.Text_Module"/>: ${mailingWizardForm.dynName}
            </div>
            <div class="tile-content tile-content-forms">
                <c:set var="position" value="1"/>

                <c:forEach var="content" items="${mailing.dynTags[mailingWizardForm.dynName].dynContent}">
                    <c:set var="index" value="${content.key}"/>
                    <c:set var="tag" value="${content.value}"/>
                    <c:set var="editorId" value="content[${index}].dynContent"/>

                    <div class="inline-tile">
                        <div class="inline-tile-header">
                            <h2 class="headline-static"><bean:message key="target.Target"/></h2>

                            <html:select styleClass="form-control js-select" property='content[${index}].targetID' size="1">
                                <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                                <logic:notEmpty name="targets" scope="request">
                                    <c:forEach var="dbTarget" items="${targets}">
                                        <html:option value="${dbTarget.id}">${dbTarget.targetName}</html:option>
                                    </c:forEach>
                                </logic:notEmpty>
                            </html:select>

                            <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                                <ul class="inline-tile-header-nav">
                                    <li class="active">
                                        <a href="#" data-toggle-tab="#tab-mailingContentViewCode${index}">
                                            <bean:message key="HTML"/>
                                        </a>
                                    </li>
                                    <li>
                                        <a href="#" data-toggle-tab="#tab-mailingContentViewHtml${index}">
                                            <bean:message key="mailingContentHTMLEditor"/>
                                        </a>
                                    </li>
                                </ul>
                            </logic:equal>

                            <ul class="inline-tile-header-actions">
                                <c:if test="${len > 1}">
                                    <c:if test="${position > 1}">
                                        <li>
                                            <a href="#" data-form-set="contentID: ${tag.dynOrder}" data-form-action="textmodule_move_up" data-tooltip="${MessageMoveUp}">
                                                <i class="icon icon-angle-double-up"></i>
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:if test="${position < len}">
                                        <li>
                                            <a href="#" data-form-set="contentID: ${tag.dynOrder}" data-form-action="textmodule_move_down" data-tooltip="${MessageMoveDown}">
                                                <i class="icon icon-angle-double-down"></i>
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:set var="position" value="${position + 1}"/>
                                </c:if>
                            </ul>
                        </div>

                        <div class="inline-tile-content">
                            <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                                <div id="tab-mailingContentViewCode${index}">
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <div id="${fn:replace(fn:replace(fn:replace(editorId, '[', '_'), ']', '_'), '.', '_')}Editor" class="form-control"></div>
                                        </div>
                                    </div>
                                </div>
                            </logic:equal>

                            <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                                <div id="tab-mailingContentViewHtml${index}" class="hidden">
                            </logic:equal>
                                    <div class="row">
                                        <div class="col-sm-12">
                                            <textarea id="${fn:replace(fn:replace(fn:replace(editorId, '[', '_'), ']', '_'), '.', '_')}" name="${editorId}"
                                                      class="form-control js-editor js-wysiwyg" rows="20" cols="85"
                                                      data-browse-mailing-id="${mailing.id}">${fn:escapeXml(tag.dynContent)}</textarea>
                                        </div>
                                    </div>
                            <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                                </div>
                            </logic:equal>
                        </div>

                        <div class="inline-tile-footer">
                            <div class="btn-group">
                                <a class="btn btn-regular btn-primary" href="#" data-form-action="${ACTION_TEXTMODULE_SAVE}">
                                    <span class="text"><bean:message key="button.Save"/></span>
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="tile-separator"></div>
                </c:forEach>

                <div class="inline-tile">
                    <div class="inline-tile-header">
                        <h2 class="headline-static"><bean:message key="target.Target"/> <button class="icon icon-help" data-help="help_${helplanguage}/mailingwizard/step_08/Target.xml" tabindex="-1" type="button"></button></h2>

                        <html:select property="targetID" size="1" styleId="targetID" styleClass="form-control js-select">
                            <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                            <logic:notEmpty name="targets" scope="request">
                                <c:forEach var="dbTarget" items="${targets}">
                                    <html:option value="${dbTarget.id}">${dbTarget.targetName}</html:option>
                                </c:forEach>
                            </logic:notEmpty>
                        </html:select>

                        <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                            <ul class="inline-tile-header-nav">
                                <li class="active">
                                    <a href="#" data-toggle-tab="#tab-mailingNewContentViewCode">
                                        <bean:message key="HTML"/>
                                    </a>
                                </li>
                                <li>
                                    <a href="#" data-toggle-tab="#tab-mailingNewContentViewHtml">
                                        <bean:message key="mailingContentHTMLEditor"/>
                                    </a>
                                </li>
                            </ul>
                        </logic:equal>

                        <ul class="inline-tile-header-actions">
                        </ul>
                    </div>

                    <div class="inline-tile-content">
                        <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                            <div id="tab-mailingNewContentViewCode">
                                <div class="row">
                                    <div class="col-sm-12">
                                        <div id="newContentEditor" class="form-control"></div>
                                    </div>
                                </div>
                            </div>
                        </logic:equal>

                        <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                        <div id="tab-mailingNewContentViewHtml" class="hidden">
                            </logic:equal>
                            <div class="row">
                                <div class="col-sm-12">
                                    <textarea id="newContent" name="newContent" class="form-control js-editor js-wysiwyg" rows="20" cols="85"
                                              data-browse-mailing-id="${mailing.id}">${fn:escapeXml(mailingWizardForm.newContent)}</textarea>
                                </div>
                            </div>
                            <logic:equal name="mailingWizardForm" property="showHTMLEditorForDynTag" value="true">
                        </div>
                        </logic:equal>
                    </div>

                    <div class="inline-tile-footer">
                        <a href="#" class="btn btn-regular btn-primary" data-form-action="${ACTION_TEXTMODULE_ADD}">
                            <i class="icon icon-plus-circle"></i>
                            <span class="text"><bean:message key="button.Add"/></span>
                        </a>
                    </div>
                </div>
            </div>
            <div class="tile-footer">
                <a href="#" class="btn btn-large pull-left" data-form-action="previous">
                    <i class="icon icon-angle-left"></i>
                    <span class="text"><bean:message key="button.Back"/></span>
                </a>
                <div class="btn-group pull-right">
                    <a href="#" class="btn btn-large btn-primary" data-form-action="${ACTION_TEXTMODULE}">
                        <span class="text"><bean:message key="button.Proceed"/></span>
                        <i class="icon icon-angle-right"></i>
                    </a>
                    <a href="#" class="btn btn-large btn-primary" data-form-action="skip">
                        <span class="text"><bean:message key="button.Skip"/></span>
                    </a>
                    <a href="#" class="btn btn-large btn-primary" data-form-action="${ACTION_FINISH}">
                        <span class="text"><bean:message key="button.Finish"/></span>
                    </a>
                </div>
                <span class="clearfix"></span>
            </div>
        </div>
    </div>
</agn:agnForm>
