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


package io.agilehandy.pikes.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agilehandy.pikes.aggregate.Pike;
import io.agilehandy.pikes.events.PikeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Haytham Mohamed
 **/
@Component
@EnableBinding(PikeEventChannels.class)
@Slf4j
public class PikeEventPubSub {

	private final PikeEventChannels source;
	public static final String EVENTS_SNAPSHOT = "events-snapshots";

	private final String HEADER_EVENT_TYPE = "event-type";

	public PikeEventPubSub(PikeEventChannels source) {
		this.source = source;
	}

	public void publish(PikeEvent event) {
		Message<PikeEvent> message = MessageBuilder
				.withPayload(event)
				.setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventSubject())
				.setHeader(HEADER_EVENT_TYPE, event.getEventType().getValue())
				.build();
		log.info("start publising create pike event..");
		source.output().send(message);
		log.info("end publising create pike event..");
	}

	// Kafka KTable of aggregate snapshot
	@StreamListener
	public void snapshot(@Input(PikeEventChannels.PIKE_EVENTS_IN) KStream<String, PikeEvent> events) {
		ObjectMapper mapper = new ObjectMapper();
		Serde<PikeEvent> pikeEventSerde = new JsonSerde<>( PikeEvent.class, mapper );
		Serde<Pike> pikeSerde = new JsonSerde<>( Pike.class, mapper );

		KTable<String, Pike> table = events
				.groupBy( (s, event) -> event.getEventSubject(),
						      Serialized.with(null, pikeEventSerde) )
				.aggregate(Pike::new, (key, event, pike) -> ((Pike) pike).handleEvent(event),
						Materialized.<String, Pike, KeyValueStore<Bytes, byte[]>>as(EVENTS_SNAPSHOT)
								.withKeySerde(Serdes.String())
								.withValueSerde(pikeSerde)
						);
	}

}
