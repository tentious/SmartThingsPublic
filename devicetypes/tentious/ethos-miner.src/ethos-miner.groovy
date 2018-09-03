/**
 *  EthOS Distro Monitor
 *
 *  Copyright 2017 Nic Bumgart.
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

preferences {
    section("EthOS"){
      input("panel_name", "text", title: "EthOS Panel Name")
      input("rig_id", "text", title: "EthOS Rig ID")
    }

    section("Particle"){
      input("particle_device_id", "text", title: "Particle Device ID")
      input("particle_access_token", "text", title: "Particle Token ID")
      input("rig_relay_id", "text", title: "Rig's Relay Slot #")
    }

}

metadata {
  definition (name: "EthOS Miner", namespace: "tentious", author: "Nic Bumgart") {
    capability "Refresh"
    capability "Polling"

    attribute "power_status","STRING"

    attribute "hashrate", "NUMBER"
    attribute "capacity", "NUMBER"

    command "power_Off"
    command "power_On"

  }

  simulator {
    // TODO
  }

  tiles {
    valueTile("hashrate", "device.hashrate", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
      state("hashrate", label: '${currentValue}\nMH/s', unit:"MH/s", backgroundColors: [
        [value: 0, color: "#bc2323"],
        [value: 20, color: "#d04e00"],
        [value: 40, color: "#f1d801"],
        [value: 80, color: "#90d2a7"],
        [value: 100, color: "#44b621"],
        [value: 120, color: "#1e9cbb"],
        [value: 130, color: "#153591"],
      ]
      )
    }

    valueTile("capacity", "device.capacity", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
      state("capacity", label: 'GPUs:\n${currentValue}%', unit:"MH/s", backgroundColors: [
        [value: 0, color: "#bc2323"],
        [value: 14, color: "#d04e00"],
        [value: 28, color: "#f1d801"],
        [value: 56, color: "#1e9cbb"],
        [value: 71, color: "#153591"],
        [value: 85, color: "#90d2a7"],
        [value: 100, color: "#44b621"],
      ]
  	  )
  	}

    standardTile("power_status", "device.power_status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
      state "on", label: 'On', action: "power_Off", icon: "st.switches.switch.on", backgroundColor: "#44b621", nextState: "off"
      state "off", label: 'Off', action: "power_On", icon: "st.switches.switch.off", backgroundColor: "#bc2323", nextState: "on"
    }

    standardTile("refresh", "device.hashrate", inactiveLabel: false, decoration: "flat") {
      state "default", action:"polling.poll", icon:"st.secondary.refresh"
    }

    main "power_status"
    details(["hashrate","capacity","refresh","power_status"])
  }
}


def poll() {
	log.debug "Polling Miners"
	rigrefresh()
}

def refresh() {
  log.debug "Executing Refresh"
  rigrefresh()
}

def power_Off() {
  if (rig_id){
	   log.debug "Executing power_Off for device: ${particle_device_id} and token: ${particle_access_token}."

     def submit_req = { response ->
       log.debug "Starting Response."
       log.debug "Response from Particle: '$response.data'"
       log.debug "Connection: '$response.data.connected'"
       if (response.data.connected == true) {
         sendEvent(name: "power_status", value: "off")
         state.power_nextchange = now() + 1200000
         log.debug "Set timestamp of rig's next power change to ${state.power_nextchange}"
       }
       else{
         log.debug "Connection to Particle failed."
       }
     }

     def params = [
	      uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
        body: [access_token: particle_access_token, params: rig_relay_id],
        success: submit_req
      ]

     try {
       httpPost(params)
     }catch (e) {
       log.error "Something went wrong sending request: $e"
     }
	}
    else {
    	log.debug "Try again at ${state.power_nextchange} it's currently ${now()}."
    }
}

def power_On() {

    if (rig_id){
		log.debug "Executing power_On for device: ${particle_device_id} and token: ${particle_access_token}."

			def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "power_status", value: "on")
            state.power_nextchange = now() + 1200000
            log.debug "Set timestamp of rig's next power change to ${state.power_nextchange}"
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }

        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: rig_relay_id],
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
    else {
    	log.debug "Try again at ${state.power_nextchange} it's currently ${now()}."
    }
}

def rigrefresh() {

  log.debug "Executing get 'hashrate'"
  try {
    httpGet(uri: "http://${settings.panel_name}.ethosdistro.com/?json=yes", contentType: 'application/json') {resp ->
      if (resp.data) {
        def hashrate = resp.data.rigs."${rig_id}".hash
        def capacity = resp.data.capacity
        def rig_condition = resp.data.rigs."${rig_id}".condition
        log.debug "Rig Hashrate is ${hashrate}"
        log.debug "Rig Condition: ${rig_condition}"
        log.debug "Active GPU Capacity: ${capacity}%"
        log.debug "Next Change for Rig: ${state.power_nextchange} Current Epoch is ${now()}"
        sendEvent(name: 'hashrate', value: (hashrate))
        sendEvent(name: 'capacity', value: (capacity))

        if (rig_condition == "unreachable" && state.power_nextchange < now()) {
            log.debug "Setting power to OFF"
            sendEvent(name: "power_status", value: "off")
        }
        if ((rig_condition == "mining" || rig_condition == "just_booted") && state.power_nextchange < now()) {
            log.debug "Setting power to ON"
            sendEvent(name: "power_status", value: "on")
        }

      }
      if(resp.status == 200) {
              log.debug "EthOS results returned"
      }
      else {
        log.error "Error polling API. HTTP status: ${resp.status}"
      }
    }
  }
  catch (e) {
    log.error "Something went wrongsending request: $e"
  }
}
