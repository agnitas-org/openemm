<%@ page language="java" import="com.agnitas.util.*, java.util.*, org.agnitas.web.EmmActionAction, com.agnitas.emm.core.mediatypes.common.MediaTypes" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:ShowByPermission token="actions.change">
    <div class="inline-tile-content">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="action.op.ActivateDOI.mailinglists"/></label>
            </div>
            <div class="col-sm-8">
                <label class="toggle">
                    <html:hidden property="__STRUTS_CHECKBOX_actions[${opIndex}].forAllLists" value="off" />
                    <html:checkbox styleId="useTrack_id_${opIndex}" property="actions[${opIndex}].forAllLists" />
                    <div class="toggle-control"></div>
                </label>
            </div>
        </div>
    </div>
    <div class="inline-tile-content">
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><bean:message key="action.op.ActivateDOI.mediatype"/></label>
            </div>
            <div class="col-sm-8">
                <html:select property="actions[${opIndex}].mediaTypeCode" size="1" styleClass="form-control js-select">
                    <c:forEach var="mediaType" items="<%= MediaTypes.values() %>">
                        <emm:ShowByPermission token="${mediaType.requiredPermission}">
                  			<html:option value="${mediaType.mediaCode}"><bean:message key="mailing.MediaType.${mediaType.mediaCode}" /></html:option>
                  		</emm:ShowByPermission>
					</c:forEach>                      
            	</html:select>
            </div>
        </div>
    </div>
    <div class="inline-tile-footer">
        <a class="btn btn-regular" href="#" data-form-set="action: <%= EmmActionAction.ACTION_REMOVE_MODULE %>, deleteModule: ${opIndex}" data-form-submit>
            <i class="icon icon-trash-o"></i>
            <span class="text"><bean:message key="button.Delete"/></span>
        </a>
    </div>
</emm:ShowByPermission>
