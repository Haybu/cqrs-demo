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


package io.agilehandy.pikes.commands.api;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import javaslang.API;
import javaslang.Predicates;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static javaslang.API.*;


/**
 * @author Haytham Mohamed
 **/
@Data
@Slf4j
public class Pike {

	private List<PikeEvent> events = new ArrayList<>();

	private String id;

	private String size;

	private Boolean availability;

	private String location;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDate rentStartTime;

	@JsonSerialize(using = LocalDateSerializer.class)
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate rentEndTime;

	private String rentedBy;

	private Double ratePerHour;

	private Double rentCost;

	public Pike() {}

	public Pike(PikeCreateCommand pikeCreateCommand) {
		Assert.notNull(pikeCreateCommand.getSize(), "Pike size should not be null");
		Assert.notNull(pikeCreateCommand.getRatePerHour(), "Pike rent ratre should assigned");
		Assert.notNull(pikeCreateCommand.getLocation(), "Pike location should not be null");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeCreateCommand.getLocation());
		metadata.put("ratePerHour", pikeCreateCommand.getRatePerHour());
		metadata.put("size", pikeCreateCommand.getSize().getValue());

		log.info("About to send create pike event..");

		PikeEvent event =
				new PikeEvent(UUID.randomUUID().toString()
						, PikeEventType.PIKE_CREATED
						, LocalDate.now(), metadata);

		pikeCreated(event);

		log.info("sent PikeCreatedEvent");
	}

	public Pike pikeCreated(PikeEvent event) {
		if (event.getEventType() != PikeEventType.PIKE_CREATED) {
			return this;
		}
		log.info("event source: {}", event.getEventType().getValue());
		this.id = event.getEventSubject();
		this.availability = true;
		this.location = (String) event.getEventMetadata().get("location");
		this.ratePerHour = (Double) event.getEventMetadata().get("ratePerHour");
		this.size = (String) event.getEventMetadata().get("size");
		this.rentCost = 0d;
		this.addEvent(event);
		return this;
	}

	public boolean rent(PikeRentCommand pikeRentCommand) {
		Assert.notNull(pikeRentCommand.getLocation(), "Rent location should be set");
		Assert.notNull(pikeRentCommand.getRentedBy(), "Renter ID should be set");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeRentCommand.getLocation());
		metadata.put("rentedBy", pikeRentCommand.getRentedBy());

		PikeEvent event =
				new PikeEvent(this.getId()
						, PikeEventType.PIKE_RENTED
						, LocalDate.now(), metadata);

		pikeRented(event);
		return true;
	}

	private Pike pikeRented(PikeEvent event) {
		if (event.getEventType() != PikeEventType.PIKE_RENTED) {
			return this;
		}
		log.info("event source: {}", event.getEventType().getValue());
		this.availability = false;
		this.location = (String)event.getEventMetadata().get("location");
		this.rentedBy = (String)event.getEventMetadata().get("rentedBy");
		this.rentStartTime = event.getEventDate();
		this.addEvent(event);
		return this;
	}

	public boolean returnPike(PikeReturnCommand pikeReturnCommand) {
		Assert.notNull(pikeReturnCommand.getLocation(), "Pike location should not be null");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeReturnCommand.getLocation());

		PikeEvent event =
				new PikeEvent(this.getId()
						, PikeEventType.PIKE_RETURNED
						, LocalDate.now(), metadata);
		
		pikeReturned(event);
		return true;
	}

	private Pike pikeReturned(PikeEvent event) {
		if (event.getEventType() != PikeEventType.PIKE_RETURNED) {
			return this;
		}
		log.info("event source: {}", event.getEventType().getValue());
		this.availability = false;
		this.location = (String)event.getEventMetadata().get("location");
		Duration between = Duration.between(this.rentStartTime, event.getEventDate());
		Double charge = this.ratePerHour * (between.getSeconds()/60 * 60);
		DecimalFormat df = new DecimalFormat("#.00");
		this.rentCost = Double.valueOf(df.format(charge));
		this.addEvent(event);
		return this;
	}

	public void addEvent(PikeEvent event) {
		this.events.add(event);
	}

	public void flush() {
		this.events.clear();
	}

	public List<PikeEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

	/**
	public static Pike sourceFrom(List<PikeEvent> events) {
		return javaslang.collection.List.ofAll(events).foldLeft(
				new Pike(),
				Pike::handleEvent
		);
	}
	 */

	public Pike handleEvent(PikeEvent event) {
		return API.Match(event.getEventType()).of(
				Case(Predicates.is(PikeEventType.PIKE_CREATED), this.pikeCreated(event)),
				Case(Predicates.is(PikeEventType.PIKE_RENTED), this.pikeRented(event)),
				Case(Predicates.is(PikeEventType.PIKE_RETURNED), this.pikeReturned(event))
		);
	}

}