/**
 *  SparkTempMon
 *
 *  Copyright 2015 Nicholas Bumgart
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
    input("deviceId", "text", title: "Device ID")
    input("token", "text", title: "Access Token")
}

metadata {

	attribute "connection", "string"
    
	definition (name: "Spark Core Temp Mon", author: "Nic B") {
		capability "Polling"
		capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
	}

	simulator {
		
	}

	tiles(scale: 2) {
		valueTile("temperature", "device.temperature", width: 6, height: 4){
            state "temperature", label: '${currentValue}°F', unit:"",
            	backgroundColors: [
					[value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
		}
        
        valueTile("humidity", "device.humidity", width: 2, height: 2) {
            state "humidity", label:'${currentValue}%', unit:"Humidity"
        }
        
        valueTile("connection", "device.connection", width: 2, height: 2){
            state "disconnected", label: 'Disconnected', backgroundColor: "#bc2323"
            state "connected", label: 'Connected', backgroundColor: "#44b621"
		}
        
        standardTile("refresh", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        main "temperature"
		details(["temperature", "humidity", "connection", "refresh"])
	}
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    
    getTemperature()
}

// Get the temperature, humidity, and connection status
private getTemperature() {
    //Spark Core API Call
    def temperatureClosure = { response ->
	  	log.debug "Temeprature request was successful, '$response.data'"
        log.debug "Connection: '$response.data.connected'"
      	if (response.data.return_value > 30 && response.data.return_value < 110) {
        sendEvent(name: "temperature", value: response.data.return_value)
        }
        else{
        sendEvent(name: "connection", value: "disconnected")
        }
        
        if (response.data.connected == true) {
        log.debug "Setting connection tile to connected"
        sendEvent(name: "connection", value: "connected")
        }
        else{
        log.debug "Setting connection tile to disconnected"
        sendEvent(name: "connection", value: "disconnected")
        }
	}
    
    def temperatureParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getTemp",
        body: [access_token: token],  
        success: temperatureClosure
	]

try {
	httpPost(temperatureParams)
} catch (e) {
    log.error "Something went wrong getting Temp: $e"
}

    def humidityClosure = { response ->
	  	log.debug "Humidity request was successful, $response.data"
      	
        if (response.data.return_value > 10 && response.data.return_value < 100) {
      	sendEvent(name: "humidity", value: response.data.return_value)
        }
        
	}
    
    def humidityParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getHum",
        body: [access_token: token],  
        success: humidityClosure
	]
try {
	httpPost(humidityParams)
} catch (e) {
    log.error "Something went wrong getting Hum: $e"
}
}