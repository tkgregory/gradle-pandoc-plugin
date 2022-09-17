This plugin makes it easy to generate various document formats from markdown, using Pandoc.

All the document generation is done within a Docker image.

## Pre-requisites

You only need a local Docker installation. Pandoc is *not* required.

## Applying the plugin

```gradle
apply plugin: PandocPlugin
```

## Configuring the plugin

A `pandoc` extension is available, in which you can configure the following properties.

| Property name        | Description                                                                                                                       | Default                         |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| `pandocDirectory`    | The directory made available to Pandoc. This must contain any files required to generated your documents, such as `.md` markdown. | Project directory               |
| `customDockerfile`   | To use a custom Pandoc Docker image, set this to the *Dockerfile*.                                                                | Uses `pandoc/latex:latest` image |

Note that the above properties apply to all output document formats.

To configure the arguments passed to Pandoc to generate a specific document format,
use this syntax:

```groovy
pandoc {
    your-document-format {
        arguments = ['arguments', 'go', '--here']
    }
}
```

Currently, the supported document formats are:
* epub
* pdf

### Example full configuration
```groovy
pandoc {
    pandocDirectory = project.buildDir
    customDockerfileDirectory = layout.buildDirectory.dir('docker')
    
    epub {
        arguments = ['chapter1.md', 'chapter2.md', 'chapter3.md']
    }
    
    pdf {
        arguments = ['chapter1.md', 'chapter2.md', 'chapter3.md']
    }
}
```

## Tasks

Once applied and configured, the plugin registers one task per document format
with name `generate<document-format>`.

e.g. `generateEpub`, `generatePdf`

These tasks are also all added to the `assemble` task as a task dependency, if present.

## Running
Run any of the above tasks. e.g.

`./gradlew generateEpub`

Once successfully completed, you'll find a file in the *build* directory of the
format `<project-name>.<document-format>` e.g. `gradle-build-bible.epub`.