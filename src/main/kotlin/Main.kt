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
    val workerName = increaseWorkerNameNumber()
    val workParameters: Array<BodyPartConstant> = arrayOf(WORK, MOVE, CARRY)
    val findIdleCreeps = findAllIdleCreeps()
    createAWorkerCreep(workParameters, workerName)
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

/**
 * Checks if spawners are available to spawn worker creeps.  Spawns a creep if possible.
 */
fun createAWorkerCreep(workParameter: Array<BodyPartConstant>, workerName: String) {
    val allSpawners = Game.spawns.values
    val spawnJobDeclaration = object {val memory = jsObject<CreepMemory> {this.job = Jobtype.IDLE.name }} as SpawnOptions
    for (spawner in allSpawners){
        if (spawner.spawning == null) {
              spawner.spawnCreep(workParameter, workerName, spawnJobDeclaration)
            break
        }
    }
}

/**
 * Finds idle Creeps
 */
fun findAllIdleCreeps(): MutableList<Creep> {
    val idleCreeps: MutableList<Creep> = mutableListOf()
    for(creep in Game.creeps.values){
        if(creep.memory.job == Jobtype.IDLE.name){
            idleCreeps.add(creep)
        }
    }
    return idleCreeps
}





