package managers.military

import job.JobType
import memory.job
import objects.MilitaryDataClass
import screeps.api.*

class MilitaryCreepManager(private val creeps:List<Creep>) {

    fun militaryOrders(){
        for(creep in creeps){
            val flag = Game.flags["Rally Point"]
            if (flag != null){
                creep.moveTo(flag)
            }
            when(creep.memory.job){
                JobType.TANK.name -> {
                    val hostileCreeps = creep.pos.findClosestByRange(FIND_HOSTILE_CREEPS)
                    if (hostileCreeps != null) {
                        if (creep.pos.getRangeTo(hostileCreeps.pos) < 6)
                            when (creep.attack(hostileCreeps)){
                                ERR_NOT_IN_RANGE -> {
                                    creep.moveTo(hostileCreeps.pos)
                            }
                        }
                    } else {
                        val hostileStructures = creep.pos.findClosestByRange(FIND_HOSTILE_STRUCTURES)
                        if (hostileStructures != null) {
                            if (hostileStructures.hitsMax != null) {
                                if (creep.pos.getRangeTo(hostileStructures.pos) < 6) {
                                    when (creep.attack(hostileStructures)) {
                                        ERR_NOT_IN_RANGE -> {
                                            creep.moveTo(hostileStructures.pos)

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                JobType.HEALER.name -> {
                    val damagedCreep = creep.pos.findClosestByRange(FIND_MY_CREEPS, options<FilterOption<Creep>> {
                        filter = {
                            it.hits < it.hitsMax
                        }
                    })
                    if(damagedCreep != null) {
                        when (creep.heal(damagedCreep)){
                            ERR_NOT_IN_RANGE -> {
                                creep.rangedHeal(damagedCreep)
                                creep.moveTo(damagedCreep.pos)
                            }
                        }
                    }
                }
            }
        }
    }
    //Do a for loop and then when statements for each job that specify orders
}