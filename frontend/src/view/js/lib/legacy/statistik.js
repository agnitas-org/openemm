$(document).ready(function() 
    { 
		$('#helpbox_container').hide();
	
		/*
		$(".help_link").click(function(event){
					$('#helpbox_container').toggle();
					event.preventDefault();	 
									   });
		*/			
		$(".stats_targetgroups_add").click(function(event5){
								var targetgroup = '<div><a href="#" class="removeTargetgroup"><img src="../../../assets/core/images/removetargetgroup2.png" /></a>'+$('#stats_targetgroups_select :selected').text()+'</div>';
								$(targetgroup).appendTo('.stats_targetgroups_added_targetgroups');
								event5.preventDefault();	 
									   });
		
		
		$(".removeTargetgroup").live('click',function(event6){
								$(this).parent().remove();
								
								event6.preventDefault();	 
									   });
		
		
		$(".help_close A").click(
				function(event2){				 
						$('#helpbox_container').hide();
								 });

}); 