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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Haytham Mohamed
 **/
@Service
public class BikeCmdService {

    private final BikeCmdRepository pikeRepository;

	public BikeCmdService(BikeCmdRepository pikeRepository) {
		this.pikeRepository = pikeRepository;
	}

	public String createPike(BikeCreateCommand pikeCreateCommand) {
		Bike pike = new Bike(pikeCreateCommand);
		pikeRepository.save(pike);
		return pike.getId();
	}

	public boolean rentPike(BikeRentCommand pikeRentCommand) {
		Bike pike = pikeRepository.findById(pikeRentCommand.getSubjectId());
		boolean result = pike.rent(pikeRentCommand);
		if (result) {
			pikeRepository.save(pike);
			return true;
		}
		return false;
	}


	public boolean returnPike(BikeReturnCommand pikeReturnCommand) {
		Bike pike = pikeRepository.findById(pikeReturnCommand.getSubjectId());
		boolean result = pike.returnPike(pikeReturnCommand);
		if (result) {
			pikeRepository.save(pike);
			return true;
		}
		return false;
	}

	public List<Bike> getAllPikes() {
		return pikeRepository.findAll();
	}

	public Bike getById(String id) {
		return pikeRepository.findById(id);
	}
}
