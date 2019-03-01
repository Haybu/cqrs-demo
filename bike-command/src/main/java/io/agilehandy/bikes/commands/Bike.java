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


package io.agilehandy.bikes.commands;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.agilehandy.common.api.*;
import javaslang.API;
import javaslang.Predicates;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static javaslang.API.*;


/**
 * @author Haytham Mohamed
 **/
@Data
@Slf4j
public class Bike {

	private List<BikeBaseEvent> events = new ArrayList<>();

	private String id;

	private BikeSize size;

	private Boolean availability;

	private String location;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime rentStartTime;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime rentEndTime;

	private String rentedBy;

	private Double rate;

	private Double rentCost;

	public Bike() {}

	public Bike(BikeCreateCommand cmd) {
		Assert.notNull(cmd.getSize(), "Bike size should not be null");
		Assert.notNull(cmd.getRate(), "Bike rent rate should assigned");
		Assert.notNull(cmd.getLocation(), "Bike location should not be null");

		BikeCreatedEvent event =
				new BikeCreatedEvent(
						UUID.randomUUID().toString(),
						cmd.getLocation(),
						cmd.getRate(),
						cmd.getSize().getValue(),
						LocalDateTime.now(), new HashMap<>());

		bikeCreated(event);
	}

	public Bike bikeCreated(BikeCreatedEvent event) {
		log.info("event sourced: {}", event.getEventType());
		this.id = event.getEventSubject();
		this.availability = true;
		this.location = (String) event.getLocation();
		this.rate = (Double) event.getRate();
		this.size = BikeSize.fromValue((String)event.getSize());
		this.rentCost = 0d;
		this.addEvent(event);
		return this;
	}

	public boolean rent(BikeRentCommand cmd) {
		Assert.notNull(cmd.getRentedBy(), "Renter ID should be set");

		BikeRentedEvent event =
				new BikeRentedEvent(this.getId()
						, cmd.getRentedBy()
						, LocalDateTime.now(), new HashMap<>());

		bikeRented(event);
		return true;
	}

	private Bike bikeRented(BikeRentedEvent event) {
		log.info("event sourced: {}", event.getEventType());
		this.availability = false;
		this.rentedBy = (String)event.getRentedBy();
		this.rentStartTime = event.getEventDate();
		this.addEvent(event);
		return this;
	}

	public boolean returnBike(BikeReturnCommand cmd) {
		Assert.notNull(cmd.getLocation(), "Bike location should not be null");

		Duration between = Duration.between(this.rentStartTime, LocalDateTime.now());
		Double charge = this.rate * between.getSeconds();
		DecimalFormat df = new DecimalFormat("#.00");

		BikeReturnedEvent event =
				new BikeReturnedEvent(this.getId()
						, cmd.getLocation()
						, Double.valueOf(df.format(charge))
						, LocalDateTime.now(), new HashMap<>());
		
		bikeReturned(event);
		return true;
	}

	private Bike bikeReturned(BikeReturnedEvent event) {
		log.info("event source: {}", event.getEventType());
		this.availability = true;
		this.location = (String)event.getLocation();
		this.rentCost = event.getCost();
		this.addEvent(event);
		return this;
	}

	public void addEvent(BikeBaseEvent event) {
		this.events.add(event);
	}

	public void flush() {
		this.events.clear();
	}

	public List<BikeBaseEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

	/**
	public static Bike sourceFrom(List<BikeEvent> events) {
		return javaslang.collection.List.ofAll(events).foldLeft(
				new Bike(),
				Bike::handleEvent
		);
	}
	 */

	public Bike handleEvent(BikeEvent event) {
		return API.Match(event.getEventType()).of(
				Case(Predicates.is(BikeEventTypes.BIKE_CREATED), this.bikeCreated((BikeCreatedEvent) event)),
				Case(Predicates.is(BikeEventTypes.BIKE_RENTED), this.bikeRented((BikeRentedEvent) event)),
				Case(Predicates.is(BikeEventTypes.BIKE_RETURNED), this.bikeReturned((BikeReturnedEvent) event))
		);
	}

}
