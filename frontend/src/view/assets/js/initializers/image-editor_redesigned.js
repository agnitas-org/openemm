AGN.Lib.CoreInitializer.new('image-editor', function($scope = $(document)) {
  _.each($scope.find('#editor-canvas'), canvas => {
    const context = canvas.getContext('2d');
    const imageObj = new Image();
    imageObj.src = $('#editor-img').attr('src');

    imageObj.onload = function() {
      const width = imageObj.width;
      const height = imageObj.height;
      context.canvas.width = width;
      context.canvas.height = height;
      context.drawImage(imageObj, 0, 0, width, height);
    };
  });
});