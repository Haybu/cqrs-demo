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


package io.agilehandy.pikes.summary;

import io.agilehandy.pikes.api.PikeEvent;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

/**
 * @author Haytham Mohamed
 **/
@Component
@EnableBinding(Sink.class)
public class SummaryProjection {

	private final String HEADER_EVENT_TYPE = "event-type";
	private final SummaryRepository repository;

	public SummaryProjection(SummaryRepository repository) {
		this.repository = repository;
	}


	@StreamListener(target = Sink.INPUT,
			condition = "headers[HEADER_EVENT_TYPE]=='PIKE_CREATED'")
	public void createSummaryProjection(PikeEvent event) {
		Summary summary = new Summary();
		summary.setPikeId(event.getEventSubject());
		summary.setNumberOfRents(0);
		summary.setTotalRevenue(0d);
		repository.save(summary);
	}

	@StreamListener(target = Sink.INPUT,
			condition = "headers[HEADER_EVENT_TYPE]=='PIKE_RENT'")
	public void rentSummaryProjection(PikeEvent event) {
		Summary summary = repository.findById(event.getEventSubject()).get();
		if (summary != null) {
			summary.setNumberOfRents(summary.getNumberOfRents() + 1);
			repository.save(summary);
		}
	}

	@StreamListener(target = Sink.INPUT,
			condition = "headers[HEADER_EVENT_TYPE]=='PIKE_RETURN'")
	public void returnSummaryrojection(PikeEvent event) {
		Summary summary = repository.findById(event.getEventSubject()).get();
		if (summary != null) {
			//summary.setTotalRentCost(summary.getTotalRentCost() + event.get);
			repository.save(summary);
		}
	}

}
