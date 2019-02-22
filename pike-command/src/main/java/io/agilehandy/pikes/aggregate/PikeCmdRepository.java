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

import io.agilehandy.pikes.pubsub.PikeEventPubSub;
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
public class PikeCmdRepository {

	private final PikeEventPubSub pubsub;
	private final QueryableStoreRegistry queryableStoreRegistry;

	public PikeCmdRepository(PikeEventPubSub pubsub, QueryableStoreRegistry registry) {
		this.pubsub = pubsub;
		this.queryableStoreRegistry = registry;
	}

	public void save(Pike pike) {
		pike.getEvents().stream().forEach(e -> pubsub.publish(e));
		pike.flush();
	}

	public Pike findById(String id) {
		ReadOnlyKeyValueStore<String, Pike> queryStore =
				queryableStoreRegistry.getQueryableStoreType(PikeEventPubSub.EVENTS_SNAPSHOT
								, QueryableStoreTypes.keyValueStore());
		return queryStore.get(id);
	}

	public List<Pike> findAll() {
		ReadOnlyKeyValueStore<String, Pike> queryStore =
				queryableStoreRegistry.getQueryableStoreType(PikeEventPubSub.EVENTS_SNAPSHOT
						, QueryableStoreTypes.keyValueStore());
		KeyValueIterator<String, Pike> all = queryStore.all();
		List<Pike> pikes = new ArrayList<>();
		while(all.hasNext()) {
			pikes.add(all.next().value);
		}
		return pikes;
	}

}
