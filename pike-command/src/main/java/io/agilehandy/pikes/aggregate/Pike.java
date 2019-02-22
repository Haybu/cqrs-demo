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


package io.agilehandy.pikes.aggregate;


import io.agilehandy.pikes.commands.PikeCreateCommand;
import io.agilehandy.pikes.commands.PikeRentCommand;
import io.agilehandy.pikes.commands.PikeReturnCommand;
import io.agilehandy.pikes.events.PikeEvent;
import io.agilehandy.pikes.events.PikeEventType;
import javaslang.API;
import javaslang.Predicates;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
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

	private Long rentStartTime;

	private Long rentEndTime;

	private String rentedBy;

	private Double ratePerHour;

	private Double rentCost;

	public Pike() {}

	public void setSize(PikeSize size) {
		this.size = size.getValue();
	}

	public Pike(PikeCreateCommand pikeCreateCommand) {
		Assert.notNull(pikeCreateCommand.getSize(), "Pike size should not be null");
		Assert.notNull(pikeCreateCommand.getRatePerHour(), "Pike rent ratre should assigned");
		Assert.notNull(pikeCreateCommand.getLocation(), "Pike location should not be null");

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("location", pikeCreateCommand.getLocation());
		metadata.put("ratePerHour", pikeCreateCommand.getRatePerHour());

		log.info("About to send create pike event..");

		PikeEvent event =
				new PikeEvent(UUID.randomUUID().toString()
						, PikeEventType.PIKE_CREATED
						, Instant.now(), metadata);

		pikeCreated(event);

		log.info("sent PikeCreatedEvent");
	}

	public Pike pikeCreated(PikeEvent event) {
		this.id = event.getEventSubject();
		this.availability = true;
		this.location = (String) event.getEventMetadata().get("location");
		this.ratePerHour = (Double) event.getEventMetadata().get("ratePerHour");
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
						, Instant.now(), metadata);

		pikeRented(event);
		return true;
	}

	private Pike pikeRented(PikeEvent event) {
		this.availability = false;
		this.location = (String)event.getEventMetadata().get("location");
		this.rentedBy = (String)event.getEventMetadata().get("rentedBy");
		this.rentStartTime = event.getEventDate().getEpochSecond();
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
						, Instant.now(), metadata);
		
		pikeReturned(event);
		return true;
	}

	private Pike pikeReturned(PikeEvent event) {
		this.availability = false;
		this.location = (String)event.getEventMetadata().get("location");
		Long seconds = Duration.between(Instant.ofEpochSecond(this.rentStartTime), event.getEventDate()).getSeconds();
		Double charge = this.ratePerHour * (seconds/60 * 60);
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
