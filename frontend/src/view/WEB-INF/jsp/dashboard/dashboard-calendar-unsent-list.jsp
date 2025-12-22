<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="unsentMailings" type="java.util.List<com.agnitas.emm.core.mailing.bean.MailingDto>"--%>

<div id="dashboard-calendar-unsent-wrapper"><%--in tile scroll mode the calendar grid has no bottom gap but unsent tile should always have it this wrapper helps to add it--%>
    <c:if test="${unsentMailings ne null}">
        <div id="dashboard-calendar-unsent-tile" class="tile">
            <script data-initializer="unsent-list" type="application/json">
                {
                    "mailings": ${emm:toJson(unsentMailings)},
                    "planned": "${dashboardCalendarForm.showUnsentPlanned}"
                }
            </script>

            <nav class="tile-header navbar border-bottom">
                <a class="btn btn-header-tab active border-top-left-radius-0 overflow-hidden" href="#">
                    <span class="text text-truncate"><mvc:message code="${dashboardCalendarForm.showUnsentPlanned ? 'calendar.planned.mailings' : 'calendar.unscheduledMailings'}"/></span>
                </a>
                <button class="navbar-toggler btn-icon border-0" type="button" data-bs-toggle="offcanvas" data-bs-target="#unsent-mailings-nav" aria-controls="unsent-mailings-nav" aria-expanded="false">
                    <i class="icon icon-bars"></i>
                </button>
                <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="unsent-mailings-nav">
                    <ul class="navbar-nav offcanvas-body">
                        <li class="nav-item">
                            <a href="#" class="btn btn-outline-primary ${dashboardCalendarForm.showUnsentPlanned ? '' : 'active'}" data-bs-dismiss="offcanvas" data-form-target='#unsent-mailings-form' data-form-submit="">
                                <span class="text-truncate"><mvc:message code="calendar.unscheduledMailings"/></span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="#" class="btn btn-outline-primary ${dashboardCalendarForm.showUnsentPlanned ? 'active' : ''}" data-bs-dismiss="offcanvas" data-form-target='#unsent-mailings-form'  data-form-set="showUnsentPlanned:true" data-form-submit="">
                                <span class="text-truncate"><mvc:message code="calendar.planned.mailings"/></span>
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>
            <div class="tile-body vstack gap-2 p-2 js-scrollable">
                <c:if test="${empty unsentMailings}">
                    <div class="notification-simple">
                        <i class="icon icon-info-circle"></i>
                        <mvc:message code="noResultsFound"/>
                    </div>
                </c:if>
                <%-- mailing labels populated with js--%>
            </div>
        </div>
    </c:if>
</div>
