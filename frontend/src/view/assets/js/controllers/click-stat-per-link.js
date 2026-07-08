AGN.Lib.Controller.new('click-stat-per-link', function () {

  const Table = AGN.Lib.Table;

  let targetGroups;
  let config;

  this.addDomInitializer("click-stat-per-link", function () {
    config = this.config;
    initTargetGroups();

    fetchData(config.mailingId, config.selectedTargets)
      .done(resp => {
        renderSummaryTable(resp.summary);
        resp.categories.forEach(category => renderCategoryTable(category, this.el));
      });
  });

  function initTargetGroups() {
    const targetGroupsMap = new Map(
      config.targetGroups.map(target => [target.id, target])
    );

    targetGroups = config.selectedTargets
      .map(targetId => {
        const targetGroup = targetGroupsMap.get(Number(targetId));
        return { id: targetGroup.id, name: targetGroup.targetName }
      })
      .filter(Boolean);

    targetGroups.unshift(getAllRecipientsTargetGroup());
  }

  function fetchData(mailingId, targetGroups) {
    return $.get(
      AGN.url(`/statistics/mailing/${(mailingId)}/clickStatPerLink.action`),
      { targetGroups }
    );
  }

  function getAllRecipientsTargetGroup() {
    return {
      id: null,
      name: t('statistics.default.allRecipients')
    }
  }

  function renderSummaryTable(summaryData) {
    new Table(
      $('#link-click-summary-table'),
      getSummaryTableColumnDefinitions(),
      prepareSummaryTableRows(summaryData),
      getDefaultTableOptions()
    );
  }

  function renderCategoryTable(categoryData, $container) {
    const $table = AGN.Lib.Template.dom('links-category-table');
    $container.append($table);

    new Table(
      $table,
      getCategoryTableColumnDefinitions(categoryData.category.name),
      prepareCategoryTableRows(categoryData),
      _.extend(
        getDefaultTableOptions(),
        {
          viewLinkTemplate: getProgressStatUrlTemplate(),
          viewInModal: true
        }
      )
    );
  }

  function getProgressStatUrlTemplate() {
    return `/statistics/mailing/link/{{- id }}/progress.action?` +
      new URLSearchParams({ targetGroups: config.selectedTargets }).toString();
  }

  function getSummaryTableColumnDefinitions() {
    const columns = [{
      headerName: `${t('statistics.default.summary')} (${t('statistics.default.clickers')})`,
      type: "textCaseInsensitiveColumn",
      field: "category",
      cellRenderer: "MustacheTemplateCellRender",
      cellRendererParams: {"templateName": "summary-cell"},
      sortable: false
    }];

    targetGroups.forEach(targetGroup => {
      columns.push({
        headerName: targetGroup.name,
        type: "textCaseInsensitiveColumn",
        field: makeTargetPropertyKey(targetGroup),
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {"templateName": "link-clickers-cell"},
        sortable: false
      })
    });

    return columns;
  }

  function prepareSummaryTableRows(summary) {
    const rows = [];

    summary.categories.forEach(entry => {
      const row = {category: entry.category.name};
      entry.targetGroups.forEach(t => row[makeTargetPropertyKey(t)] = getTargetGroupData(t));

      rows.push(row);
    });

    rows.push(getSummaryTotalRow(rows))

    return rows;
  }

  function getSummaryTotalRow(rows) {
    const row = {
      isTotal: true,
      category: t('statistics.default.total')
    }

    targetGroups.forEach(t => {
      const propName = makeTargetPropertyKey(t);
      const totalValue = rows.map(r => r[propName].total.clickers.value)
        .reduce((acc, value) => acc + value, 0);

      row[propName] = { totalClickers: AGN.formatNumber(totalValue) };
    });

    return row;
  }

  function getCategoryTableColumnDefinitions(categoryName) {
    const columns = [{
      headerName: categoryName,
      type: "textCaseInsensitiveColumn",
      field: "url",
      cellRenderer: "MustacheTemplateCellRender",
      cellRendererParams: {"templateName": "summary-cell"},
      sortable: false
    }];

    targetGroups.forEach(targetGroup => {
      columns.push({
        headerName: `${targetGroup.name} (${t('statistics.default.clicks')})`,
        type: "textCaseInsensitiveColumn",
        field: makeTargetPropertyKey(targetGroup),
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {"templateName": "link-clicks-cell"},
        sortable: false
      });

      columns.push({
        headerName: `${targetGroup.name} (${t('statistics.default.clickers')})`,
        type: "textCaseInsensitiveColumn",
        field: makeTargetPropertyKey(targetGroup),
        cellRenderer: "MustacheTemplateCellRender",
        cellRendererParams: {"templateName": "link-clickers-cell"},
        sortable: false
      })
    });

    return columns;
  }

  function prepareCategoryTableRows(categoryData) {
    const rows = [];

    categoryData.links.forEach(link => {
      const row = { id: link.id, url: link.url };
      link.targetGroups.forEach(t => row[makeTargetPropertyKey(t)] = getTargetGroupData(t));

      rows.push(row);
    });

    return rows;
  }

  function getTargetGroupData(data) {
    return {
      totalClickers: formatMetric(data.total.clickers),
      mobileClickers: formatMetric(data.mobile.clickers),
      anonymousClickers: formatMetric(data.anonymous.clickers),
      totalClicks: formatMetric(data.total.clicks),
      mobileClicks: formatMetric(data.mobile.clicks),
      anonymousClicks: formatMetric(data.anonymous.clicks),
      ... data
    }
  }

  function makeTargetPropertyKey(targetGroup) {
    return `target-${targetGroup.id}`;
  }

  function formatMetric(metric) {
    if (metric == null) return metric;
    return `${AGN.formatNumber(metric.value)} (${metric.rate.toFixed(1)} %)`;
  }

  function getDefaultTableOptions() {
    return {
      pagination: false,
      showRecordsCount: "simple",
      domLayout: "autoHeight"
    }
  }

});
