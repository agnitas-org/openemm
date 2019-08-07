 /**
 * common functions to make a displaytag-table have 
 */

 //min width for header cell
 var minWidth = 26;
 var minWidthLast = 40;
 var overview = true;

 function resize( index, delta ) {
     table = document.getElementById(tableID);
     firstrow = table.rows[0];
     lastindex = firstrow.cells.length - 1;
     //header cell width
     var cellWidth;
     //nested span width
     var spanWidth;
     //header text holder index
     var i = firstrow.cells[index].getElementsByTagName('SPAN')[0].className == 'list_table_columnchange_left' ? 1 : 0;

     cellWidth = getWidthAsNumber(getStyleProperty(firstrow.cells[index], 'width')) + delta >= minWidth ? getWidthAsNumber(getStyleProperty(firstrow.cells[index], 'width')) + delta : minWidth;
     firstrow.cells[index].style.width = cellWidth + 'px';
     if(firstrow.cells[lastindex].offsetWidth < minWidthLast) firstrow.cells[lastindex].style.width = minWidthLast + 'px';
     spanWidth = cellWidth - minWidth;
     firstrow.cells[index].getElementsByTagName('SPAN')[i].style.width = index == lastindex ? '0px' : spanWidth + 'px';
 }

 function resizeAllCells(){
     var table = document.getElementById(tableID);
     var firstrow = table.rows[0];
     var lastindex = firstrow.cells.length - 1;

     for(var i=0; i<=lastindex; i++){
         if(parseInt(firstrow.cells[i].offsetWidth) != getWidthAsNumber(getStyleProperty(firstrow.cells[i], 'width'))){
             if(i==lastindex){
                 firstrow.cells[i].style.width = minWidthLast + 'px';
             } else {
                 resize(i, parseInt(getWidthAsNumber(getStyleProperty(firstrow.cells[i], 'width')) - firstrow.cells[i].offsetWidth));
             }
         }
     }

     var sum = 0;
     for(var i=0; i<lastindex; i++){
         sum = sum + firstrow.cells[i].offsetWidth;
         var leftGrab = document.getElementById('left_grab_' + i);
         var rightGrab = document.getElementById('right_grab_' + i);
         var posX = sum - 11;
         var posY = leftGrab.offsetTop + 1;
         rightGrab.style.position = 'absolute';
         rightGrab.style.left = posX;
         rightGrab.style.top = posY;
     }
  }

     function getStyleProperty(element,styleProp)
     {
		if (element.currentStyle) {
			 var property = element.currentStyle[styleProp];
			}
		else if (window.getComputedStyle) { 
			 var property = document.defaultView.getComputedStyle(element,'').getPropertyValue(styleProp);					
			}	
		return property;
		
     }
    
     function getWidthAsNumber( width ) {
		return parseInt(width.substring(0,width.indexOf('px')));   
     }  
	
     function getColumnIndex ( columns, className ) {
		for( i=0; i< columns.length; i++ ) {
			 if ( className == columns[i].className ){
				return i;
			 }
		}
		return -1;
     }

    function dragstart(e) {
    	target = getTarget(e);
		columnindex = target.id.substring( target.id.lastIndexOf('_') + 1 ,target.id.length);
	  	dragging = true;
	   	prevX= getEvent(e).clientX;	   
	   	if (getEvent(e).stopPropagation){
	   		getEvent(e).stopPropagation();
	   	}
         else getEvent(e).cancelBubble = true;
	   	if (getEvent(e).preventDefault){
		   	getEvent(e).preventDefault();
	   	}
         else getEvent(e).returnValue = false;
    }
        
    function drag( event ) {
        if(dragging){
            browserevent = getEvent(event);
            aktX = browserevent.clientX;
            delta = aktX - prevX;
        }
    }
	
    function dragstop(event) {
        browserevent = getEvent(event);
        target = getTarget(browserevent);
        if (dragging) {
            dragging = false;
            resize(columnindex, delta);
            writeHiddenColumnWidths();
            rewriteLinks();
            if(detectIE() == 7.0) {
                resizeAllCells();
            }
            stripWhitespaces();
            delta = 0;
            prevX = -1;
        }
        if (getEvent(event).stopPropagation) {
            getEvent(event).stopPropagation();
        }
           else getEvent(event).cancelBubble = true;
        if (getEvent(event).preventDefault) {
            getEvent(event).preventDefault();
        }
           else getEvent(event).returnValue = false;
    }	

  
	// get the event in dependency of the browser type
   function getEvent( event ) {
    if( !event ) {
    		return window.event;
    }
    return event;   
   }
    
   function rewriteLinks()  {    
	var availableLinks = document.links;
	var hiddenColumnWidthParameters = getHiddenColumnWidthParameters();	
	for (var i = 0; i < availableLinks.length; i++) {
		if( availableLinks[i].href.indexOf('__fromdisplaytag=true' ) > -1 ) {
			for(var j=0; j< hiddenColumnWidthParameters.length; j++) {
				if( availableLinks[i].href.indexOf('columnwidthsList['+j+']' ) == -1 ) { // parameter doesn't exist in URL
					availableLinks[i].href += '&';
					availableLinks[i].href += hiddenColumnWidthParameters[j];
				} 
				else { // replace parameter value
					availableLinks[i].href.replace(/columnwidthsList\['+ i +'\]=[0-9]+/gi,  hiddenColumnWidthParameters[j]);
				}
			}						 
		  }
		}
	 	
	}

  function getHiddenColumnWidthParameters() {
	hiddenElements = document.getElementsByTagName('input');
	hiddenColumnWidthParameters = new Array();	
		for( i = 0; i < hiddenElements.length; i++) {
			if( hiddenElements[i].type == null  || hiddenElements[i].type != 'hidden') {
				continue;
			}
			if( hiddenElements[i].name.indexOf('columnwidthsList[') > -1 ) {
				index = hiddenElements[i].name.substring(hiddenElements[i].name.indexOf('[') +1 ,hiddenElements[i].name.indexOf(']'));
				hiddenColumnWidthParameters[index] = 'columnwidthsList['+index+']=' + hiddenElements[i].value;
			}
		}
    	return hiddenColumnWidthParameters;
	} 
	
	function getHiddenColumnWidths() {
		hiddenElements = document.getElementsByTagName('input');
		hiddenColumnWidths = new Array();
		for( i = 0; i < hiddenElements.length; i++) {
			if( hiddenElements[i].type == null  || hiddenElements[i].type != 'hidden') {
				continue;
			}
			if( hiddenElements[i].name.indexOf('columnwidthsList[') > -1 ) {
				index = hiddenElements[i].name.substring(hiddenElements[i].name.indexOf('[') +1 ,hiddenElements[i].name.indexOf(']'));
                hiddenColumnWidths[index] = hiddenElements[i].value;
			}
		}
		return hiddenColumnWidths
	}
		
	function writeHiddenColumnWidths() {
				hiddenElements = document.getElementsByTagName('input');
				currentColumnWidths = getCurrentColumnWidths();
				for( i = 0; i < hiddenElements.length; i++) {
					if( hiddenElements[i].type == null  || hiddenElements[i].type != 'hidden') {
						continue;
					}
					if( hiddenElements[i].name.indexOf('columnwidthsList[') > -1 ) {
						index = hiddenElements[i].name.substring(hiddenElements[i].name.indexOf('[') +1 ,hiddenElements[i].name.indexOf(']'));
						hiddenElements[i].value = currentColumnWidths[index];
					}
				}
			}

	function getCurrentColumnWidths() {
			currentColumnWidths = new Array();
			firstrow = table.rows[0]		
			for( i=0 ; i < firstrow.cells.length ; i++ ) {  	
			    currentColumnWidths[i] = ( getWidthAsNumber(getStyleProperty( firstrow.cells[i], 'width'))) ; 
			}			
			return currentColumnWidths;
    }
    
    function getTarget(e) {
      var target;
	  if (!e)
  	  {
  		e=window.event;
 	  }
	  if (e.target)
  	  {
  		target=e.target;
  	  }
	  else if (e.srcElement)
  	  {
  		target=e.srcElement;
  	  }
	  if (target.nodeType==3) // Safari bug
  	  {
  		target = target.parentNode;
  	  }	
  	  return target;
    }	
    
    	// add an <div> with the onmouse-stuff to each header cell			
			function rewriteTableHeader( table ) {
                if(table){
                    firstrow = table.rows[0];
                    cells = firstrow.cells;
                    for( i=0 ; i< cells.length; i++ ) {
                        currentcell = cells[i];
                        innerHTML = currentcell.innerHTML ;
                        cleanup( currentcell );
                        // use i - 1, because the left grabber should handle the column on the right hand side
                        leftGrabber = '<span id="left_grab_' + (i-1) + '" style="cursor: move;" class="list_table_columnchange_left" onmousedown="dragstart(event)"></span>';
                        rightGrabber = '<span id="right_grab_' + i + '" style="cursor: move;" class="list_table_columnchange_right" onmousedown="dragstart(event)"></span>';
                        if( i == 0) {
                            leftGrabber = '';
                        }
                        if (i == cells.length-1) {
                            rightGrabber = '';
                        }
                        currentcell.innerHTML = leftGrabber + '<span style="float:left">' +innerHTML +'</span>' + rightGrabber;
                    }
                }
			}
			// set the width of the column header to the value of the corresponding hidden field
			// will be called after the table has been loaded
			function writeWidthFromHiddenFields(table) {
                if(table){
                    firstrow = table.rows[0];
                    widths = getHiddenColumnWidths();
                    for( i = 0; i < widths.length; i++) {
                        if( widths[i] && widths[i] != '-1') {
                             firstrow.cells[i].style.width = widths[i] + 'px';
                        }
                    }
                }
			}
			
			// cleanup a element
			function cleanup(element) {
				if ( element.hasChildNodes() ) {
    				while ( element.childNodes.length >= 1 ){
        				element.removeChild( element.firstChild );       
    				} 
				}			
			}

            // prevent displaying header cells content in two rows & avoid selection text during dragging in IE
            function onPageLoad() {
                onPageLoadEx(false);
            }

            function onPageLoadEx(lockLastColumn) {
                var table = document.getElementById(tableID);
                if(table){
                    var parent = table.parentNode;
                    var firstrow = table.rows[0];
                    if(!(detectIE() == 7.0)){
                        var i = 0;
                        if(lockLastColumn){
                            i = 1;
                        }
                        for (;i<firstrow.cells.length-1; i++)
                            resize(i,0);
                        if(lockLastColumn){
                            firstrow.cells[firstrow.cells.length-1].style.width = minWidthLast;
                        }
                    } else {
                        resizeAllCells();
                    }
                    var myElement = document.createElement('DIV');
                    myElement.className = table.className;
                    myElement.id = "table-holder";
                    myElement.style.overflowX = 'auto';
                    table.style.marginLeft = "0px";
                    if(detectIE() == 7.0 && tableID != 'mailing'){
                        myElement.style.overflowY = 'hidden';
                    }
                    if(detectIE() == 7.0){
                        myElement.style.height = table.offsetHeight + 20;
                    }
                    if(tableID == 'mailing' && !overview){
                        myElement.style.height = '200px';
                        myElement.style.width = '921px';
                        myElement.style.overflowY = 'auto';
                        myElement.style.marginLeft = '28px';
                    }
                    myElement.style.display= 'block';
                    parent.replaceChild(myElement,table);
                    myElement.appendChild(table);
                    stripWhitespaces();
                    firstrow.onselectstart = function(){ return false; }
                }
            }
 // fix for IE9 (the content after table moves down while moving mouse over the table)
function stripWhitespaces(){
    if(checkIE9()){
        var myElement = document.getElementById('table-holder');
        if(myElement){
            var tableHtml = myElement.innerHTML;
            var expr = new RegExp('>[ \t\r\n\v\f]*<', 'g');
            tableHtml = tableHtml.replace(expr, '><');
            myElement.appendHTML(tableHtml);
        }
    }
}

function checkIE9(){
   var result = false;
   var rv = detectIE();
   if(rv == 9.0) {
       result = true;
        HTMLElement.prototype.appendHTML = function(s) {
        var div = document.createElement('div');
        div.innerHTML = s;
        this.childNodes[0].remove();
        while (div.firstChild)
        this.appendChild(div.firstChild);
        }
   }
   return result;
}

function detectIE(){
   var rv = -1;
   if (navigator.appName == 'Microsoft Internet Explorer')
   {
      var ua = navigator.userAgent;
      var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
      if (re.exec(ua) != null)
         rv = parseFloat( RegExp.$1 );
   }
   return rv;
}


