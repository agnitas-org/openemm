<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.send.show"/>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<div data-initializer="transmission-status">
    <div class="well block">
        <b><bean:message key="mailing.DistribStatus"/>:</b> <bean:message key="statistic.DeliveryStatus.${mailingSendForm.deliveryStat.deliveryStatus}"/>
    </div>
    <c:if test="${mailingSendForm.deliveryStat.lastType ne 'NO'}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped">
                <thead>
                <tr>
                    <th><bean:message key="mailing.LastDelivery"/></th>
                    <th><bean:message key="mailing.send.delivery.status.${mailingSendForm.deliveryStat.lastType}"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><bean:message key="Date"/></td>
                    <td><fmt:formatDate value="${mailingSendForm.deliveryStat.lastDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" /></td>
                </tr>
                <tr>
                    <td><bean:message key="default.Time"/></td>
                    <td><fmt:formatDate value="${mailingSendForm.deliveryStat.lastDate}" pattern="${adminTimeFormat}" timeZone="${adminTimeZone}" /></td>
                </tr>
                <tr>
                    <td><bean:message key="Targets"/></td>
                    <td>
                        <c:if test="${not empty mailingSendForm.targetGroupsNames}">
                            <c:forEach var="targetName" items="${mailingSendForm.targetGroupsNames}" varStatus="vs">
                                <p>${targetName}</p>
                            </c:forEach>
                        </c:if>
                    </td>
                </tr>
                <tr>
                    <td><bean:message key="statistic.TotalMails"/></td>
                    <td><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${mailingSendForm.deliveryStat.lastTotal}"/></td>
                </tr>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 0}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped">
                <thead>
                <tr>
                    <th colspan="2"><bean:message key="mailing.Generation"/></th>
                </tr>
                </thead>
                <tbody>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 1 && mailingSendForm.deliveryStat.generateStartTime ne null}">
                    <tr>
                        <td><bean:message key="mailing.sendStatus.started"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.generateStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 2 && mailingSendForm.deliveryStat.generateEndTime ne null}">
                    <tr>
                        <td><bean:message key="mailing.sendStatus.ended"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.generateEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus le 2}">
                    <tr>
                        <td><bean:message key="statistic.ScheduledGenerateTime"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.scheduledGenerateTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 1}">
                    <tr>
                        <td><bean:message key="mailing.GeneratedMails"/></td>
                        <td><fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${mailingSendForm.deliveryStat.generatedMails}"/></td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 0}">
        <div class="table-responsive vspace-top-10">
            <table class="table table-bordered table-striped">
                <thead>
                <th colspan="2"><bean:message key="mailing.Delivery"/></th>
                </thead>
                <tbody>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 3 && mailingSendForm.deliveryStat.sendStartTime ne null}">
                    <tr>
                        <td><bean:message key="mailing.sendStatus.started"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.sendStartTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 4 && mailingSendForm.deliveryStat.sendEndTime ne null}">
                    <tr>
                        <td><bean:message key="mailing.sendStatus.ended"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.sendEndTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus le 4}">
                    <tr>
                        <td><bean:message key="statistic.ScheduledSendTime"/></td>
                        <td><fmt:formatDate value="${mailingSendForm.deliveryStat.scheduledSendTime}" pattern="${adminDateTimeFormat}" timeZone="${adminTimeZone}" /></td>
                    </tr>
                </c:if>
                <c:if test="${mailingSendForm.deliveryStat.deliveryStatus gt 3}">
                    <tr>
                        <td><bean:message key="mailing.SentMails"/></td>
                        <td class="send_status_second_column">
                            <fmt:formatNumber type="number" pattern="###,###,###,###,##0" value="${mailingSendForm.deliveryStat.sentMails}"/>
                        </td>
                    </tr>
                </c:if>

                <c:if test="${not empty mailingSendForm.deliveryStat.optimizeMailGeneration }">
                    <tr>
                        <td><bean:message key="mailing.optimizeMailGeneration"/></td>
                        <c:choose>
                            <c:when test="${mailingSendForm.deliveryStat.optimizeMailGeneration eq '24h'}">
                                <td><bean:message key="mailing.optimizeMailGeneration.next24h"/></td>
                            </c:when>
                            <c:otherwise>
                                <td><bean:message key="mailing.optimizeMailGeneration.${mailingSendForm.deliveryStat.optimizeMailGeneration}"/></td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </c:if>

    <c:if test="${mailingSendForm.deliveryStat ne null && mailingSendForm.deliveryStat.cancelable}">
        <c:set var="ACTION_CANCEL_MAILING_REQUEST" value="<%= MailingSendAction.ACTION_CANCEL_MAILING_REQUEST %>"/>
        <agn:agnLink styleClass="btn btn-regular btn-warning vspace-top-10" data-confirm="" page="/mailingsend.do?action=${ACTION_CANCEL_MAILING_REQUEST}&mailingID=${mailingSendForm.mailingID}" target="_parent">
            <i class="icon icon-ban"></i>
            <c:choose>
            	<c:when test="${mailingSendForm.deliveryStat.deliveryStatus == 3 or mailingSendForm.deliveryStat.deliveryStatus == 4}">
            		<span class="text"><bean:message key="mailing.PauseDelivery"/></span>
                </c:when>
                <c:otherwise>
            		<span class="text"><bean:message key="mailing.CancelGeneration"/></span>
            	</c:otherwise>
            </c:choose>
        </agn:agnLink>
    </c:if>
    
    <emm:ShowWhenFeatureEnabled feature="AUTOMATION">
	    <emm:ShowByPermission token="mailing.resume.world">
		    <c:if test="${not empty mailingSendForm.deliveryStat and mailingSendForm.deliveryStat.stopped}">
	            <c:choose>
	                <c:when test="${mailingSendForm.deliveryStat.resumable}">
	                    <c:set var="ACTION_RESUME_MAILING_REQUEST" value="<%= MailingSendAction.ACTION_RESUME_MAILING_REQUEST %>"/>
	                    <agn:agnLink styleClass="btn btn-regular btn-primary vspace-top-10" data-confirm="" page="/mailingsend.do?action=${ACTION_RESUME_MAILING_REQUEST}&mailingID=${mailingSendForm.mailingID}" target="_parent">
	                        <i class="icon icon-paper-plane"></i>
	                        <c:choose>
	                            <c:when test="${mailingSendForm.deliveryStat.deliveryStatus == 3
	                            or mailingSendForm.deliveryStat.deliveryStatus == 4
	                            or mailingSendForm.deliveryStat.deliveryStatus == 8}">
	                                <span class="text"><bean:message key="mailing.ResumeDelivery"/></span>
	                                <c:set var="SHOW_DELIVERY_INFO" value="true" />
	                            </c:when>
	                            <c:otherwise>
	                                <span class="text"><bean:message key="mailing.ResumeGeneration"/></span>
	                            </c:otherwise>
	                        </c:choose>
	                    </agn:agnLink>
	                </c:when>
	                <c:otherwise>
	                    <span data-tooltip="<bean:message key="error.mailing.delivery.resuming.impossible" arg0='48'/>">
	                        <a href="#" class="btn btn-regular btn-primary vspace-top-10 disabled">
	                        <i class="icon icon-paper-plane"></i>
	                        <span class="text"><bean:message key="mailing.ResumeDelivery"/></span>
	                        </a>
	                    </span>
	                </c:otherwise>
	            </c:choose>
	            
	            <c:if test="${MAILTRACKING_ENABLED}">
			        <c:set var="ACTION_RESUME_MAILING_BY_COPY_REQUEST" value="<%= MailingSendAction.ACTION_RESUME_MAILING_BY_COPY_REQUEST %>"/>
			        <agn:agnLink styleClass="btn btn-regular btn-warning vspace-top-10" data-confirm="" page="/mailingsend.do?action=${ACTION_RESUME_MAILING_BY_COPY_REQUEST}&mailingID=${mailingSendForm.mailingID}" target="_parent">
			            <i class="icon icon-copy"></i>
			            <c:choose>
		            		<c:when test="${mailingSendForm.deliveryStat.deliveryStatus == 3 or mailingSendForm.deliveryStat.deliveryStatus == 4}">
			            		<span class="text"><bean:message key="mailing.ResumeDeliveryByCopy"/></span>
			            		<c:set var="SHOW_DELIVERY_INFO" value="true" /> 
		    	            </c:when>
		        	        <c:otherwise>
				    	        <span class="text"><bean:message key="mailing.ResumeGenerationByCopy"/></span>
		            		</c:otherwise>
		            	</c:choose>
			        </agn:agnLink>
			   </c:if>
		        
				<c:if test="${SHOW_DELIVERY_INFO and MAILTRACKING_ENABLED}">
		            <div class="tile-content-forms form-vertical">
		                 <div class="form-group">
							<div class="notification notification-info">
								<div class="notification-header">
									<p class="headline">
										<i class="icon icon-state-info"></i> 
										<span class="text"><bean:message key="Info" /></span>
									</p>
								</div>
								<div class="notification-content">
									<p>
										<bean:message key="mailing.ResumeDelivery.info" />
									</p>
								</div>
							</div>
						</div>
					</div>
				</c:if>
			</c:if>
	   </emm:ShowByPermission>
	</emm:ShowWhenFeatureEnabled>
	
    <c:if test="${not empty isPostMailing and isPostMailing eq 'true'}">
        <div class="tile-content-forms form-vertical">
            <div class="form-group">
                <div class="notification notification-info">
                    <div class="notification-header">
                        <p class="headline">
                            <i class="icon icon-state-info"></i>
                            <span class="text"><bean:message key="Info" /></span>
                        </p>
                    </div>
                    <div class="notification-content">
                        <p>
                            <bean:message key="mailing.send.post.hint" />
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <script id="config:transmission-status" type="application/json">
        {
          "isRunning":${mailingSendForm.transmissionRunning}
        }
    </script>
</div>
