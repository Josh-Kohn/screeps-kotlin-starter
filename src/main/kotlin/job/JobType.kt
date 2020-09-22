package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    COURIER(1),
    UPGRADER(2),
    BUILDER(3),
    JANITOR(4),
    REPAIRMAN(5),
    FIGHTER(6),
    IDLE(99),
    NONE(100)
}