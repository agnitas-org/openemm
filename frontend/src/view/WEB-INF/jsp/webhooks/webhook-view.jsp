<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"			uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" 			uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="emm" 		uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="display" 	uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc" 		uri="https://emm.agnitas.de/jsp/jsp/spring" %>


<mvc:form servletRelativeAction="/webhooks/${WEBHOOK_EVENT_TYPE}/save.action" id="webhookConfigForm" modelAttribute="webhookConfigForm" method="POST">
    <div class="tile">
        <div class="tile-header">
        	<h2 class="headline">
	            <mvc:message code="webhooks.event.${WEBHOOK_EVENT_TYPE}.label" />
	        </h2>
        </div>
        <div class="tile-content tile-content-forms">
        	<div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="webhook_url"><mvc:message code="webhooks.url"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="url" maxlength="1000" cssClass="form-control" id="webhook_url" />
                </div>
            </div>
        </div>
    </div>
    
    <c:if test="${WEBHOOK_EVENT_TYPE.includesRecipientData}">
	   <div class="tile">
	       <div class="tile-header">
	       		<h2 class="headline">
	            	<mvc:message code="webhooks.profilefields" />
	        	</h2>
	       </div>
	       <div class="tile-content tile-content-forms">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="profileFields"><mvc:message code="recipient.fields"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="includedProfileFields" multiple="true"
                                    id="profileFields" cssClass="form-control js-select">
                            <mvc:options items="${PROFILE_FIELDS}" itemValue="internalName" itemLabel="shortname"/>
                        </mvc:select>
                    </div>
                </div>
           </div>
	   </div>
	</c:if>
	
</mvc:form>
