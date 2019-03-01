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

import io.agilehandy.common.api.BikeCreateCommand;
import io.agilehandy.common.api.BikeRentCommand;
import io.agilehandy.common.api.BikeReturnCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Haytham Mohamed
 **/
@RestController
@Slf4j
public class BikeCmdController {

	private final BikeCmdService bikeService;

	public BikeCmdController(BikeCmdService bikeService) {
		this.bikeService = bikeService;
	}

	@PostMapping("/")
	public String createBike(@RequestBody BikeCreateCommand cmd) {
		log.info("creating command received: " + cmd);
		return bikeService.createBike(cmd);
	}

	@PostMapping("/{id}/rent")
	public boolean rentBike(@PathVariable String id, @RequestBody BikeRentCommand cmd) {
		cmd.setSubjectId(id);
		return bikeService.rentBike(cmd);
	}

	@PostMapping("/{id}/return")
	public boolean returnBike(@PathVariable String id, @RequestBody BikeReturnCommand cmd) {
		cmd.setSubjectId(id);
		return bikeService.returnBike(cmd);
	}

	/*
	hidden behind a proxy. Responsible to read aggregates only
	 */
	@GetMapping("/")
	public List<Bike> allBikes() {
		return bikeService.getAllBikes();
	}

	/*
	hidden behind a proxy. Responsible to read aggregates only
	 */
	@GetMapping("/{id}")
	public Bike BikeById(@PathVariable String id) {
		return bikeService.getById(id);
	}
}
