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


package io.agilehandy.bikes.summary;

import io.agilehandy.common.api.BikeBaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @author Haytham Mohamed
 **/
@Component
@EnableBinding(Sink.class)
@Slf4j
public class SummaryProjection {

	private final String HEADER_EVENT_TYPE = "event-type";
	private final SummaryRepository repository;

	public SummaryProjection(SummaryRepository repository) {
		this.repository = repository;
	}

	/*@StreamListener(target = Sink.INPUT)
	public void listenToAll(@Payload BikeEvent event,
	                        @Headers Map<?,?> headers) {
		log.info("Summary projected from event {} : ", event);
		String first = headers.entrySet().stream()
				.map(e -> e.getValue()).findFirst()
				.get().toString();
		log.info("One header value is {} ", first);
	}*/

	@StreamListener(target = Sink.INPUT,
			condition = "headers['event_type']=='BIKE_CREATED'")
	public void createSummaryProjection(@Payload BikeBaseEvent event) {
		log.info("Summary projected from bike created event");
		Summary summary = new Summary();
		summary.setBikeId(event.getEventSubject());
		summary.setNumberOfRents(0);
		summary.setTotalRevenue(0d);
		repository.save(summary);
	}

	@StreamListener(target = Sink.INPUT,
			condition = "headers['event_type']=='BIKE_RENTED'")
	public void rentSummaryProjection(@Payload BikeBaseEvent event) {
		log.info("Summary projected from bike rented event");
		Summary summary = repository.findById(event.getEventSubject()).get();
		if (summary != null) {
			summary.setNumberOfRents(summary.getNumberOfRents() + 1);
			repository.save(summary);
		}
	}

	@StreamListener(target = Sink.INPUT,
			condition = "headers['event-type']=='BIKE_RETURNED'")
	public void returnSummaryProjection(@Payload BikeBaseEvent event) {
		log.info("Summary projected from bike returned event");
		Summary summary = repository.findById(event.getEventSubject()).get();
		if (summary != null) {
			summary.setTotalRevenue(summary.getTotalRevenue().doubleValue()
					+ Double.valueOf((String)event.getEventMetadata().get("cost")).doubleValue());
			repository.save(summary);
		}
	}

}
