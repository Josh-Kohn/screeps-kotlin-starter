package managers.creeps

import memory.fullOfEnergy
import screeps.api.Creep

open class CreepStateManager{

    fun energyManagement(creep: Creep){
        var freeEnergyCapacity = creep.store.getFreeCapacity()
        var emptyEnergyCapacity = creep.store.getUsedCapacity()
        if (freeEnergyCapacity == 0){
            creep.memory.fullOfEnergy = true
        }
        if (emptyEnergyCapacity == 0){
            creep.memory.fullOfEnergy = false
        }
    }
}