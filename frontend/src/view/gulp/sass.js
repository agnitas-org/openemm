const gulp = require('gulp');
const path = require('path');
const insert = require('gulp-insert');
const sass = require('gulp-sass')(require('sass')); // Use Dart Sass
const cleanCSS = require('gulp-clean-css');
const config = require('./config');
const sourcemaps = require('gulp-sourcemaps');

function mapSources(filePath) {
  if (filePath.includes(path.resolve('.').substring(1))) {
    return path.relative(config.assets, `/${filePath}`).replace(/\\/g, '/');
  }

  return filePath;
}

module.exports = {
  buildCSS: () => {
    return gulp.src(`${config.assets}/sass/application.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass().on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write("./", {mapSources}))
      .pipe(gulp.dest(config.assets));
  },
  buildCssForStyleguide() {
    return gulp.src(`${config.assets}/sass/application.scss`)
      .pipe(insert.prepend(`$assets-path: '';\n`))
      .pipe(sourcemaps.init())
      .pipe(sass().on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write("./", {mapSources}))
      .pipe(gulp.dest(config.assets));
  },
  buildLandingCSS() {
    return gulp.src(`${config.assets}/sass/landing.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass().on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write("./", {mapSources}))
      .pipe(gulp.dest(config.assets));
  },
  buildFormCSS() {
    return gulp.src(`${config.assets}/sass-forms/form.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass().on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write("./", {mapSources}))
      .pipe(gulp.dest(config.assets));
  }
}
