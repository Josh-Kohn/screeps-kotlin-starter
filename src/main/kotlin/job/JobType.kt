package job

enum class JobType(val priority: Int) {
    HARVESTER(0),
    COURIER(1),
    UPGRADER(2),
    BUILDER(3),
    JANITOR(4),
    REPAIRMAN(5),
    TANK(6),
    SCOUT(7),
    HEALER(8),
    RANGER(9),
    CAPTAIN(10),
    SENTINEL(11),
    IDLE(99),
    NONE(100)
}