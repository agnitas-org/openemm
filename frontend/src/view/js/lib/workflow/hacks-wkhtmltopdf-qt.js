/**
* Override prototype methods because of wkhtml2pdf 12.5 (patched qt) issues
*/
if (window.Prototype) {
    delete Array.prototype.toJSON;
}

Object.isUndefined = Object.isUndefined || function(object) {
    return typeof object === "undefined"
};
Array.prototype.merge =  Array.prototype.merge || function(array, args) {
    array = Array.prototype.slice.call(array, 0);
    var arrayLength = array.length, length = args.length;
    while (length--) array[arrayLength + length] = args[length];
    return array;
};

Function.prototype.bind = function (thisp) {
    if (arguments.length < 2 &&  Object.isUndefined(arguments[0])) {
        return this;
    }
    var fn = this, args = Array.prototype.slice.call(arguments, 1);
    return function () {
        var a = Array.prototype.merge(args, arguments);
        return fn.apply(thisp, a);
    };
};

(function(JSON) {
   var oldParse = JSON.parse;

   JSON.parse = function newParse(thisp, args) {
       try {
        return oldParse(thisp, args);
       } catch (e) {
        console.log('Error JSON parse occured! ' + thisp, e);
        throw e;
       }
   }
}(JSON));