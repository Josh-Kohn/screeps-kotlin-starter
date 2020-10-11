package managers.military

import objects.MilitaryDataClass
import screeps.api.Creep
import screeps.api.Game
import screeps.api.get

class MilitaryCreepManager(val militaryCreeps: Array<MilitaryDataClass>, roomName: String?) {

    fun militarySpawn(){
        val conquestFlag = Game.flags["Rally Point"]
        if (conquestFlag != null){

        }
    }
}