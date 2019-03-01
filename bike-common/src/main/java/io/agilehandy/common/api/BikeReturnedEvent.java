/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.agilehandy.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Haytham Mohamed
 **/
@Data
public class BikeReturnedEvent extends BikeBaseEvent implements BikeEvent {

	private LocalDateTime endDate;
	private String location;
	private Double cost;

	public BikeReturnedEvent() { }

	public BikeReturnedEvent(String id, String location, Double cost
			,LocalDateTime eventDate, Map<String, Object> metadata) {
		super(id, BikeEventTypes.BIKE_RENTED, eventDate, metadata);
		this.location = location;
		this.endDate = eventDate;
		this.cost = cost;
	}

}
