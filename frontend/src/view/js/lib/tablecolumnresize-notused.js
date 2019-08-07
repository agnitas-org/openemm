 /**
 * common functions to make a displaytag-table have 
 */
 function resize( index, delta ){
    	table = document.getElementById(tableID);
	    firstrow = table.rows[0];
	    firstrow.cells[index].style.width = ( getWidthAsNumber(getStyleProperty( firstrow.cells[index], 'width')) + delta ) + 'px';	
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
	   	if (getEvent(e).stopPropagation) getEvent(e).stopPropagation();
		getEvent(e).preventDefault();
    }
        
    function drag( event ) {
		browserevent = getEvent(event);
		aktX = browserevent.clientX;
       	delta = aktX - prevX;
       	if (getEvent(event).stopPropagation) getEvent(event).stopPropagation();
		getEvent(event).preventDefault();
    }
	
    function dragstop(event) {
    	browserevent = getEvent(event);
    	target = getTarget( browserevent );
    	if( dragging ) {
       		dragging = false;
       		resize( columnindex ,delta );
			writeHiddenColumnWidths();
			rewriteLinks();	
			prevX= -1;			
		}
    	if (getEvent(event).stopPropagation) getEvent(event).stopPropagation();
    	getEvent(event).preventDefault();
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
				firstrow = table.rows[0];
				cells = firstrow.cells;
				for( i=0 ; i< cells.length; i++ ) {
					currentcell = cells[i];
					innerHTML = currentcell.innerHTML ;
					cleanup( currentcell );		
					currentcell.innerHTML ='<div id="head_div_'+i+'" style="background-color: #D2D7D2; cursor: move; font-weight: bold; color: #FFFFFF; display:inline;float: right; " onmousedown="dragstart(event)" >::</div>' + '<div style="display:inline; float:left">' +innerHTML +'</div>'; 
				}				
			}
			// set the width of the column header to the value of the corresponding hidden field
			// will be called after the table has been loaded
			function writeWidthFromHiddenFields(table) {				
				firstrow = table.rows[0];				
				widths = getHiddenColumnWidths();
				for( i = 0; i < widths.length; i++) {
					if( widths[i] != '-1') {
  						 firstrow.cells[i].style.width = widths[i] + 'px';
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