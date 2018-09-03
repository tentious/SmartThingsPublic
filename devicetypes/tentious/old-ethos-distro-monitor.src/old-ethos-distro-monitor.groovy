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
      input("panel_name", "text", title: "Custom Panel Name")
      input("rig_id", "text", title: "Rig ID")
    }
    
    section("Particle"){
      input("particle_device_id", "text", title: "Device ID")
      input("particle_access_token", "text", title: "Token ID")
    }
    
    section("Rigs"){
      input("rig1_id", "text", title: "Rig 1 ID")
      input("rig1_relay_id", "text", title: "Rig 1 Relay ID")
      input("rig2_id", "text", title: "Rig 2 ID")
      input("rig2_relay_id", "text", title: "Rig 2 Relay ID")
      input("rig3_id", "text", title: "Rig 3 ID")
      input("rig3_relay_id", "text", title: "Rig 3 Relay ID")
      input("rig4_id", "text", title: "Rig 4 ID")
      input("rig4_relay_id", "text", title: "Rig 4 Relay ID")
    }
    
      
    
}
metadata {
	definition (name: "OLD-EthOS Distro Monitor", namespace: "tentious", author: "Nic Bumgart") {
    capability "Refresh"
	capability "Polling"

    attribute "rig1_power_status","STRING"
    attribute "rig2_power_status","STRING"
    attribute "rig3_power_status","STRING"
    

    
    attribute "hashrate", "NUMBER"
    attribute "capacity", "NUMBER"

    command "rig1_power_Off"
    command "rig1_power_On"
    command "rig2_power_Off"
    command "rig2_power_On"
    command "rig3_power_Off"
    command "rig3_power_On"

}

	simulator {
		// TODO
	}

	tiles {
            valueTile("hashrate", "device.hashrate", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
   	         state("hashrate", label: '${currentValue}\nMH/s', unit:"MH/s", backgroundColors: [
                    [value: 0, color: "#bc2323"],
                    [value: 25, color: "#d04e00"],
                    [value: 50, color: "#f1d801"],
                    [value: 100, color: "#90d2a7"],
		            [value: 150, color: "#44b621"],
                    [value: 250, color: "#1e9cbb"],
                    [value: 300, color: "#153591"],
    	            ]
            	  )
        	}    
            
            valueTile("capacity", "device.capacity", width: 2, height: 2, decoration: "flat", canChangeIcon: true) {
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

			standardTile("rig1_power_status", "device.rig1_power_status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
                  state "on", label: 'Rig 1', action: "rig1_power_Off", icon: "st.switches.switch.on", backgroundColor: "#44b621", nextState: "off"
                  state "off", label: 'Rig 1', action: "rig1_power_On", icon: "st.switches.switch.off", backgroundColor: "#bc2323", nextState: "on"
            }
            
            standardTile("rig2_power_status", "device.rig2_power_status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
                  state "on", label: 'Rig 2', action: "rig2_power_Off", icon: "st.switches.switch.on", backgroundColor: "#44b621", nextState: "off"
                  state "off", label: 'Rig 2', action: "rig2_power_On", icon: "st.switches.switch.off", backgroundColor: "#bc2323", nextState: "on"
            }
            
			standardTile("rig3_power_status", "device.rig3_power_status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
                  state "on", label: 'Rig 3', action: "rig3_power_Off", icon: "st.switches.switch.on", backgroundColor: "#44b621"
                  state "off", label: 'Rig 3', action: "rig3_power_On", icon: "st.switches.switch.off", backgroundColor: "#bc2323"
            }
            
            standardTile("refresh", "device.hashrate", inactiveLabel: false, decoration: "flat") {
                state "default", action:"polling.poll", icon:"st.secondary.refresh"
            }

        
        main "hashrate"
        details(["hashrate","capacity","refresh","rig1_power_status","rig2_power_status","rig3_power_status"])

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

def rig1_power_Off() {
	if (rig1_id){

		log.debug "Executing rig1_power_Off for device: ${particle_device_id} and token: ${particle_access_token}."
        
        def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            	sendEvent(name: "rig1_power_status", value: "off")
            }
            else{
            	log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "1"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        }
        catch (e) {
            log.error "Something went wrong sending request: $e"
        }

	}
}

def rig1_power_On() {
	log.debug "Executing rig1_power_On for device: ${particle_device_id} and token: ${particle_access_token}."
    if (rig1_id){
		       
        def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "rig1_power_status", value: "on")
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "1"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
}

def rig2_power_Off() {
	if (rig2_id){

		log.debug "Executing rig2_power_Off for device: ${particle_device_id} and token: ${particle_access_token}."
        
        def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "rig2_power_status", value: "off")
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "2"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
}

def rig2_power_On() {
	log.debug "Executing rig2_power_On for device: ${particle_device_id} and token: ${particle_access_token}."
    if (rig2_id){
		       
        def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "rig2_power_status", value: "on")
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "2"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
}

def rig3_power_Off() {
	if (rig3_id){
		log.debug "Executing rig3_power_Off for device: ${particle_device_id} and token: ${particle_access_token}."

			def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "rig3_power_status", value: "off")
            state.rig3_power_nextchange = now() + 600000
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "3"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
    else {
    	log.debug "Try again at ${state.rig3_power_nextchange} it's currently ${now()}."
    }
}

def rig3_power_On() {
	
    if (rig3_id){
		log.debug "Executing rig3_power_On for device: ${particle_device_id} and token: ${particle_access_token}."       

			def submit_req = { response ->
        	log.debug "Starting Response."
            log.debug "Response from Particle: '$response.data'"
            log.debug "Connection: '$response.data.connected'"
            if (response.data.connected == true) {
            sendEvent(name: "rig3_power_status", value: "on")
            state.rig3_power_nextchange = now() + 600000
            log.debug "Set timestamp of rig 3 next change to ${state.rig3_power_nextchange}"
            }
            else{
            log.debug "Connection to Particle failed."
            }
        }
        
        def params = [
        	uri: "https://api.particle.io/v1/devices/${particle_device_id}/PowerToggle",
            body: [access_token: particle_access_token, params: "3"],  
            success: submit_req
        ]

        try {
            httpPost(params)
        } catch (e) {
            log.error "Something went wrongsending request: $e"
        }

	}
    else {
    	log.debug "Try again at ${state.rig3_power_nextchange} it's currently ${now()}."
    }
}

def rigrefresh() {  
  
  log.debug "Executing get 'hashrate'"
  try {   
      httpGet(uri: "http://${settings.panel_name}.ethosdistro.com/?json=yes", contentType: 'application/json') {resp ->
            if (resp.data) {
                def hashrate = resp.data.total_hash
                def capacity = resp.data.capacity
                def rig1_condition = resp.data.rigs."${rig1_id}".condition
                def rig2_condition = resp.data.rigs."${rig2_id}".condition
                def rig3_condition = resp.data.rigs."${rig3_id}".condition
                log.debug "Total Hashrate is ${hashrate}"
                log.debug "Rig1 Condition: ${rig1_condition}"
                log.debug "Rig2 Condition: ${rig2_condition}"
                log.debug "Rig3 Condition: ${rig3_condition}"
                log.debug "Active GPU Capacity: ${capacity}%"
                log.debug "Next Change for Rig3: ${state.rig3_power_nextchange} Current Epoch is ${now()}"
                sendEvent(name: 'hashrate', value: (hashrate))
                sendEvent(name: 'capacity', value: (capacity))


            if (rig1_condition == "unreachable") {
                log.debug "Rig 1 is ${rig1_condition}"
                sendEvent(name: "rig1_power_status", value: "off")
            }

            if (rig2_condition == "unreachable") {
                log.debug "Rig 2 is ${rig2_condition}"
                sendEvent(name: "rig2_power_status", value: "off")
            }

            if (rig3_condition == "unreachable" && state.rig3_power_nextchange < now()) {
                log.debug "Rig 3 is ${rig3_condition}"
                sendEvent(name: "rig3_power_status", value: "off")
            }
            if (rig3_condition == "mining" && state.rig3_power_nextchange < now()) {
                log.debug "Rig 3 is ${rig3_condition}"
                sendEvent(name: "rig3_power_status", value: "on")
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