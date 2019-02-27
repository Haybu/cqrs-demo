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


package io.agilehandy.pikes.commands;

import io.agilehandy.common.api.PikeCreateCommand;
import io.agilehandy.common.api.PikeRentCommand;
import io.agilehandy.common.api.PikeReturnCommand;
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
public class PikeCmdController {

	private final PikeCmdService pikeService;

	public PikeCmdController(PikeCmdService pikeService) {
		this.pikeService = pikeService;
	}

	@PostMapping("/")
	public String createPike(@RequestBody PikeCreateCommand cmd) {
		log.info("creating command received: " + cmd);
		return pikeService.createPike(cmd);
	}

	@PostMapping("/{id}/rent")
	public boolean rentPike(@PathVariable String id, @RequestBody PikeRentCommand cmd) {
		cmd.setSubjectId(id);
		return pikeService.rentPike(cmd);
	}

	@PostMapping("/{id}/return")
	public boolean returnPike(@PathVariable String id, @RequestBody PikeReturnCommand cmd) {
		cmd.setSubjectId(id);
		return pikeService.returnPike(cmd);
	}

	/*
	hidden behind a proxy. Responsible to read aggregates only
	 */
	@GetMapping("/")
	public List<Pike> allPikes() {
		return pikeService.getAllPikes();
	}

	/*
	hidden behind a proxy. Responsible to read aggregates only
	 */
	@GetMapping("/{id}")
	public Pike pikeById(@PathVariable String id) {
		return pikeService.getById(id);
	}
}
