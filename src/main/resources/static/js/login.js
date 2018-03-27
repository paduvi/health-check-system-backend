$("#login-form").submit(function(event) {
	$.ajax({
		type : 'POST',
		url : '/login',
		data : $('#login-form').serialize(),
		cache : false,
		dataType : "json",
		crossDomain : false,
		success : function(data) {
			if (data.success == true) {
				$('#login-form').fadeOut(500);
				$('.wrapper').addClass('form-success');
				setTimeout(function() {
					window.location = "/";
				}, 1000);
				return;
			}
			$(".alert-box").hide();
			$(".alert-box.error").show();
		},
		error : function(data) {
			$(".alert-box").hide();
			$(".alert-box.error").show();
		}
	});
	event.preventDefault();
});
