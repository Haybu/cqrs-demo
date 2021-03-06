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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Haytham Mohamed
 **/
@RestController
public class SummaryController {

	private final SummaryRepository repository;

	public SummaryController(SummaryRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/summary/{id}")
	public Summary getOnSummary(@PathVariable String id) {
		return repository.findById(id).orElse(new Summary("-1",0,0d));
	}

	@GetMapping("/summary")
	public Iterable<Summary> getAllSummary() {
		return repository.findAll();
	}

}
