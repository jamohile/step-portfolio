# Personal Portfolio

## Development

### Styles
All styles should be written using SASS (SCSS variant)
in .../webapp/styles/sass. Each 'feature' should get its own scss file.
These individual files should be imported into styles/sass/style.scss using @include directives.

For now, these styles should be manually compiled using `node-sass` from npm.
In the future, this should be added to the maven pipeline.

#### Build Command
`node-sass styles/sass/style.scss styles/css/style.css`