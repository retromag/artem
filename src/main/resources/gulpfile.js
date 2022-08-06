var gulp = require('gulp');
var sass = require('gulp-sass')(require('sass'));
var browserSync = require('browser-sync');
var cssnano = require('gulp-cssnano');
var autoprefixer = require('gulp-autoprefixer');
var cache = require('gulp-cache');
var del = require('del');
var uglify = require('gulp-uglify');

gulp.task('sass', function(){
    return gulp.src('static/styles/index.scss')
        .pipe(sass())
        .pipe(autoprefixer(['last 15 versions', '> 1%', 'ie 8', 'ie 7'], { cascade: true }))
        .pipe(gulp.dest('static/css'))
        .pipe(browserSync.reload({stream: true}))
});

gulp.task('browser-sync', function() {
    browserSync({
        server: {
            baseDir: '../resources',
            index: 'templates/index.html'
        },
        notify: false
    });
});

gulp.task('code', function() {
    return gulp.src('templates/*.html')
        .pipe(browserSync.reload({ stream: true }))
});

// gulp.task('scripts', function() {
//     return gulp.src('static/js/*.js')
//         .pipe(browserSync.reload({ stream: true }))
// });

gulp.task('watch', function() {
    gulp.watch('static/styles/*.scss', gulp.parallel('sass'));
    gulp.watch('templates/*.html', gulp.parallel('code'));
    // gulp.watch('static/js/*.js', gulp.parallel('scripts'));
});

gulp.task('run', gulp.parallel('sass', 'browser-sync', 'watch'));

gulp.task('clear', function () {
    return cache.clearAll();
});

gulp.task('clean', async function() {
    return del.sync('dist');
});

gulp.task('prebuild', function() {
    var buildCss = gulp.src('static/css/index.css')
        .pipe(cssnano())
        .pipe(gulp.dest('dist/css'))

    // var buildJs = gulp.src('static/js/index.js')
    //     .pipe(uglify())
    //     .pipe(gulp.dest('dist/js'))

    var buildImg = gulp.src('static/images/**/*')
        .pipe(gulp.dest('dist/img'));

    // var buildHtml = gulp.src('templates/*.html')
    //     .pipe(gulp.dest('dist'));
});

gulp.task('build', gulp.parallel('clean', 'prebuild'));