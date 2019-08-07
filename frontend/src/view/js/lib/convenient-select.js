function compareOptionEntries( aOptions,bOptions) {

	a = aOptions.text;
	a = a.toLowerCase();
	a = a.replace(/ä/g,"a");
	a = a.replace(/ö/g,"o");
	a = a.replace(/ü/g,"u");
	a = a.replace(/ß/g,"s");

	b = bOptions.text;
	b = b.toLowerCase();
	b = b.replace(/ä/g,"a");
	b = b.replace(/ö/g,"o");
	b = b.replace(/ü/g,"u");
	b = b.replace(/ß/g,"s");

	return(a==b)?0:(a>b)?1:-1;

	
}

function toRight() {
	left = document.getElementsByName(leftName)[0];
	right = document.getElementsByName(rightName)[0];
	leftlength = left.options.length;
	
	for (i = 0; i < leftlength; i++) { // for each selected option on the left -> create a new one on the right
		if (left.options[i].selected == true) {
			rightcurrentlength = right.options.length;
			newOption = new Option(left.options[i].text);
			newOption.value = left.options[i].value;
			right.options[rightcurrentlength] = newOption;
		}		
	}
	
	for (i = (leftlength - 1); i >= 0; i--) { // delete the selected options on the left
		if (left.options[i].selected == true) {
			left.options[i] = null;
		}
	}
	
}

function toLeft() {
	left = document.getElementsByName(leftName)[0];
	right = document.getElementsByName(rightName)[0];
	rightlength = right.options.length;
	for (i = 0; i < rightlength; i++) { // for each selected option on the right -> create a new one on the left
		if (right.options[i].selected == true) {
			leftcurrentlength = left.options.length;
			newOption = new Option(right.options[i].text);
			newOption.value = right.options[i].value;
			left.options[leftcurrentlength] = newOption;
		}
	}
	
	for (i = (rightlength - 1); i >= 0; i--) { // delete the selected options on the right
		if (right.options[i].selected == true) {
			right.options[i] = null;
		}
	}

	 leftOptions = new Array();

	// options are not an array in the DOM ?!!  
	for ( i = 0; i < left.options.length ; i++ ) {
		leftOptions[i] = left.options[i];		
	}	
	
	leftOptions.sort(compareOptionEntries);
	
	for ( i = 0; i < leftOptions.length ; i++ ) {
		left.options[i] = leftOptions[i];
	}  
	
}

function selectAllBeforeSubmit() {
	right = document.getElementsByName(rightName)[0];
	rightlength = right.options.length;
	var i = 0;
	for (; i < rightlength; i++) { // for each selected option on the right -> create a new one on the left
		right.options[i].selected = true;
	}
	form = document.getElementsByName(formName)[0];
	if (i == 0) {
		form.targetSelectsReset.value = "true";
	}
	form.submit();
}
