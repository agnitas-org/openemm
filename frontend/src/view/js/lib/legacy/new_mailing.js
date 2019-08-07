$(document).ready(function() 
    { 
		$('#helpbox_container').hide();
	
		/*
		$(".help_link").click(function(event){
					$('#helpbox_container').toggle();
					event.preventDefault();	 
									   });
		*/			
		$(".assistant_step7_targetgroups_add").click(function(event5){
								var targetgroup = '<div><a href="#" class="removeTargetgroup"><img src="../../../assets/core/images/removetargetgroup2.png" /></a>'+$('#assistant_step7_targetgroups_select :selected').text()+'</div>';
								$(targetgroup).appendTo('.assistant_step7_targetgroups_added_targetgroups');
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