$.fn.serializeObject = function() {
  var map = {};

  $.each(this.serializeArray(), function() {
    if (map[this.name]) {
      if (!map[this.name].push) {
        map[this.name] = [map[this.name]];
      }
      map[this.name].push(this.value || '');
    } else {
      map[this.name] = this.value || '';
    }
  });

  return map;
};

$.fn.serializeFormDataObject = function() {
  var map = this.serializeObject();

  this.find('input[type=file]').each(function() {
    if (!$(this).is(':disabled')) {
      var files = this.files;

      if (files.length == 0) {
        // Make this implementation compliant with FormData constructor's behavior.
        files = [new File([], '', {type: 'application/octet-stream'})];
      }

      if (map[this.name]) {
        var previousValues = map[this.name];

        if ($.isArray(previousValues)) {
          map[this.name] = previousValues.concat(files);
        } else {
          map[this.name] = [previousValues].concat(files);
        }
      } else {
        if (files.length > 1) {
          map[this.name] = files;
        } else {
          map[this.name] = files[0];
        }
      }
    }
  });

  return map;
};
