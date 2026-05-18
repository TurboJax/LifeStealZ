rootProject.name = "LifeStealZ"

sequenceOf(
    // "paper",
    "common",
    "bukkit"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}