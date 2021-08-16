<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<emm:instantiate type="java.util.LinkedHashMap" var="allowedIntervals">
    <c:set target="${allowedIntervals}" property="1" value="default.every.hour"/>
    <c:set target="${allowedIntervals}" property="2" value="default.every.hour.2"/>
    <c:set target="${allowedIntervals}" property="3" value="default.every.hour.3"/>
    <c:set target="${allowedIntervals}" property="4" value="default.every.hour.4"/>
    <c:set target="${allowedIntervals}" property="6" value="default.every.hour.6"/>
    <c:set target="${allowedIntervals}" property="12" value="default.every.hour.12"/>
</emm:instantiate>

<script id="schedule-day-row" type="text/x-mustache-template">
	<tr class="l-time-schedule-row">
        <td class="col-sm-3">
            <div class="day-wrapper">
                <div class="form-group">
                    <div class="input-group">
                        <div class="input-group-controls">
							<select name="dayOfTheWeek" class="form-control js-select" size="1">
                                <c:forEach var="weekDay" begin="0" end="7" step="1">

                                    {{ var _weekDay = ${weekDay};}}
                                    {{ var selected = entry.weekDay == _weekDay ? 'selected="selected"' : '';}}

                                    <c:if test="${weekDay == 0}">
                                        <option value="${weekDay}" {{- selected}}><bean:message key="default.every.day"/></option>
                                    </c:if>
                                    <c:if test="${weekDay != 0 && weekDay != 1}">
                                        <option value="${weekDay}" {{- selected}}><bean:message key="calendar.dayOfWeek.${weekDay}"/></option>
                                    </c:if>
                                </c:forEach>
                                <option value="1" {{ entry.weekDay == 1 ? print('selected="selected"') : print('')}}><bean:message key="calendar.dayOfWeek.1"/></option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <a href="#" class="btn btn-regular" data-action="add-day">
                        <i class="icon icon-plus"></i>
                        <span class="text"><bean:message key="button.Add"/></span>
                    </a>
                    <a href="#" class="btn btn-regular hidden" data-action="remove-day">
                        <i class="icon icon-trash-o"></i>
                        <span class="text"><bean:message key="Delete"/></span>
                    </a>
                </div>
            </div>
        </td>
        <td class="col-sm-9" data-action="validate-changes">
			<div class="schedule-settings-wrapper">
                <div class="form-group">
                    <div class="input-group">
                        <div class="input-group-addon">
                            <div class="addon addon-undecorated" data-tooltip="${tooltipMessage}">
                                <input type="checkbox" class="hour-checkbox"
                                       data-action="hour-checkbox-toggle"
                                       disables=".time-checkbox,.time-input" ${not extended ? 'disabled' : ''}
                                        {{ (entry.scheduledInterval.active) ? print('checked="checked"') : print('') }}>
                            </div>
                        </div>
                        <div class="input-group-controls"  data-tooltip="${tooltipMessage}">
                            <select class="form-control intervals hour-select" ${not extended ? 'disabled' : ''}>
                                <c:forEach items="${allowedIntervals}" var="interval">
                                    {{ var value = ${interval.key}; }}
                                    {{ var selected = entry.scheduledInterval.interval == value ? 'selected="selected"' : ''; }}
                                    <option value="${interval.key}" {{- selected}}><bean:message key="${interval.value}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>

				{{ _.each(entry.scheduledTime, function(scheduleElement) { }}
				<div class="form-group schedule-time-wrapper prime">
                    <div class="input-group">
                        <div class="input-group-addon">
                            <div class="addon addon-undecorated">
                                <input type="checkbox" class="time-checkbox"
                                       {{ (scheduleElement.active) ? print('checked="checked"') : print(''); }}
                                       data-action="time-checkbox-toggle" disables=".hour-checkbox,.hour-select">
                            </div>
                        </div>
                        <div class="input-group-controls">
							<input class="form-control js-timepicker time-input" type="text" value="{{- scheduleElement.time}}" data-timepicker-options="mask: 'h:s'"
								   placeholder="_"/>
                            <span class="interval-reminder"><bean:message key="default.interval"/> <bean:message key="default.minutes.60"/></span>
						</div>
						<div class="input-group-addon">
							<span class="addon">
								<i class="icon icon-clock-o"></i>
								<span class="text">${adminTimezone}</span>
							</span>
						</div>
                        <c:if test="${extended}">
                            <div class="input-group-btn">
                                <a href="#" class="btn btn-regular btn-primary" data-action="add-schedule" data-tooltip="<bean:message key="button.Add"/>">
                                    <i class="icon icon-plus"></i>
                                </a>
                            </div>
                            <div class="input-group-addon hidden">
                                <a href="#" class="btn btn-regular btn-alert" data-action="remove-schedule">
                                    <i class="icon icon-trash-o"></i>
                                </a>
                            </div>
                        </c:if>
                    </div>
                </div>
                {{});}}
            </div>
        </td>
    </tr>
</script>

<script id="schedule-extended-time-wrapper" type="text/x-mustache-template">
	<div class="form-group schedule-time-wrapper prime">
		<div class="input-group">
			<div class="input-group-addon">
				<div class="addon addon-undecorated">
					<input type="checkbox" class="time-checkbox"
						   {{ (scheduleElement.active) ? print('checked="checked"') : print(''); }}
						   data-action="time-checkbox-toggle" disables=".hour-checkbox,.hour-select">
				</div>
			</div>
			<div class="input-group-controls">
				<input class="form-control js-timepicker time-input" type="text" value="{{- scheduleElement.time}}" data-timepicker-options="mask: 'h:s'"
					   placeholder="_"/>
                <span class="interval-reminder"><bean:message key="default.interval"/> <bean:message key="default.minutes.60"/></span>
            </div>
			<div class="input-group-addon">
				<span class="addon">
					<i class="icon icon-clock-o"></i>
					<span class="text">${adminTimezone}</span>
				</span>
			</div>
			<c:if test="${extended}">
				<div class="input-group-btn">
					<a href="#" class="btn btn-regular btn-primary" data-action="add-schedule" data-tooltip="<bean:message key="button.Add"/>">
						<i class="icon icon-plus"></i>
					</a>
				</div>
				<div class="input-group-addon hidden">
					<a href="#" class="btn btn-regular btn-alert" data-action="remove-schedule">
						<i class="icon icon-trash-o"></i>
					</a>
				</div>
			</c:if>
		</div>
	</div>
</script>
