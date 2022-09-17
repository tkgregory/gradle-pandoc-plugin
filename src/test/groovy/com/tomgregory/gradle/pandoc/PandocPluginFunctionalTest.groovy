package com.tomgregory.gradle.pandoc


import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.FAILED

class PandocPluginFunctionalTest extends Specification {
    @TempDir
    File testProjectDir
    File markdown
    File customLatexStyle

    def setup() {
        markdown = new File(testProjectDir, 'document.md')
        markdown << """
            # Important heading
            
            Some paragraph.
            
            ## Subheading
            
            More cool stuff.
        """

        customLatexStyle = new File(testProjectDir, 'title.sty')
        customLatexStyle << """
            \\usepackage{titlesec}
            
            \\definecolor{bluey}{HTML}{3c78d8}
            \\titleformat*{\\section}{\\Large\\bfseries\\sffamily\\color{bluey}}
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
                epub {
                    arguments = ['document.md']
                }
            }
        """
        when:
        def result = runnerForTask('generateEpub').build()

        then:
        result.task(":generateEpub").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.epub') }
                .size() == 1
    }

    def "builds simple PDF with default Docker image"() {
        given:
        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.pandoc'
            }

            pandoc {
                pdf {
                    arguments = ['document.md']
                }
            }
        """
        when:
        def result = runnerForTask('generatePdf').build()

        then:
        result.task(":generatePdf").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.pdf') }
                .size() == 1
    }

    def "builds complex PDF with custom Docker image"() {
        given:
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
                customDockerfile = file('Dockerfile')
                
                pdf {
                    arguments = ['document.md', '-H', 'title.sty']
                }
            }
        """

        when:
        def result = runnerForTask('generatePdf').build()

        then:
        result.task(":generatePdf").outcome == SUCCESS

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.pdf') }
                .size() == 1
    }

    def "fails to build complex PDF with default Docker image"() {
        given:
        File buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.tomgregory.pandoc'
            }
            
            pandoc {
                pdf {
                    arguments = ['document.md', '-H', 'title.sty']
                }
            }
        """

        when:
        def result = runnerForTask('generatePdf').buildAndFail()

        then:
        result.task(":generatePdf").outcome == FAILED

        testProjectDir.listFiles()
                .findAll { it.path.endsWith('.pdf') }
                .isEmpty()
    }

    private GradleRunner runnerForTask(String taskName) {
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('--stacktrace', taskName)
                .forwardOutput()
                .withPluginClasspath()
                .withDebug(true)
    }
}