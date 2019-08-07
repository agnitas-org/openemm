// Very Bad Code!
hasChanged=false;

function	enableButton(name, msg, how)    {
	hasChanged=!how;
	buttonmsg =  document.getElementById(name).src;
	jsessionid = buttonmsg.match(";jsessionid=[0-9a-zA-Z]*");
	
	if(how) {
		if( !(  buttonmsg.indexOf("button") != -1 && buttonmsg.indexOf("msg=") != -1 &&  buttonmsg.indexOf("t=3") != -1 ) ) {
			if ( jsessionid != null && jsessionid != ''  && jsessionid != 'null' ) {
				document.getElementById(name).src="button" +jsessionid + "?msg="+msg+"&t=3";
			}
			else {
				document.getElementById(name).src="button?msg="+msg+"&t=3";
			}
			
		}
	} else {
		
		if( (buttonmsg.indexOf("button") != -1 && buttonmsg.indexOf("msg=") != -1)) {
			if ( jsessionid != null && jsessionid != ''  && jsessionid != 'null' ) {
				document.getElementById(name).src="button" +jsessionid + "?msg="+msg;
			}
			else {
				document.getElementById(name).src="button?msg="+msg;
			}
		}
	}
}

function	changed()	{
	enableButton("save", "Save", false);
}

function	doSave()	{
	if(!hasChanged) {
		return false;
	}
	return true;
}

function	enableChanged() {	
	fields=document.getElementsByTagName("input");
	for (i=0; i < fields.length; i++) {
		//fields[i].attachEvent("onchange", changed);
		fields[i].attachEvent("onmouseout", changed);
	}
}



document.onchange=changed;
