[![Gradle Pandoc Plugin](https://github.com/tkgregory/gradle-pandoc-plugin/actions/workflows/gradle.yml/badge.svg)](https://github.com/tkgregory/gradle-pandoc-plugin/actions/workflows/gradle.yml)

This plugin makes it easy to generate various document formats from markdown, using Pandoc.

Why use this plugin?

* **no local Pandoc installation required:** document generation happens within Docker
* **customisable document output format:** pass whatever arguments you like to Pandoc

## Pre-requisites

You only need a local Docker installation. Pandoc is *not* required.

## Applying the plugin

```gradle
apply plugin: PandocPlugin
```

## Configuring the plugin

A `pandoc` extension is available, in which you can configure the following properties.

| Property name        | Description                                                                                                                      | Default                         |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| `pandocDirectory`    | The directory made available to Pandoc. This must contain any files required to generate your documents, such as `.md` markdown. | Project directory               |
| `customDockerfile`   | To use a custom Pandoc Docker image, set this to the *Dockerfile*.                                                               | Uses `pandoc/latex:latest` image |

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

These tasks are also all added to the `assemble` task as a task dependency, assuming that task is available.

## Running
To generate a specific document format, run the corresponding generate task e.g. 

`./gradlew generateEpub`

On completion, in the pandoc directory a file is generated with name `<project-name>.<document-format>` e.g. `gradle-build-bible.epub`.