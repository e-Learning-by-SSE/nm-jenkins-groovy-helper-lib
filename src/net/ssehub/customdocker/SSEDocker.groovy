package net.ssehub.customdocker

import org.jenkinsci.plugins.docker.workflow.Docker
import org.jenkinsci.plugins.docker.workflow.Docker.Image

class SSEDocker {
    def buildConfig
    def publishConfig
    Docker docker

    SSEDocker(Docker docker) {
        this.docker = docker
    }

    def build(Closure buildClosure) {
        def builconfig = new BuildConfig()
        Delegates.call(buildConfig, buildClosure)
    }

    def publish(Closure publishClosure) {
        publishConfig = new PublishConfig()
        Delegates.call(publishConfig, publishClosure)
    }

    def execute() {
        def image = null
        if (buildConfig != null) {
            image = builconfig.build()
        }
        if (publishConfig != null) {
            publishConfig.publish(image)
        }
    }

    class BuildConfig {
        String target
        String dockerfile = '.'

        Image build() {
            return docker.build(target, dockerfile)
        }
    }

    class PublishConfig {
        String imageName
        String additionalTags

        String publishConfig(Image image = null) {
            if (image == null) {
                image = docker.image(imageName)
            }
            if (!image.exists()) {
                error("Image ${target} must be build in order to publish it")
            }
            docker.withRegistry('https://ghcr.io', 'github-ssejenkins') {
                    image.push() // target contains tag - push this too
                    config.additionalTags.each{ tag -> 
                    image.push("${tag}")
                }
            }
        }
    }
}