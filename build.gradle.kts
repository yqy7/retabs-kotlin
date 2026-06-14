plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "io.github.yqy7.retabs"
version = "0.2.0"

kotlin {
    js {
        browser({
            webpackTask {
                mainOutputFileName = "main.js"
            }
        })
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
        }
        jsMain.dependencies {
            val composeVersion = "1.10.3"
            implementation("org.jetbrains.compose.html:html-core:$composeVersion")
            implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
        }
    }
}

// 将打包好的代码与静态资源组装
val prepareExtensionDistribution = tasks.register("prepareExtensionDistribution") {
    // 确保在 Webpack 生产环境打包完成后执行
    dependsOn("jsBrowserProductionWebpack")

    val compileDistributionDir = layout.buildDirectory.dir("dist/js/productionExecutable")
    val outputDir = layout.buildDirectory.dir("chrome-extension-unpacked")

    inputs.dir(compileDistributionDir)
    outputs.dir(outputDir)

    doLast {
        copy {
            from(compileDistributionDir)
            into(outputDir)
        }
    }
}

// 将组装好的目录压缩为上传商店所需的 .zip 文件
tasks.register<Zip>("packageExtension") {
    dependsOn(prepareExtensionDistribution)

    from(layout.buildDirectory.dir("chrome-extension-unpacked"))
    // 输出文件名形如：my-extension-v1.0.0.zip
    archiveFileName.set("${project.name}-v${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("outputs/extension"))

    doLast {
        logger.lifecycle("Extension zipped successfully at: ${destinationDirectory.get().asFile}/${archiveFileName.get()}")
    }
}
