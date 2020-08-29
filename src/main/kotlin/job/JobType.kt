package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    UPGRADER(1),
    BUILDER(2),
    REPAIRMAN(3),
    FIGHTER(4),
    IDLE(99),
    NONE(100)
}