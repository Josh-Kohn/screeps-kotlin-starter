package harvest
import screeps.api.*
import screeps.api.structures.StructureContainer

/**
 * Finds sources for creeps and makes creeps harvest energy and perform an action
 */

class EnergyManager(val creep: Creep) {

    /**
     * Function for Finding Sources
     */

    fun findSources() {
        for (playerOwnedRoom in Game.rooms.values){
            val roomController = playerOwnedRoom.controller
            if (roomController != null){
               if (roomController.my == true) {
                    val sourcesInRoom: Array<Source> = playerOwnedRoom.find(FIND_SOURCES)

               }
            }
        }
    }

    /**
     * Function for Harvesting Energy
     */
    fun collectEnergy() {
        val source:
creep.harvest()
    }


    /**
     * Function for storing energy
     */

}



