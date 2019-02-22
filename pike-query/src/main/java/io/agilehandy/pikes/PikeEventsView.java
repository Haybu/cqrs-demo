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


package io.agilehandy.pikes;

import io.agilehandy.pikes.events.PikeEvent;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

/**
 * @author Haytham Mohamed
 **/
@Service
@EnableBinding(PikeEventsBinding.class)
public class PikeEventsView {

	@StreamListener(PikeEventsBinding.PIKE_EVENT_IN)
	public KTable<String, PikeEvent> sink(KStream<String, PikeEvent> events) {
		//events
				//.groupByKey()
				//.aggregate(new Pike(), )
		return null;

	}
}
