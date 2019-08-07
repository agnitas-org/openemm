$(document).ready(function() 
    { 
		$('.schablonen_container').hide();
		$('#helpbox_container').hide();
		$('.other_media_container').hide();
		$('.settings_general_container').hide();
		$('.settings_targetgroups_container').hide();
        
		$(".schablonen_toggle").click(function(event4){
					$(this).toggleClass('toggle_open');						   
					$(this).toggleClass('toggle_closed');						   
					$(this).next().toggle();
					event4.preventDefault();	 
		});
		
		$(".settings_toggle").click(function(event3){
					$(this).toggleClass('toggle_open');
					$(this).toggleClass('toggle_closed');
					$(this).next().toggle();
					event3.preventDefault();	 
		});
		
		/*
		$(".help_link").click(function(event){
					$('#helpbox_container').toggle();
					event.preventDefault();	 
									   });
		*/			
		$(".settings_targetgroups_add").click(function(event5){
								var targetgroup = '<div>'+$('#settings_targetgroups_select :selected').text()+'<a href="#" class="removeTargetgroup"><img src="../img/removetargetgroup.png" /></a></div>';
								$(targetgroup).appendTo('.settings_targetgroups_added_targetgroups');
								event5.preventDefault();	 
									   });
		
		/*
		$(".removeTargetgroup").live('click',function(event6){
								$(this).parent().remove();
								
								event6.preventDefault();	 
									   });
		
		*/
		$(".help_close A").click(
				function(event2){				 
						$('#helpbox_container').hide();
								 });

}); 