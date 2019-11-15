(function(){

  var GridView;

  GridView = function($target) {
    this.$el = $target;
  }

  GridView.prototype.update = function(config) {
    this.config = config;
    this.draw()
  }

  GridView.prototype.template = function() {
    var config = this.config;

    return _.template(
      '<div class="grid-stage center-block" style="width: {{= viewWidth}} px; height: {{= viewHeight }}px;">' +
        '<svg class="grid-view center-block" viewBox="0 0 {{= viewWidth }} {{= viewHeight }}" style="width: {{= viewWidth }}px; height: {{= viewHeight }}px">' +
          '<g class="grid-columns"></g>' +
        '</svg>' +
      '</div>'
    )({
      viewWidth: config.templateWidth,
      viewHeight: 100
    });
  }

  GridView.prototype.data = function() {
    var d3data = [],
        config = this.config;

    _.times(config.columnNum, function(col) {

      d3data.push({
        type: 'gutter',
        width: config.gutter / 2,
        offset: _.reduce(d3data, function(result, col) {
          return result + col.width;
        }, 0)
      })


      d3data.push({
        type: 'col',
        width: config.columnWidth,
        offset: _.reduce(d3data, function(result, col) {
          return result + col.width;
        }, 0)
      });


      d3data.push({
        type: 'gutter',
        width: config.gutter / 2,
        offset: _.reduce(d3data, function(result, col) {
          return result + col.width;
        }, 0)
      })
    });

    return d3data;
  }

  GridView.prototype.draw = function() {
    var col;

    this.$el.html(this.template());

    // create the d3 svg
    col = d3.select(this.$el.find('.grid-columns')[0])
      .selectAll("g")
      .data(this.data())
      .enter().append("g")
      .attr("transform", function(d, i) {
        return "translate(" + d.offset + ",0)";
      });

    col.append("rect")
      .attr("class", function(d) { return 'grid-view-' + d.type })
      .attr("width", function(d) { return d.width; })
      .attr("height", 100)

  }

  AGN.Lib.GridView = GridView;

})();


(function(){

  var GridColumnView;

  GridColumnView = function($target) {
    this.$el = $target;
  }

  GridColumnView.prototype.update = function(config) {
    this.config = config;
    this.draw()
  }

  GridColumnView.prototype.template = function() {
    var config = this.config;

    return _.template(
      '<div class="grid-stage center-block" style="width: {{= viewWidth }}"px"; height: {{= viewHeight }}px;">' +
        '<svg class="column-view center-block" viewBox="0 0 {{= viewWidth }} {{= viewHeight }}" style="width: {{= viewWidth }}px; height: {{= viewHeight }}px">' +
          '<g class="grid-columns"></g>' +
        '</svg>' +
      '</div>'
    )({
      viewWidth:  config.templateWidth,
      viewHeight: config.columnNum * 35 - 5
    });
  }

  GridColumnView.prototype.data = function() {
    var config = this.config,
        d3data = [];

    // create a dataset for each row
    _.times(config.columnNum, function(row) {
      var colData = [];

      colData.push({
        type: 'gutter',
        width: config.gutter / 2,
        offset: 0
      })

      // first column
      colData.push({
        type: 'col',
        width: (row + 1) * config.columnWidth + row * config.gutter,
        offset: _.reduce(colData, function(result, col) {
          return result + col.width;
        }, 0)
      })

      colData.push({
        type: 'gutter',
        width: config.gutter / 2,
        offset: _.reduce(colData, function(result, col) {
          return result + col.width;
        }, 0)
      })

      // rest of the columns
      _.times(config.columnNum - (row + 1), function(col) {

        colData.push({
          type: 'gutter',
          width: config.gutter / 2,
          offset: _.reduce(colData, function(result, col) {
            return result + col.width;
          }, 0)
        })

        colData.push({
          type: 'empty-col',
          width: config.columnWidth,
          offset: _.reduce(colData, function(result, col) {
            return result + col.width;
          }, 0)
        });

        colData.push({
          type: 'gutter',
          width: config.gutter / 2,
          offset: _.reduce(colData, function(result, col) {
            return result + col.width;
          }, 0)
        })
      });

      d3data.push(colData)
    });

    return d3data;
  }

  GridColumnView.prototype.draw = function() {
    var config = this.config,
        col, row;

    this.$el.html(this.template());

    // create rows
    row = d3.select(this.$el.find('.grid-columns')[0])
      .selectAll(".row")
      .data(this.data())
      .enter().append("g")
      .attr("class", "row")
      .attr("transform", function(d, i) {
        return "translate(0," + i * 35 + ")";
      });

    // enter the rows and add the cols
    col = row.selectAll(".col")
      .data(function(d){ return d; })
      .enter().append("g")
      .attr("class", "col")
      .attr("transform", function(d) {
        return "translate(" + d.offset + ",0)";
      });

    col.append("rect")
      .attr("class", function(d) { return 'column-view-' + d.type })
      .attr("width", function(d) { return d.width; })
      .attr("height", 30)

    row.append("text")
      .attr("class", "column-view-text")
      .attr("x", config.gutter / 2 + 10)
      .attr("y", 20)
      .text(function(d, i) {
        return _.template("{{= width }}px / {{= perc }}%")({
          width: d[1].width,
          perc: Math.floor( 100 * ( i + 1 ) / config.columnNum )
        });
      })

  }

  AGN.Lib.GridColumnView = GridColumnView;

})();
