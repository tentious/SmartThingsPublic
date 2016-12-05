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
	definition (name: "Spark Core Temp Mon", author: "Nic B") {
		capability "Polling"
		capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
	}

	simulator {
		
	}

	tiles {
		valueTile("temperature", "device.temperature", width: 2, height: 2){
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
        
        valueTile("humidity", "device.humidity", width: 1, height: 1) {
            state "default", label:'${currentValue}%', unit:"Humidity"
        }
        
        standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
        
        main "temperature"
		details(["temperature", "humidity", "refresh"])
	}
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    
    getTemperature()
}

// Get the temperature & humidity
private getTemperature() {
    //Spark Core API Call
    def temperatureClosure = { response ->
	  	log.debug "Temeprature Request was successful, $response.data"
      
      	sendEvent(name: "temperature", value: response.data.return_value)
	}
    
    def temperatureParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getTemp",
        body: [access_token: token],  
        success: temperatureClosure
	]

	httpPost(temperatureParams)

    def humidityClosure = { response ->
	  	log.debug "Humidity Request was successful, $response.data"
      
      	sendEvent(name: "humidity", value: response.data.return_value)
	}
    
    def humidityParams = [
  		uri: "https://api.spark.io/v1/devices/${deviceId}/getHum",
        body: [access_token: token],  
        success: humidityClosure
	]

	httpPost(humidityParams)
}