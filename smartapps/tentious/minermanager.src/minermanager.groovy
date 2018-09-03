/**
 *  ParticleTemp
 *
 *  Copyright 2016 Nicholas Bumgart
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "MinerManager",
    namespace: "tentious",
    author: "Nicholas Bumgart",
    description: "Automate Miners Based on Sensor Data",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true) {
}


preferences {
	page(name: "page1", title: "Select devices", nextPage: "page2", uninstall:true) {
        section("Choose a thermometer and miner to control:") {
            input "temperature", "capability.temperatureMeasurement", required: true
            input "miner", "device.ethosMiner", required: true
        }
    }
    page(name: "page2", title: "Settings", install: true, uninstall: true) {
        section {
            input "max_temp", "number", title: "Max Temperature - Turn Off Miner"
            input "min_temp", "number", title: "Min Temperature - Turn On Miner"
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(miner,"power_status",power_changed)
    subscribe(temperature,"temperature",temp_changed)
}

def power_changed(evt) {
	log.debug "Rig power changed to: ${miner.power_statusState.value}."
    log.debug "Current temp is: ${temperature.latestValue("temperature").toInteger()}."
    log.debug "Rig power is: ${miner.power_statusState.value}."
    if (temperature.currentValue("temperature") >= settings.max_temp && miner.power_statusState.value == "on"){
    log.debug "Over $settings.max_temp and on."
    }
    if (temperature.currentValue("temperature") <= settings.min_temp && miner.power_statusState.value == "off"){
    log.debug "Under $settings.min_temp and off!"
    }
    else {
        log.debug "NOT over $settings.max_temp"
    }
}

def temp_changed(evt) {
	log.debug "Temp changed to: ${temperature.latestValue("temperature").toInteger()}."
    log.debug "Rig is : ${miner.power_statusState.value}."
    if (temperature.currentValue("temperature").toInteger() >= settings.max_temp && miner.power_statusState.value == "on"){
    	log.debug "Powering off Rig."
    	 miner.power_Off()
         log.debug "Rig is now: ${miner.power_statusState.value}."
    }
    else if (temperature.latestValue("temperature").toInteger() <= settings.min_temp && miner.power_statusState.value == "off") {
    	log.debug "Powering on Rig."
    	 miner.power_On()
         log.debug "Rig is now: ${miner.power_statusState.value}."
    }
}

// TODO: implement event handlers