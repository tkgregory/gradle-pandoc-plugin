[![Gradle Pandoc Plugin](https://github.com/tkgregory/gradle-pandoc-plugin/actions/workflows/gradle.yml/badge.svg)](https://github.com/tkgregory/gradle-pandoc-plugin/actions/workflows/gradle.yml)

# Gradle Pandoc Plugin

This plugin makes it easy to generate various document formats from markdown, using Pandoc.

Why use this plugin?

* **no local Pandoc installation required:** document generation happens within Docker
* **customisable document output format:** pass whatever arguments you like to Pandoc
* **customisable Pandoc environment:** provide your own Pandoc *Dockerfile* or use the default

## Pre-requisites

You just need a local Docker installation. Pandoc is *not* required.

## Applying the plugin

```gradle
plugins {
    id 'com.tomgregory.pandoc' version '<latest-version>'
}
```

## Configuring the plugin

Configure the following properties within the `pandoc` extension.

| Property name        | Description                                                                                                                            | Default                         |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| `pandocDirectory`    | The directory mounted on the Pandoc container. This should contain all files required to generate documents e.g. `.md` markdown files. | Project directory               |
| `customDockerfile`   | Set this to your own *Dockerfile* to use a custom Pandoc Docker image.                                                               | Uses `pandoc/latex:latest` image |

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

The currently supported document formats are:
* epub
* pdf

### Example full configuration
```groovy
pandoc {
    pandocDirectory = project.buildDir
    customDockerfile = layout.projectDirectory.file('docker/Dockerfile')
    
    epub {
        arguments = ['chapter1.md', 'chapter2.md', 'chapter3.md']
    }
    
    pdf {
        arguments = ['chapter1.md', 'chapter2.md', 'chapter3.md']
    }
}
```

## Tasks

Once applied and configured, the plugin registers one main task per document format
with name `generate<document-format>`.

e.g. `generateEpub`, `generatePdf`

These tasks are also all added to the `assemble` task as a task dependency, assuming that task is available.

## Running
To generate a specific document format, run the corresponding generate task e.g. 

`./gradlew generateEpub`

Or to generate all document formats at once:

`./gradlew assemble`

## Output

On completion, a file is generated within the `pandocDirectory` with name `<project-name>.<document-format>` 
e.g. `gradle-build-bible.epub`.

## Releasing

Run the following step manually, passing `incrementMajor` or `incrementMinor`.

`./gradlew release -Prelease.versionIncrementer=<increment-option>`

This will push a new tag, then the CI build will publish the new plugin version.

You may need to provide your GitHub token by passing 
`-Prelease.customUsername=<your-username> -Prelease.customPassword=<your-token>`. 