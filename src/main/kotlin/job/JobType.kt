package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    BUILDER(1),
    FIGHTER(2),
    IDLE(99),
    NONE(100)
}