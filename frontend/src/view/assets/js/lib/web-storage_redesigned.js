(() => {

  const Storage = AGN.Lib.Storage;

  class WebStorage {

    static PREFIX = 'web-storage#';

    static get(key) {
      try {
        return Storage.get(WebStorage.PREFIX + $.trim(key));
      } catch (e) {
        console.error(e);
      }

      return null;
    }

    static set(key, val) {
      Storage.set(WebStorage.PREFIX + $.trim(key), val);
    }

    static extend(key, val) {
      let value = WebStorage.get(key);

      if (value != null) {
        value = $.extend(value, val);
      } else {
        value = val;
      }

      WebStorage.set(key, value);

      return value;
    }
  }

  AGN.Lib.WebStorage = WebStorage;

})();
