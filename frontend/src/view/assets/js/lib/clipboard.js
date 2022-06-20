(function() {
  function setViaClipboardApi(value, callback) {
    try {
      navigator.clipboard.writeText(value).then(function() {
        callback(true);
      }, function() {
        callback(false);
      });
    } catch (e) {
      console.warn(e);
      callback(false);
    }
  }

  function setViaTempInput(value, callback) {
    var success = true;
    try {
      var $input = $('<input>');
      $("body").append($input);
      $input.val(value);
      $input.select();
      document.execCommand("copy");
      $input.remove();
    } catch (e) {
      console.warn(e);
      success = false;
    }

    if (callback) {
      callback(success);
    }
  }

  AGN.Lib.Clipboard = {
    set: function(value, callback) {
      setViaClipboardApi(value, function(success1) {
        if (success1) {
          if (callback) {
            callback(value, true);
          }
        } else {
          setViaTempInput(value, function(success2) {
            if (callback) {
              callback(value, success2);
            }
          });
        }
      });
    }
  };
})();
