const gulp = require('gulp');
const svgSprite = require('gulp-svg-sprite');
const config = require('./config');

module.exports = {
  wysiwyg: () => {
    return gulp.src(`${config.assets}/core/images/wysiwyg/icons/*.svg`)
      .pipe(svgSprite({
        mode: {
          defs: {
            dest: './',
            sprite: 'wysiwyg-icon-sprite.svg'
          }
        }
      }))
      .pipe(gulp.dest(`${config.assets}/core/images/wysiwyg`));
  },
  workflow: () => {
    return gulp.src(`${config.assets}/core/images/campaignManager/icons/*.svg`)
      .pipe(svgSprite({
        mode: {
          defs: {
            dest: './',
            sprite: 'campaign-icon-sprite.svg'
          }
        }
      }))
      .pipe(gulp.dest(`${config.assets}/core/images/campaignManager`));
  },
  dashboard: () => {
    return gulp.src(`${config.assets}/core/images/dashboard/tile/*.svg`)
      .pipe(svgSprite({
        mode: {
          defs: {
            dest: './',
            sprite: 'dashboard-tile-sprite.svg'
          }
        }
      }))
      .pipe(gulp.dest(`${config.assets}/core/images/dashboard`));
  },
  sprite: () => {
    return gulp.src(`${config.assets}/core/images/facelift/svg/*.svg`)
      .pipe(svgSprite({
        mode: {
          defs: {
            dest: './',
            sprite: 'sprite.svg'
          }
        }
      }))
      .pipe(gulp.dest(`${config.assets}/core/images/facelift`));
  }
}
