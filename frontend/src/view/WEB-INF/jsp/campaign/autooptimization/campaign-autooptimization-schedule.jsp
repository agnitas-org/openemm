
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>


<script type="text/javascript">

	function createPickers() { 
		$(document.body).select('input.datepicker').each( 
				function(e) {
					 new Control.DatePicker(e, { 'icon': 'images/datepicker/calendar.png' ,
					  timePicker: true,
					  timePickerAdjacent: true ,
					  dateTimeFormat: '${DATE_PATTERN_FULL}',
					  dateFormat: '${DATE_PATTERN_FULL}'  
				    }); } 
		);
	  }
	
	 Event.observe(window, 'load', createPickers);
				

	// send the form with JS to avoid problems with IE image button stuff...
	function sendForm( method) {
		document.optimizationScheduleForm.method.value = method;
		document.optimizationScheduleForm.submit();
	} 
	
</script>

<html:form action="/optimize_schedule">

	<html:hidden property="optimizationID" />
	<input type="hidden" name="method"  />

    <div class="grey_box_container">
      <div class="grey_box_top"></div>
      <div class="grey_box_content">
          <div class="blue_box_form_item autoopt_property">
          	<label><bean:message key="mailing.autooptimization.testmailingssenddate" /></label>
          	<html:text property="testMailingsSendDateAsString" styleClass="datepicker" />
          </div>
            <div class="blue_box_form_item autoopt_property">
          	<label><bean:message key="mailing.autooptimization.resultsenddate" /></label>
          	<html:text property="resultSendDateAsString" styleClass="datepicker" />
          </div> 
      </div>
      <div class="grey_box_bottom"></div>
    </div>
    
    <div class="button_container">
    	<div class="action_button">
    		<a href="#" onclick="sendForm('schedule');"><span><bean:message key="mailing.autooptimization.schedule" /></span></a>
    	</div>
    	<div class="action_button">
			<a href="#" onclick="sendForm('unSchedule');"><span><bean:message key="button.Delete"/></span></a>
		</div>	
    </div>
</html:form>


