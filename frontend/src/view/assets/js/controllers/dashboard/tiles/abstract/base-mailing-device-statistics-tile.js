(() => {

  class BaseMailingDeviceStatisticsTile extends AGN.Lib.Dashboard.BaseMailingStatisticsTile {

    constructor() {
      if (new.target === BaseMailingDeviceStatisticsTile) {
        throw new TypeError("Cannot construct Abstract BaseMailingDeviceStatisticsTile instances directly!");
      }

      super();
    }

    getDeviceTypesMap() {
      const DEVICE_TYPES = {
        PC: t('statistics.device.desktop'),
        TABLET: t('statistics.device.tablet'),
        MOBILE: t('statistics.device.mobile'),
        SMART_TV: t('statistics.device.smarttv'),
        MULTIPLE: t('statistics.device.mixed')
      };

      return Object.fromEntries(
        Object.entries(this._data.byDevice)
          .map(([deviceClass, value]) => [DEVICE_TYPES[deviceClass], value])
      );
    }

    getChartOptions() {
      const deviceTypesMap = this.getDeviceTypesMap();

      return {
        type: 'agnDoughnut',
        data: {
          labels: Object.keys(deviceTypesMap),
          datasets: [{
            data: Object.values(deviceTypesMap),
            backgroundColor: this._colors,
            borderWidth: 0
          }]
        }
      };
    }

    getTotalValue() {
      const values = Object.values(this._data.byDevice);
      return AGN.Lib.ChartUtils.getTotal(values);
    }

  }

  AGN.Lib.Dashboard.BaseMailingDeviceStatisticsTile = BaseMailingDeviceStatisticsTile;
})();
