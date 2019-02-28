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

import io.agilehandy.bikes.commands.pubsub.BikeEventPubSub;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.cloud.stream.binder.kafka.streams.QueryableStoreRegistry;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Haytham Mohamed
 **/
@Repository
public class BikeCmdRepository {

	private final BikeEventPubSub pubsub;
	private final QueryableStoreRegistry queryableStoreRegistry;

	public BikeCmdRepository(BikeEventPubSub pubsub, QueryableStoreRegistry registry) {
		this.pubsub = pubsub;
		this.queryableStoreRegistry = registry;
	}

	public void save(Bike pike) {
		pike.getEvents().stream().forEach(e -> pubsub.publish(e));
		pike.flush();
	}

	public Bike findById(String id) {
		ReadOnlyKeyValueStore<String, Bike> queryStore =
				queryableStoreRegistry.getQueryableStoreType(BikeEventPubSub.EVENTS_SNAPSHOT
								, QueryableStoreTypes.<String, Bike>keyValueStore());
		return queryStore.get(id);
	}

	public List<Bike> findAll() {
		ReadOnlyKeyValueStore<String, Bike> queryStore =
				queryableStoreRegistry.getQueryableStoreType(BikeEventPubSub.EVENTS_SNAPSHOT
						, QueryableStoreTypes.<String, Bike>keyValueStore());
		KeyValueIterator<String, Bike> all = queryStore.all();
		List<Bike> pikes = new ArrayList<>();
		while(all.hasNext()) {
			pikes.add(all.next().value);
		}
		return pikes;
	}

}
