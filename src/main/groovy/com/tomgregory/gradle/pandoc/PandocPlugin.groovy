package com.tomgregory.gradle.pandoc

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerWaitContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider

class PandocPlugin implements Plugin<Project> {
    private final String DEFAULT_IMAGE = 'pandoc/latex:latest'
    def SUPPORTED_FORMATS = ['epub', 'pdf']

    @Override
    void apply(Project project) {
        def outerExtension = project.extensions.create('pandoc', PandocPluginExtension)
        SUPPORTED_FORMATS.each { format ->
            outerExtension.extensions.create(format, DocumentFormatExtension)
        }

        project.getPlugins().apply(DockerRemoteApiPlugin)

        def pandocPluginBuildDirectory = project.layout.buildDirectory.dir('.pandoc')

        TaskProvider<Copy> copyDockerfileTask = project.tasks.register('copyDockerfile', Copy) {
            from outerExtension.customDockerfile
            into pandocPluginBuildDirectory
            enabled = outerExtension.customDockerfile.isPresent()
        }

        TaskProvider<DockerBuildImage> buildImageTask = project.tasks.register('buildImage', DockerBuildImage) {
            inputDir = pandocPluginBuildDirectory
            images.add('tkgregory/latex:latest')
            enabled = outerExtension.customDockerfile.isPresent()
            dependsOn copyDockerfileTask //project.tasks.named('processResources')
        }

        TaskProvider<DockerPullImage> pullDefaultPandocImageTask = project.tasks.register('pullDefaultPandocImage', DockerPullImage) {
            image = DEFAULT_IMAGE
            enabled = !buildImageTask.get().isEnabled()
        }

        SUPPORTED_FORMATS.each { String format ->
            DocumentFormatExtension documentFormatExtension = outerExtension.extensions[format]
            registerTasksForDocumentFormat(project, buildImageTask, pullDefaultPandocImageTask, format,
                    outerExtension.pandocDirectory, documentFormatExtension.arguments)
        }
    }

    private void registerTasksForDocumentFormat(Project project, TaskProvider<DockerBuildImage> buildImageTask, TaskProvider<DockerPullImage> pullDefaultPandocImageTask, String format,
                                                DirectoryProperty pandocDirectory, ListProperty<String> arguments) {
        def taskNameExtension = format.capitalize()



        def createContainerTask = project.tasks.register("createContainer$taskNameExtension", DockerCreateContainer) {
            if (buildImageTask.get().isEnabled()) {
                dependsOn buildImageTask
                targetImageId buildImageTask.get().getImageId()
            } else {
                dependsOn pullDefaultPandocImageTask
                targetImageId DEFAULT_IMAGE
            }
            hostConfig.autoRemove = true

            hostConfig.binds = [(pandocDirectory.get().asFile.getAbsolutePath()): '/data']

            def decoratedArguments = arguments.get() + ['-o', "${project.name}.$format"]
            attachStdout.set(true)
            attachStderr.set(true)
            cmd = decoratedArguments
        }

        def generateTask = project.tasks.register("generate$taskNameExtension", DockerStartContainer) {
            group 'Document generation'

            dependsOn createContainerTask
            targetContainerId createContainerTask.get().getContainerId()
            finalizedBy project.tasks.named("wait$taskNameExtension")
        }

        project.tasks.register("wait$taskNameExtension", DockerWaitContainer) {
            dependsOn generateTask
            targetContainerId createContainerTask.get().getContainerId()
        }

        project.pluginManager.withPlugin('base') {
            project.tasks.named('assemble') {
                dependsOn generateTask
            }
        }
    }
}