import job.Jobtype
import memory.job
import screeps.api.Game
import screeps.api.get
import screeps.api.structures.Structure
import screeps.api.structures.StructureSpawn
import screeps.api.*
import screeps.api.structures.SpawnOptions
import screeps.utils.unsafe.jsObject

/**
 * Entry point
 * is called by screeps
 *
 * must not be removed by DCE
 */
@Suppress("unused")
fun loop() {
    val workerNumber = increaseWorkerNameNumber()
    val georgeSpawn: StructureSpawn = Game.spawns["George"]!!
    val workParameters: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)
    val spawnJobDeclaration = object {val memory = jsObject<CreepMemory> {this.job = Jobtype.IDLE.name }} as SpawnOptions
    val spawnCreepResult = georgeSpawn.spawnCreep(workParameters,workerNumber, spawnJobDeclaration)
    if(spawnCreepResult == OK) {
        console.log("$georgeSpawn is creating $workerNumber with $workParameters in room ${georgeSpawn.pos.roomName}")
    }
    val findIdleCreeps = findAllIdleCreeps()
}

/**
 * Checks to see if worker is null and increases worker number
 */
fun increaseWorkerNameNumber(): String {
    var workerNumber = 1
    while (true) {
        val workerName = "Worker $workerNumber"
        val nameChecker: Creep? = Game.creeps[workerName]
        if (nameChecker == null) {
            return workerName
        }
        else{
            workerNumber = workerNumber+1
        }
    }
}

fun findAllIdleCreeps(): MutableList<Creep> {
    val idleCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == Jobtype.IDLE.name){
            idleCreeps.add(creep)
        }
    }
    return idleCreeps
}





