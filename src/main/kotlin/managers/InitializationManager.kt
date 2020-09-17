package managers
import memory.initialized
import memory.sources
import objects.SourceDataObject
import screeps.api.*
import screeps.api.structures.StructureContainer

class InitializationManager(val rooms: List<Room> ) {
    init {
        for (room in rooms){
            if (!room.memory.initialized){
                val getAllSources = room.find(FIND_SOURCES)
                for (singleSource in getAllSources) {
                    val lookResult = room.lookAtAreaAsArray(singleSource.pos.y-1, singleSource.pos.x-1, singleSource.pos.y+1, singleSource.pos.x+1)
                    var maxEmptySpaces = lookResult.filter { space -> space.type == LOOK_TERRAIN && space.terrain != TERRAIN_WALL }.size
                    if (maxEmptySpaces == 1) {
                        maxEmptySpaces += 1
                    }
                    val sourceDataMemory = SourceDataObject(singleSource.id,0, maxEmptySpaces, containerID = "")
                    room.memory.sources = room.memory.sources + sourceDataMemory
                }
                room.memory.initialized = true
            }
        }
    }

    fun sourceContainerAssociation() {
        for (room in rooms){
            val sources = room.memory.sources
            for (source in sources){
                if(source.containerID.isBlank()) {
                    val sourceID = source.sourceID
                    val sourceObject = Game.getObjectById<Source>(sourceID)!!

                    val lookResult = room.lookAtAreaAsArray(
                            sourceObject.pos.y - 2,
                            sourceObject.pos.x - 2,
                            sourceObject.pos.y + 2,
                            sourceObject.pos.x + 2)
                    val containersLookResult = lookResult.filter { it.type == LOOK_STRUCTURES && it.structure!!.structureType == STRUCTURE_CONTAINER }

                    val findContainers = containersLookResult.map { (it.structure as StructureContainer) }
                    if (findContainers.isNotEmpty()) {
                        for (container in findContainers) {
                            val sourceIDCheck = sources.any { container.id == it.containerID }
                            if (!sourceIDCheck) {
                                source.containerID = container.id
                                break
                            }
                        }
                    }
                } else {
                    if (Game.getObjectById<StructureContainer>(source.containerID) == null) {
                        source.containerID = ""
                    }
                }
            }
        }
    }
}