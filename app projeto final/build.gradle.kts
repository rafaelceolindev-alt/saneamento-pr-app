// Arquivo: build.gradle.kts (RAIZ do projeto)

plugins {
    // não declare plugins aqui
    // os plugins pertencem ao módulo "app"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
