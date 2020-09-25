package memory

import job.JobType
import objects.ConstructionDataObject
import objects.RepairDataObject
import objects.SourceDataObject
import screeps.api.*
import screeps.utils.memory.memory

/* Add the variables that you want to store to the persistent memory for each object type.
* They can be accessed by using the .memory attribute of any of the instances of that class
* i.e. creep.memory.building = true */

/* Arbitrary Memory */

var GlobalMemory.constructionDataObjects: Array<ConstructionDataObject> by memory { arrayOf<ConstructionDataObject>() }
var GlobalMemory.repairDataObjects: Array<RepairDataObject> by memory { arrayOf<RepairDataObject>() }

/* Creep.memory */
var CreepMemory.job: String by memory {JobType.IDLE.name}
var CreepMemory.roomSpawnLocation: String by memory {""}
var CreepMemory.sourceIDAssignment: String by memory {""}
var CreepMemory.fullOfEnergy: Boolean by memory { false }
var CreepMemory.withdrawID: String by memory {""}
var CreepMemory.depositID: String by memory {""}
var CreepMemory.constructionSiteID: String by memory {""}
var CreepMemory.droppedID: String by memory {""}
var CreepMemory.repairID: String by memory {""}

/* Rest of the persistent memory structures.
* These set an unused test variable to 0. This is done to illustrate the how to add variables to
* the memory. Change or remove it at your convenience.*/

/* Power creep is a late game hero unit that is spawned from a Power Spawn
   see https://docs.screeps.com/power.html for more details.
   This set sets up the memory for the PowerCreep.memory class.
 */
var PowerCreepMemory.test : Int by memory { 0 }

/* flag.memory */
var FlagMemory.test : Int by memory { 0 }

/* room.memory */
var RoomMemory.sources : Array<SourceDataObject> by memory { arrayOf<SourceDataObject>() }
var RoomMemory.initialized : Boolean by memory { false }

/* spawn.memory */
var SpawnMemory.test : Int by memory { 0 }
