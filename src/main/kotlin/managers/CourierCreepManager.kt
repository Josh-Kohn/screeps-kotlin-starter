package managers

import screeps.api.Creep

class CourierCreepManager(private val creeps:List<Creep>): EnergyLocationManager, CreepStateManager() {

    //If courier is full on energy
        //Check spawn and extensions if they need energy
            //if so deposit there
            //if not, check construction sites exist
                //if so, drop energy near sites
                //if not deposit at central location
    //if courier is not full on energy
        //check dedicated location
        //if exists, withdraw energy
            //if not, get dedicated location
}

//SCANNING A DEDICATED CONTAINER
    //Find all the sources in the room
    //Scan in a 5x5 grid for a container with the source as the center square
    //Store the container IDs found in a list or an array
    //Pick a container ID
    //Check against the room's memory and see if a courier is already assigned to that container
