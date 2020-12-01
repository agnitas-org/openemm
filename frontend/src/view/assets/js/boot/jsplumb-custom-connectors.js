AGN.Opt.Components.JsPlumbFixedBezierConnector = function(params) {
  var _super = jsPlumb.Connectors.AbstractConnector.apply(this, arguments);

  this.type = 'FixedBezierConnector';
  this._compute = function(d /* , paintParams */) {
    var k = d.anchorOrientation === 'opposite' ? 2 : 1.5;

    _super.addSegment(this, 'Bezier', {
      x1: d.tx,
      y1: d.ty,
      x2: d.sx,
      y2: d.sy,
      cp1x: d.tx + d.to[0] * d.w/k,
      cp1y: d.ty + d.to[1] * d.h/k,
      cp2x: d.sx + d.so[0] * d.w/k,
      cp2y: d.sy + d.so[1] * d.h/k
    });
  };
};
