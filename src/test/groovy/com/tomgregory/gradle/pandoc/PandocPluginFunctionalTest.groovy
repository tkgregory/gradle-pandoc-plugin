package com.tomgregory.gradle.pandoc

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PandocPluginFunctionalTest extends Specification {
    @TempDir
    File testProjectDir
    File markdown

    def setup() {
        markdown = new File(testProjectDir, 'document.md')
        markdown << """
            # Important heading
            
            Some paragraph
        """
    }

    def "builds EPUB with default Docker image"() {
        given:
        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.pandoc'
            }

            pandoc {
                pandocDirectory = project.projectDir
                
                epub {
                    arguments = ['document.md']
                }
            }
        """
        when:
        def result = runTask('generateEpub')

        then:
        result.task(":generateEpub").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.epub') }
                .size() == 1
    }

    def "builds PDF with default Docker image"() {
        given:
        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.pandoc'
            }

            pandoc {
                pandocDirectory = project.projectDir
                
                pdf {
                    arguments = ['document.md']
                }
            }
        """
        when:
        def result = runTask('generatePdf')

        then:
        result.task(":generatePdf").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.pdf') }
                .size() == 1
    }

    def "builds PDF with custom Docker image"() {
        given:
        File customLatexStyle = new File(testProjectDir, 'title.sty')
        customLatexStyle << """
            \\usepackage{titlesec}
            
            \\definecolor{bluey}{HTML}{3c78d8}
            \\titleformat*{\\section}{\\Large\\bfseries\\sffamily\\color{bluey}}
        """

        File customDockerfile = new File(testProjectDir, 'Dockerfile')
        customDockerfile << """
            FROM pandoc/latex:latest
            RUN tlmgr install titlesec
        """

        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.pandoc'
            }
            
            pandoc {
                pandocDirectory = project.projectDir
                customDockerfile = file('Dockerfile')
                
                pdf {
                    arguments = ['document.md', '-H', 'title.sty']
                }
            }
        """

        when:
        def result = runTask('generatePdf')

        then:
        result.task(":generatePdf").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.pdf') }
                .size() == 1
    }

    private BuildResult runTask(String taskName) {
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('--stacktrace', taskName)
                .forwardOutput()
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }
}