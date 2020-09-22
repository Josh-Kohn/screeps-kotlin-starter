package util
import job.JobType
import memory.job
import memory.sourceIDAssignment
import objects.SourceDataObject
import screeps.api.Game
import screeps.api.WORK
import screeps.api.values

fun workPerSource(sourceDataObject: SourceDataObject): Int {
    val harvesters = Game.creeps.values.filter { it.memory.job == JobType.HARVESTER.name }
    val harvestersOnSource = harvesters.filter { it.memory.sourceIDAssignment == sourceDataObject.sourceID }
    val workFilter = harvestersOnSource.map {creep ->
        creep.body.filter { it.type == WORK }.size
    }
    //total is the accumulation of entries, work is the each individual index within the array, total+work adds as it goes along
    return workFilter.reduce { total, work -> total+work }
}

val maxWorkPerSource = 6