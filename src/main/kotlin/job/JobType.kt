package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    FERRYMAN(1),
    UPGRADER(2),
    BUILDER(3),
    REPAIRMAN(4),
    FIGHTER(5),
    IDLE(99),
    NONE(100)
}