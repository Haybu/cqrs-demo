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


package io.agilehandy.pikes.commands;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.agilehandy.common.api.PikeCreateCommand;
import io.agilehandy.common.api.PikeEvent;
import io.agilehandy.common.api.PikeEventType;
import io.agilehandy.common.api.PikeRentCommand;
import io.agilehandy.common.api.PikeReturnCommand;
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
	private LocalDateTime rentStartTime;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime rentEndTime;

	private String rentedBy;

	private Double rate;

	private Double rentCost;

	public Pike() {}

	public Pike(PikeCreateCommand pikeCreateCommand) {
		Assert.notNull(pikeCreateCommand.getSize(), "Pike size should not be null");
		Assert.notNull(pikeCreateCommand.getRate(), "Pike rent rate should assigned");
		Assert.notNull(pikeCreateCommand.getLocation(), "Pike location should not be null");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeCreateCommand.getLocation());
		metadata.put("rate", pikeCreateCommand.getRate());
		metadata.put("size", pikeCreateCommand.getSize().getValue());

		log.info("About to send create pike event..");

		PikeEvent event =
				new PikeEvent(UUID.randomUUID().toString()
						, PikeEventType.PIKE_CREATED
						, LocalDateTime.now(), metadata);

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
		this.rate = (Double) event.getEventMetadata().get("rate");
		this.size = (String) event.getEventMetadata().get("size");
		this.rentCost = 0d;
		this.addEvent(event);
		return this;
	}

	public boolean rent(PikeRentCommand pikeRentCommand) {
		Assert.notNull(pikeRentCommand.getRentedBy(), "Renter ID should be set");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("rentedBy", pikeRentCommand.getRentedBy());

		PikeEvent event =
				new PikeEvent(this.getId()
						, PikeEventType.PIKE_RENTED
						, LocalDateTime.now(), metadata);

		pikeRented(event);
		return true;
	}

	private Pike pikeRented(PikeEvent event) {
		if (event.getEventType() != PikeEventType.PIKE_RENTED) {
			return this;
		}
		log.info("event source: {}", event.getEventType().getValue());
		this.availability = false;
		this.rentedBy = (String)event.getEventMetadata().get("rentedBy");
		this.rentStartTime = event.getEventDate();
		this.addEvent(event);
		return this;
	}

	public boolean returnPike(PikeReturnCommand pikeReturnCommand) {
		Assert.notNull(pikeReturnCommand.getLocation(), "Pike location should not be null");

		Duration between = Duration.between(this.rentStartTime, LocalDateTime.now());
		Double charge = this.rate * between.getSeconds();
		DecimalFormat df = new DecimalFormat("#.00");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeReturnCommand.getLocation());
		metadata.put("cost", df.format(charge));

		PikeEvent event =
				new PikeEvent(this.getId()
						, PikeEventType.PIKE_RETURNED
						, LocalDateTime.now(), metadata);
		
		pikeReturned(event);
		return true;
	}

	private Pike pikeReturned(PikeEvent event) {
		if (event.getEventType() != PikeEventType.PIKE_RETURNED) {
			return this;
		}
		log.info("event source: {}", event.getEventType().getValue());
		this.availability = true;
		this.location = (String)event.getEventMetadata().get("location");
		this.rentCost = Double.valueOf((String)event.getEventMetadata().get("cost"));
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
