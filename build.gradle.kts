import io.fabric8.crdv2.generator.CRDGenerationInfo
import io.fabric8.crdv2.generator.CRDGenerator
import io.fabric8.crd.generator.collector.CustomResourceCollector
import java.nio.file.Files

plugins {
    id("java")
}

group = "io.fabric8.crd-generator.gradle"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val kubernetesClientVersion: String by project
    val junitVersion: String by project

    compileOnly("io.fabric8:kubernetes-client-api:$kubernetesClientVersion")
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        val kubernetesClientVersion: String by project

        classpath("io.fabric8:crd-generator-api-v2:$kubernetesClientVersion")
        classpath("io.fabric8:crd-generator-collector:$kubernetesClientVersion")
    }
}

tasks.test {
    useJUnitPlatform()
}

val generateCrds by tasks.registering {
    doLast {

        val sourceSet = project.sourceSets["main"]

        val classpathElements = listOf(
            sourceSet.compileClasspath.map { e -> e.absolutePath },
            sourceSet.output.classesDirs.map { d -> d.absolutePath }
        ).flatten()

        val filesToScan = listOf(project.layout.buildDirectory.get().asFile)

        val outputDir = sourceSet.output.resourcesDir

        if (outputDir != null) {
            Files.createDirectories(outputDir.toPath())
        }

        val collector = CustomResourceCollector()
            .withParentClassLoader(Thread.currentThread().contextClassLoader)
            .withClasspathElements(classpathElements)
            .withFilesToScan(filesToScan)

        val crdGenerator = CRDGenerator()
            .customResourceClasses(collector.findCustomResourceClasses())
            .inOutputDir(sourceSet.output.resourcesDir)

        val crdGenerationInfo: CRDGenerationInfo = crdGenerator.detailedGenerate()

        crdGenerationInfo.crdDetailsPerNameAndVersion.forEach { (crdName, versionToInfo) ->
            println("Generated CRD $crdName:")
            versionToInfo.forEach { (version, info) -> println(" " + version + " -> " + info.filePath) }
        }
    }
}

tasks.named("classes") {
    finalizedBy("generateCrds")
}
