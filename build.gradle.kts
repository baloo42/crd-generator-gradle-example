import io.fabric8.crdv2.generator.CRDGenerationInfo
import io.fabric8.crdv2.generator.CRDGenerator
import io.fabric8.crd.generator.collector.CustomResourceCollector
import org.gradle.api.internal.tasks.JvmConstants
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

tasks.register("generateCrds") {
    description = "Generate CRDs from compiled custom resource classes"
    group = "crd"

    val sourceSet = project.sourceSets["main"]

    val compileClasspathElements = sourceSet.compileClasspath.map { e -> e.absolutePath }

    val outputClassesDirs = sourceSet.output.classesDirs
    val outputClasspathElements = outputClassesDirs.map { d -> d.absolutePath }

    val classpathElements = listOf(outputClasspathElements, compileClasspathElements).flatten()
    val filesToScan = listOf(outputClassesDirs).flatten()
    val outputDir = sourceSet.output.resourcesDir

    doLast {
        Files.createDirectories(outputDir!!.toPath())

        val collector = CustomResourceCollector()
            .withParentClassLoader(Thread.currentThread().contextClassLoader)
            .withClasspathElements(classpathElements)
            .withFilesToScan(filesToScan)

        val crdGenerator = CRDGenerator()
            .customResourceClasses(collector.findCustomResourceClasses())
            .inOutputDir(outputDir)

        val crdGenerationInfo: CRDGenerationInfo = crdGenerator.detailedGenerate()

        crdGenerationInfo.crdDetailsPerNameAndVersion.forEach { (crdName, versionToInfo) ->
            println("Generated CRD $crdName:")
            versionToInfo.forEach { (version, info) -> println(" $version -> ${info.filePath}") }
        }
    }
}

tasks.named(JvmConstants.CLASSES_TASK_NAME) {
    finalizedBy("generateCrds")
}
