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


package io.agilehandy.bikes.commands.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agilehandy.bikes.commands.Bike;
import io.agilehandy.common.api.BikeBaseEvent;
import io.agilehandy.common.api.BikeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.cloud.stream.annotation.EnableBinding;
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
@EnableBinding(BikeEventChannels.class)
@Slf4j
public class BikeEventPubSub {

	private final BikeEventChannels channels;

	public static final String EVENTS_SNAPSHOT = "events_snapshots";

	private final String HEADER_EVENT_TYPE = "event_type";

	public BikeEventPubSub(BikeEventChannels channels) {
		this.channels = channels;
	}

	public void publish(BikeBaseEvent event) {
		Message<BikeBaseEvent> message = MessageBuilder
				.withPayload(event)
				.setHeader(KafkaHeaders.MESSAGE_KEY, event.getEventSubject().getBytes())
				.setHeader(HEADER_EVENT_TYPE, event.getEventType())
				.build();
		log.info("start publishing create pike event..");
		channels.output().send(message);
		log.info("finish publishing create pike event..");
	}

	// Kafka KTable of aggregate snapshot
	@StreamListener(BikeEventChannels.PIKE_EVENTS_IN)
	public void snapshot(KStream<String, BikeEvent> events) {
		Serde<BikeEvent> pikeEventSerde = new JsonSerde<>( BikeEvent.class, new ObjectMapper() );
		Serde<Bike> pikeSerde = new JsonSerde<>( Bike.class, new ObjectMapper() );

		events
				//.groupBy( (s, event) -> event.getEventSubject(),
				//Serialized.with(Serdes.String(), pikeEventSerde) )
				.groupByKey()
				.aggregate(Bike::new, (key, event, pike) -> ((Bike) pike).handleEvent(event),
						Materialized.<String, Bike, KeyValueStore<Bytes, byte[]>>as(EVENTS_SNAPSHOT)
								.withKeySerde(Serdes.String())
								.withValueSerde(pikeSerde)
				);
	}

}
