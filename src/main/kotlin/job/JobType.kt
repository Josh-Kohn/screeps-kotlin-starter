package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    BUILDER(1),
    REPAIRMAN(2),
    UPGRADER(3),
    FIGHTER(4),
    IDLE(99),
    NONE(100)
}