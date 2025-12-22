/*doc
---
title: Cache
name: cache
category: Javascripts - Cache
---

`AGN.Lib.InMemoryCache` allows you to cache some data for a certain time (default 1 minute).

Below are some methods you can call:

method           |description                                                     |
-----------------|----------------------------------------------------------------|
`set(key, data)` |Adds data to the cache                                          |
`get(key)`       |Returns data from the cache. If data was expired - returns null |
`clear()`        |Clears cache                                                    |
`delete(key)`    |Deletes data from the cache by specified key                    |

*/

(() => {

  class InMemoryCache {
    constructor(durationMs = 60000) {
      this.cache = new Map();
      this.cacheDuration = durationMs;
    }

    set(key, data) {
      const expiration = Date.now() + this.cacheDuration;
      this.cache.set(key, { data, expiration });
    }

    get(key) {
      const cachedData = this.cache.get(key);
      if (!cachedData) {
        return null;
      }

      if (cachedData.expiration < Date.now()) {
        this.delete(key);
        return null;
      }

      return cachedData.data;
    }

    clear() {
      this.cache.clear();
    }

    delete(key) {
      this.cache.delete(key);
    }
  }

  AGN.Lib.InMemoryCache = InMemoryCache;

})();
