$(document).ready(function() 
    { 
		$('#helpbox_container').hide();
        $("#mailuebersicht").tablesorter({
			cancelSelection:true,
			widgets: ['zebra'],
			headers: { 
							6: { 
								sorter:false
							} 
						}							 
		}); 
		$('#mailuebersicht tbody tr').hover(
   			function()
   				{
   				 $(this).addClass("list_highlight");
  				 },
  			function()
   				{
    			$(this).removeClass("list_highlight");
  				 }
  			)
		/*
		$(".help_link").click(function(event){
					$('#helpbox_container').toggle();
					event.preventDefault();	 
									   });
					
		*/
		$(".help_close A").click(
				function(event2){				 
						$('#helpbox_container').hide();
								 });

}); 