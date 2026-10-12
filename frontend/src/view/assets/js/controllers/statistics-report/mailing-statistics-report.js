class MailingStatisticsReportController extends AGN.Lib.StatisticsReportBaseController {

  static DEVICE_CLASS_LABELS = {
    DESKTOP: () => t('statistics.device.desktop'),
    TABLET: () => t('statistics.device.tablet'),
    MOBILE: () => t('statistics.device.mobile'),
    SMARTTV: () => t('statistics.device.smarttv')
  };

  static DEVICE_CLASSES = Object.keys(MailingStatisticsReportController.DEVICE_CLASS_LABELS);

  static SHARE_ROW_DEFS = [
    {key: 'total', label: () => t('statistics.default.total'), bold: true},
    {key: 'mobile', label: () => t('statistics.default.mobileShare')},
    {key: 'anonymous', label: () => t('statistics.default.anonymousShare')}
  ];

  static MAIL_FORMAT_COLUMN_DEFS = [
    {figure: 'HTML', field: 'html', label: () => t('mailing.default.mailFormat.html')},
    {figure: 'TEXT', field: 'text', label: () => t('mailing.default.mailFormat.text')},
    {figure: 'OFFLINE_HTML', field: 'offlineHtml', label: () => t('mailing.default.mailFormat.html_offline')}
  ];

  static LINK_CLICK_METRICS = [
    {field: 'clickers', label: () => t('statistics.default.clickers')},
    {field: 'clicks', label: () => t('statistics.default.clicks')}
  ];

  static DEVICE_TYPE_METRICS = [
    {
      figure: 'OPENERS_AFTER_DEVICE',
      field: 'openersByDeviceType',
      totalField: 'openersMeasured',
      label: () => t('statistics.report.openersMeasured')
    },
    {
      figure: 'CLICKERS_AFTER_DEVICE',
      field: 'clickersByDeviceType',
      totalField: 'clickers',
      label: () => t('statistics.default.clicker')
    }
  ];

  static DEVICE_NAME_METRICS = [
    {
      figure: 'OPENERS_DEVICES',
      field: 'openersByDeviceName',
      label: () => t('statistics.default.openers')
    },
    {
      figure: 'CLICKER_DEVICES',
      field: 'clickersByDeviceName',
      label: () => t('statistics.default.clickers')
    }
  ];

  static BOUNCE_TYPE_METRICS = [
    {
      figure: 'SOFTBOUNCES',
      field: 'softBounces',
      label: () => t('statistics.report.softBounces'),
      includeUndelivered: true
    },
    {
      figure: 'HARDBOUNCES',
      field: 'hardBounces',
      label: () => t('statistics.default.hardBounces'),
      includeUndelivered: false
    }
  ];

  static LINK_CATEGORIES = [
    {key: 'leadingLinks', label: () => t('statistics.report.leadingLinks')},
    {key: 'administrativeLinks', label: () => t('statistics.report.adminLinks')}
  ];

  static DOMAIN_METRICS = [
    {field: 'emailsSent', label: () => t('statistics.default.sentEmails')},
    {field: 'hardBounces', label: () => t('statistics.default.hardBounces')},
    {field: 'softBounces', label: () => t('statistics.report.softBounces')},
    {field: 'openers', label: () => t('statistics.default.openers')},
    {field: 'clickers', label: () => t('statistics.default.clickers')}
  ];

  static MAX_BARS_PER_CHART = 60;

  render() {
    this.#renderReportSections();
    this.#renderSummaryCharts();
    this.#renderSummaryTables();
    this.#renderDeviceStats();
    this.#renderBouncesAndLinksSummary();
    this.#renderLinkClicksDetail();
    this.#renderDomainStats();
  }

  #renderReportSections() {
    const hasMailTypeStats = ['HTML', 'TEXT', 'OFFLINE_HTML'].some(f => this.figures.includes(f));
    const hasSummaryStats = this.#getSummaryMetrics().some(metric => this.figures.includes(metric.figure));

    this.config.mailingIds?.forEach((mailingId, idx) => {
      const data = this.config.mailingsData[mailingId];

      this.$container.append(AGN.Lib.Template.text('mailing-report-section', {
        forceNewPage: idx > 0,
        generalInfoRows: data.generalInfo ? this.#buildGeneralInfoRows(data.generalInfo) : null,
        thumbnailUrl: data.thumbnailUrl,
        mailFormatColumns: hasMailTypeStats ? this.#buildMailFormatColumns(data.recipientsByMailFormat) : null,
        hasSummary: hasSummaryStats && data.targetGroupsData?.length > 0,
        mailingId
      }));
    });
  }

  #buildGeneralInfoRows(info) {
    return this.#getGeneralInfoRowDefs(info)
      .filter(row => this.figures.includes(row.figure));
  }

  #getGeneralInfoRowDefs(info) {
    return [
      {figure: 'MAILING_NAME', label: t('statistics.report.mailingName'), value: info.name ?? ''},
      {figure: 'MAILING_ID', label: t('statistics.report.mailingId'), value: info.id ?? ''},
      {figure: 'DESCRIPTION', label: t('statistics.report.description'), value: info.description ?? ''},
      {figure: 'ARCHIVE', label: t('statistics.report.archive'), value: info.archive ?? ''},
      {figure: 'MAIL_FORMAT', label: t('statistics.report.mailFormat'), value: this.#getMailTypeLabel(info.mailType)},
      {figure: 'AVERAGE_SIZE', label: t('statistics.report.averageSize'), value: info.avgMailSizeKb != null ? AGN.formatNumber(info.avgMailSizeKb) : ''},
      {figure: 'SUBJECT', label: t('statistics.report.subject'), value: info.subject ?? ''},
      {figure: 'MAILING_LIST', label: t('statistics.report.mailingList'), value: info.mailinglist ?? ''},
      {
        figure: 'TARGET_GROUPS', label: t('statistics.report.targets'),
        value: info.targetGroups?.length > 0
          ? info.targetGroups.map(name => _.escape(name)).join('<br>')
          : _.escape(t('statistics.default.allRecipients')),
        rawHtml: true
      },
      {
        figure: 'NR_OF_RECIPIENTS_WITH_TRACK_VETO', label: t('statistics.report.recipientsNoTracking'),
        value: `${info.recipientsCount != null ? AGN.formatNumber(info.recipientsCount) : ''} (${info.anonymousRecipientsCount != null ? AGN.formatNumber(info.anonymousRecipientsCount) : ''})`
      },
      {figure: 'SENDING_STARTED', label: t('statistics.report.sendingStarted'), value: info.sendingStarted ?? ''},
      {figure: 'SENDING_FINISHED', label: t('statistics.report.sendingFinished'), value: info.sendingFinished ?? ''},
      {
        figure: 'SENDER_INFO', label: t('statistics.report.senderInfo'),
        value: this.#getNameEmailLabel(info.senderName, info.senderEmail)
      },
      {
        figure: 'REPLY_TO_INFO', label: t('statistics.report.replyInfo'),
        value: this.#getNameEmailLabel(info.replyName, info.replyEmail)
      }
    ];
  }

  #getMailTypeLabel(mailType) {
    return mailType ? t(`mailing.default.mailFormat.${mailType.toLowerCase()}`) : '';
  }

  #getNameEmailLabel(name, email) {
    if (!name) {
      return email ?? '';
    }

    return `${name} <${email ?? ''}>`;
  }

  #buildMailFormatColumns(recipientsByType) {
    const defs = MailingStatisticsReportController.MAIL_FORMAT_COLUMN_DEFS
      .filter(d => this.figures.includes(d.figure));

    if (defs.length === 0 || !recipientsByType) {
      return null;
    }

    const total = recipientsByType.html + recipientsByType.text + recipientsByType.offlineHtml;

    return defs.map(d => {
      const value = recipientsByType[d.field];
      return {
        label: d.label(),
        value: AGN.formatNumber(value) ?? value,
        percent: total > 0 ? Math.round((value / total) * 100) : 0
      };
    });
  }

  #getSummaryMetrics() {
    const rate = raw => this.formatRateCell(raw);
    const count = raw => this.formatCountCell(raw);
    const currency = raw => this.formatCurrencyCell(raw);

    return [
      {figure: 'SENT_MAILS', field: 'sentMails', label: t('statistics.default.sentEmails'), color: 'darkest-blue', formatCell: rate},
      {figure: 'EMAILS_ACCEPTED', field: 'emailsAccepted', label: t('statistics.default.acceptedEmails'), color: 'blue', formatCell: rate},
      {
        figure: 'OPENERS_MEASURED', field: 'openersMeasured', label: t('statistics.report.openersMeasured'), color: 'dark-cyan', formatCell: rate,
        subField: 'openersProxy', subLabel: t('statistics.report.openersProxy'), subColor: 'light-cyan'
      },
      {figure: 'OPENERS_INVISIBLE', field: 'openersInvisible', label: t('statistics.report.openersInvisible'), color: 'cyan', formatCell: rate},
      {figure: 'OPENERS_TOTAL', field: 'openersTotal', label: t('statistics.default.totalOpeners'), color: 'darkest-cyan', formatCell: rate},
      {figure: 'OPENINGS_ANONYMOUS', field: 'anonymousOpenings', label: t('statistics.progress.anonymousOpenings'), color: 'light-blue', formatCell: count},
      {figure: 'CLICKERS_TOTAL', field: 'clickers', label: t('statistics.default.clicker'), color: 'dark-yellow', formatCell: rate},
      {figure: 'CLICKERS_AFTER_DEVICE', field: 'clicks', label: t('statistics.default.clicks'), color: 'orange', formatCell: rate},
      {figure: 'CLICKS_ANONYMOUS', field: 'anonymousClicks', label: t('statistics.progress.anonymousClicks'), color: 'yellow', formatCell: count},
      {figure: 'SIGNED_OFF', field: 'optOuts', label: t('statistics.optOuts'), color: 'violet', formatCell: rate},
      {figure: 'SOFTBOUNCES', field: 'softBounces', label: t('statistics.report.softBounces'), color: 'light-violet', formatCell: rate},
      {figure: 'HARDBOUNCES', field: 'hardBounces', label: t('statistics.default.hardBounces'), color: 'dark-red', formatCell: rate},
      {figure: 'REVENUE', field: 'revenue', label: t('statistics.default.revenue'), color: 'darkest-violet', formatCell: currency}
    ];
  }

  #renderSummaryCharts() {
    const metrics = this.#getSummaryMetrics().filter(metric => this.figures.includes(metric.figure));
    if (metrics.length === 0) {
      return;
    }

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const { targetGroupsData } = data;
      if (!targetGroupsData?.length) {
        return;
      }

      const $canvas = $(`[data-summary-chart="${mailingId}"]`);
      const summary = targetGroupsData[0].summary;

      new Chart($canvas[0].getContext('2d'), {
        type: 'bar',
        data: this.#buildSummaryChartData(metrics, summary),
        options: {
          indexAxis: 'y',
          animation: false,
          scales: {
            x: {
              stacked: true,
              beginAtZero: true,
              display: false
            },
            y: {
              stacked: true,
              // Widens the auto-sized label column to avoid clipping the longest label under Puppeteer
              afterFit: scale => { scale.width += 16; }
            }
          },
          plugins: {
            ...this.backgroundColorPlugin,
            datalabels: { display: false }
          }
        }
      });
    });
  }

  #buildSummaryChartData(metrics, summary) {
    const labels = [];
    const mainVals = [];
    const mainColors = [];
    const subVals = [];
    const subColors = [];

    metrics.forEach(metric => {
      const total = this.metricValue(summary[metric.field]);
      labels.push(metric.label);
      mainColors.push(AGN.Lib.Helpers.getStatColorHexByName(metric.color));

      if (metric.subField) {
        const subVal = this.metricValue(summary[metric.subField]);
        mainVals.push(total - subVal);
        subVals.push(subVal);
        subColors.push(AGN.Lib.Helpers.getStatColorHexByName(metric.subColor));
      } else {
        mainVals.push(total);
        subVals.push(0);
        subColors.push('rgba(0,0,0,0)');
      }
    });

    return {
      labels,
      datasets: [
        {
          data: mainVals,
          backgroundColor: mainColors
        },
        {
          data: subVals,
          backgroundColor: subColors,
          minBarLength: 0
        }
      ]
    };
  }

  #renderSummaryTables() {
    const metrics = this.#getSummaryMetrics()
      .filter(metric => this.figures.includes(metric.figure));

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const targetGroupsData = data.targetGroupsData;
      if (!targetGroupsData?.length) {
        return;
      }

      this.renderChunkedTables(
        $(`[data-summary-tables="${mailingId}"]`),
        targetGroupsData,
        this.constructor.MAX_COLUMNS_PER_TABLE,
        (chunk, idx) => {
          const rows = metrics.map(metric => this.#buildSummaryTableRow(metric, chunk));
          const rowCount = rows.length + rows.filter(row => row.subLabel).length;
          return ['mailing-summary-table', { columns: chunk, rows, rowCount, idx }];
        }
      );
    });
  }

  #buildSummaryTableRow(metric, chunk) {
    return {
      label: metric.label,
      color: AGN.Lib.Helpers.getStatColorHexByName(metric.color),
      subLabel: metric.subField ? metric.subLabel : null,
      subColor: metric.subField ? AGN.Lib.Helpers.getStatColorHexByName(metric.subColor) : null,
      cells: chunk.map(col => metric.formatCell(col.summary[metric.field])),
      subCells: metric.subField ? chunk.map(col => this.formatRateCell(col.summary[metric.subField])) : null
    };
  }

  #getDeviceClassLabel(deviceClass) {
    return MailingStatisticsReportController.DEVICE_CLASS_LABELS[deviceClass]();
  }

  #renderDeviceTypeSection($container, title, targetGroupsData, fieldName, totalField, totalLabel) {
    const targets = this.toTargets(targetGroupsData);
    const byTargetId = this.indexByTargetId(targetGroupsData);

    const metricDefs = [
      {key: 'total', label: totalLabel, chartSlice: false},
      {key: 'pc', label: t('statistics.device.desktop')},
      {key: 'tablet', label: t('statistics.device.tablet')},
      {key: 'mobile', label: t('statistics.device.mobile')},
      {key: 'smartTv', label: t('statistics.device.smarttv')},
      {key: 'multi', label: t('statistics.device.mixed')}
    ];

    this.renderStatSection($container, title, targets, metricDefs, (targetId, m) => {
      const tgData = byTargetId[targetId];
      if (!tgData) {
        return null;
      }
      if (m.key === 'total') {
        return tgData.summary[totalField];
      }
      return tgData[fieldName]?.[m.key] ?? null;
    });
  }

  #renderDeviceNameSection($container, title, targetGroupsData, fieldName, deviceClass) {
    const targets = this.toTargets(targetGroupsData);
    const deviceRowsByTargetId = {};
    targetGroupsData.forEach(tgData =>
      deviceRowsByTargetId[tgData.targetId] = tgData[fieldName]?.[deviceClass] || []);

    const seen = new Map();
    Object.values(deviceRowsByTargetId).forEach(rows => {
      rows.forEach(row => {
        if (!seen.has(row.deviceName)) {
          seen.set(row.deviceName, row.other ? t('statistics.other') : row.deviceName);
        }
      });
    });

    if (seen.size === 0) {
      return;
    }

    const orderedKeys = Array.from(seen.keys()).sort((a, b) => (a === null) - (b === null));
    const metricDefs = orderedKeys.map(key => ({key, label: seen.get(key)}));

    const deviceValueByTargetId = {};
    Object.entries(deviceRowsByTargetId).forEach(([targetId, rows]) => {
      const map = {};
      rows.forEach(row => map[row.deviceName] = row.value);
      deviceValueByTargetId[targetId] = map;
    });

    this.renderStatSection($container, title, targets, metricDefs, (targetId, m) => {
      return deviceValueByTargetId[targetId]?.[m.key] ?? null;
    });
  }

  #renderDeviceStats() {
    const deviceTypeMetrics = MailingStatisticsReportController.DEVICE_TYPE_METRICS
      .filter(m => this.figures.includes(m.figure));
    const deviceNameMetrics = MailingStatisticsReportController.DEVICE_NAME_METRICS
      .filter(m => this.figures.includes(m.figure));

    if (!deviceTypeMetrics.length && !deviceNameMetrics.length) {
      return;
    }

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const { targetGroupsData } = data;
      if (!targetGroupsData?.length) {
        return;
      }

      const $container = $(`[data-device-stats="${mailingId}"]`);
      const devicesTitle = t('statistics.report.devices');

      deviceTypeMetrics.forEach(metric =>
        this.#renderDeviceTypeSection(
          $container,
          `${devicesTitle}: ${metric.label()}`,
          targetGroupsData,
          metric.field,
          metric.totalField,
          metric.label()
        )
      );

      deviceNameMetrics.forEach(metric =>
        MailingStatisticsReportController.DEVICE_CLASSES.forEach(deviceClass =>
          this.#renderDeviceNameSection(
            $container,
            `${devicesTitle}: ${this.#getDeviceClassLabel(deviceClass)} ${metric.label()}`,
            targetGroupsData,
            metric.field,
            deviceClass
          )
        )
      );
    });
  }

  #renderBounceTypeSection($container, title, targetGroupsData, reasonsField, includeUndelivered) {
    const UNDELIVERED_KEY = '__undelivered__';
    const targets = this.toTargets(targetGroupsData);
    const byTargetId = this.indexByTargetId(targetGroupsData);

    const referenceData = targetGroupsData[0];
    const reasons = referenceData.bounces?.[reasonsField];
    if (!reasons?.length) {
      return;
    }

    const metricDefs = reasons.map((r, i) => {
      const isTotalRow = i === reasons.length - 1;
      return {
        key: r.reason,
        label: r.reason,
        chartSlice: !isTotalRow,
        noColor: isTotalRow,
        bold: isTotalRow
      };
    });

    if (includeUndelivered) {
      metricDefs.push({
        key: UNDELIVERED_KEY,
        label: t('statistics.bounce.undelivered'),
        chartSlice: false,
        noColor: true
      });
    }

    this.assignColors(metricDefs);

    const getValue = (targetId, m) => {
      const col = byTargetId[targetId];
      if (!col?.bounces) {
        return null;
      }

      if (m.key === UNDELIVERED_KEY) {
        return col.bounces.undelivered;
      }

      return col.bounces[reasonsField]?.find(x => x.reason === m.key)?.value ?? null;
    };

    const chartMetrics = metricDefs.filter(m => m.chartSlice !== false);
    const referenceTarget = targets[0];
    const values = chartMetrics.map(m => this.metricValue(getValue(referenceTarget.id, m)));
    const hasData = values.some(v => v);

    const $section = AGN.Lib.Template.dom('bounce-section', {});
    $container.append($section);

    $section.find('[data-chart]').append(
      AGN.Lib.Template.text('stat-donut-tile', {targetName: title, hasData})
    );

    if (hasData) {
      this.renderDoughnutChart($section.find('canvas')[0], chartMetrics, values);
    }

    this.renderLegendTable($section.find('[data-table]'), title, targets, metricDefs, getValue);
  }

  #renderLinkClicksSummaryTable($container, targetGroupsData) {
    const targets = this.toTargets(targetGroupsData);
    const byTargetId = this.indexByTargetId(targetGroupsData);

    const shareRow = (def, categoryKey, chunkTargets) => ({
      label: def.label(),
      cells: chunkTargets.map(target => {
        const stats = byTargetId[target.id]?.linkClicks;
        const share = stats ? stats[categoryKey].summary[def.key] : null;
        return this.formatRateCell(share?.clickers);
      })
    });

    const buildGroup = (label, categoryKey, chunkTargets) => ({
      label,
      rows: MailingStatisticsReportController.SHARE_ROW_DEFS.map(def => shareRow(def, categoryKey, chunkTargets))
    });

    const totalCellsFor = chunkTargets => chunkTargets.map(tg => {
      const stats = byTargetId[tg.id]?.linkClicks;
      if (!stats) {
        return this.formatRateCell(null);
      }
      const leading = stats.leadingLinks.summary.total.clickers || { value: 0, rate: 0 };
      const admin = stats.administrativeLinks.summary.total.clickers || { value: 0, rate: 0 };
      return this.formatRateCell({value: leading.value + admin.value, rate: leading.rate + admin.rate});
    });

    this.renderChunkedTables(
      $container,
      targets,
      this.constructor.MAX_COLUMNS_PER_TABLE,
      (chunk, idx) => {
        const groups = MailingStatisticsReportController.LINK_CATEGORIES
          .map(cat => buildGroup(cat.label(), cat.key, chunk));

        return ['link-clicks-summary-table', {
          title: `${t('statistics.default.summary')} (${t('statistics.default.clickers')})`,
          targets: chunk,
          groups,
          totalCells: totalCellsFor(chunk),
          idx
        }];
      }
    );
  }

  #renderBouncesAndLinksSummary() {
    const bounceMetrics = MailingStatisticsReportController.BOUNCE_TYPE_METRICS
      .filter(m => this.figures.includes(m.figure));
    const hasLinks = this.figures.includes('ACTIVATE_LINK_STATISTICS');

    if (!bounceMetrics.length && !hasLinks) {
      return;
    }

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const { targetGroupsData } = data;
      if (!targetGroupsData?.length) {
        return;
      }

      const $container = $(`[data-bounces-and-links="${mailingId}"]`);

      const $bounces = $('<div class="mb-2"></div>');
      bounceMetrics.forEach(metric =>
        this.#renderBounceTypeSection(
          $bounces,
          metric.label(),
          targetGroupsData,
          metric.field,
          metric.includeUndelivered
        )
      );

      if ($bounces.children().length) {
        this.appendSectionHeading($container, t('statistics.report.bounces'));
        $container.append($bounces);
      }

      if (hasLinks) {
        this.#renderLinkClicksSummaryTable($container, targetGroupsData);
      }
    });
  }

  #sortLinksByVolume(links, metricField) {
    return links.slice().sort((a, b) => {
      const aVal = a.stats?.total?.[metricField]?.value ?? 0;
      const bVal = b.stats?.total?.[metricField]?.value ?? 0;
      return bVal - aVal;
    });
  }

  #getReferenceLinks(targetGroupsData, categoryKey) {
    const category = targetGroupsData[0].linkClicks?.[categoryKey];
    return category?.links || [];
  }

  #findLinkStatsForTarget(byTargetId, targetId, categoryKey, linkId) {
    const category = byTargetId[targetId]?.linkClicks?.[categoryKey];
    return category?.links.find(l => l.linkId === linkId)?.stats ?? null;
  }

  #linkEntryValue(byTargetId, target, metricField, entry) {
    const stats = this.#findLinkStatsForTarget(byTargetId, target.id, entry.categoryKey, entry.link.linkId);
    return stats?.total?.[metricField]?.value ?? 0;
  }

  #renderLinkClicksBarChart($container, sectionTitle, metricField, targets, byTargetId, categorized) {
    const entries = categorized.flatMap(c => c.refLinks.map(link => ({link, categoryKey: c.key})));
    if (!entries.length) {
      return false;
    }

    const hasData = entries.some(entry => targets.some(target => this.#linkEntryValue(byTargetId, target, metricField, entry)));
    if (!hasData) {
      return false;
    }

    const colors = AGN.Lib.Helpers.getStatColorsHexValues();
    const maxLinksPerChart = Math.max(1, Math.floor(MailingStatisticsReportController.MAX_BARS_PER_CHART / targets.length));

    let numberOffset = 0;
    this.chunkBalanced(entries, maxLinksPerChart).forEach(chunkEntries => {
      const datasets = targets.map((target, i) => ({
        label: target.name,
        data: chunkEntries.map(entry => this.#linkEntryValue(byTargetId, target, metricField, entry)),
        backgroundColor: colors[i % colors.length]
      }));

      const legends = targets.map((tg, i) => ({
        name: tg.name,
        color: colors[i % colors.length]
      }));

      const $page = AGN.Lib.Template.dom('link-clicks-chart-page', { title: sectionTitle, legends });
      $container.append($page);
      const $canvas = $page.find('canvas');

      new Chart($canvas[0].getContext('2d'), {
        type: 'bar',
        data: {
          labels: chunkEntries.map((entry, idx) => String(numberOffset + idx + 1)),
          datasets
        },
        options: {
          indexAxis: 'y',
          animation: false,
          scales: {
            x: {
              beginAtZero: true,
              display: false
            }
          },
          layout: {
            padding: {
              top: 0,
              right: 100
            }
          },
          plugins: {
            ...this.backgroundColorPlugin,
            legend: { display: false },
            datalabels: {
              offset: 0,
              align: 'right'
            }
          }
        }
      });

      numberOffset += chunkEntries.length;
    });

    return true;
  }

  #renderLinkDetailTables($container, metricField, targets, byTargetId, category, numberOffset, firstChunkForcesNewPage) {
    const { label: categoryLabel, key: categoryKey, refLinks } = category;
    if (!refLinks.length) {
      return;
    }

    const buildLinks = chunkTargets => refLinks.map((refLink, idx) => ({
      number: numberOffset + idx + 1,
      url: refLink.url,
      rows: MailingStatisticsReportController.SHARE_ROW_DEFS.map((def, defIdx) => {
        const classes = [];
        if (def.bold) {
          classes.push('fw-medium');
        }
        if (defIdx === MailingStatisticsReportController.SHARE_ROW_DEFS.length - 1) {
          classes.push('table-row-divider');
        }
        return {
          label: def.label(),
          rowClass: classes.join(' '),
          cells: chunkTargets.map(target => {
            const stats = this.#findLinkStatsForTarget(byTargetId, target.id, categoryKey, refLink.linkId);
            const share = stats?.[def.key]?.[metricField];
            return this.formatRateCell(share);
          })
        };
      })
    }));

    this.renderChunkedTables($container, targets, this.constructor.MAX_COLUMNS_PER_TABLE, (chunk, idx) => [
      'link-category-table',
      {
        title: categoryLabel,
        forceNewPage: idx === 0 ? firstChunkForcesNewPage : false,
        targets: chunk,
        links: buildLinks(chunk),
        idx
      }
    ]);
  }

  #renderLinkClicksDetail() {
    if (!this.figures.includes('ACTIVATE_LINK_STATISTICS')) {
      return;
    }

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const { targetGroupsData } = data;
      if (!targetGroupsData?.length || !targetGroupsData[0].linkClicks) {
        return;
      }

      const targets = this.toTargets(targetGroupsData);
      const byTargetId = this.indexByTargetId(targetGroupsData);

      const $container = $(`[data-link-clicks-detail="${mailingId}"]`);

      MailingStatisticsReportController.LINK_CLICK_METRICS.forEach(({field: metricField, label}) => {
        const categorized = MailingStatisticsReportController.LINK_CATEGORIES.map(cat => ({
          key: cat.key,
          label: cat.label(),
          refLinks: this.#sortLinksByVolume(this.#getReferenceLinks(targetGroupsData, cat.key), metricField)
        }));

        if (!categorized.some(c => c.refLinks.length)) {
          return;
        }

        const sectionTitle = `${t('statistics.report.clickStatistics')}: ${label()}`;
        const chartRendered = this.#renderLinkClicksBarChart($container, sectionTitle, metricField, targets, byTargetId, categorized);

        let numberOffset = 0;
        categorized.forEach((c, catIdx) => {
          this.#renderLinkDetailTables($container, metricField, targets, byTargetId, c, numberOffset, catIdx > 0 || chartRendered);
          numberOffset += c.refLinks.length;
        });
      });
    });
  }

  #renderDomainStats() {
    if (!this.figures.includes('TOP_DOMAINS')) {
      return;
    }

    Object.entries(this.config.mailingsData).forEach(([mailingId, data]) => {
      const { targetGroupsData } = data;
      if (!targetGroupsData?.length || !targetGroupsData[0].domainStats) {
        return;
      }

      const targets = this.toTargets(targetGroupsData);
      const byTargetId = this.indexByTargetId(targetGroupsData);

      const domainsByTarget = field => {
        const result = {};
        targets.forEach(target => {
          const stats = byTargetId[target.id].domainStats;
          result[target.id] = stats?.[field] || [];
        });
        return result;
      };

      const $container = $(`[data-domain-stats="${mailingId}"]`);
      const domainsTitle = t('statistics.report.domains');

      this.appendSectionHeading($container, domainsTitle);
      MailingStatisticsReportController.DOMAIN_METRICS.forEach(metric =>
        this.#renderDomainTable($container, metric.label(), targets, domainsByTarget(metric.field))
      );
    });
  }

  #renderDomainTable($container, title, targets, domainsByTargetId) {
    const referenceDomains = domainsByTargetId[targets[0].id];
    if (!referenceDomains.length) {
      return;
    }

    const rows = referenceDomains.map(entry => ({
      domain: entry.domain,
      cells: targets.map(target => {
        const match = domainsByTargetId[target.id].find(d => d.domain === entry.domain);
        return this.formatRateCell(match?.value ?? null);
      })
    }));

    $container.append(AGN.Lib.Template.text('domain-table', { title, targets, rows }));
  }

}

AGN.Lib.Controller.new('mailing-statistics-report', MailingStatisticsReportController);
