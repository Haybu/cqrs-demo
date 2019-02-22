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

import lombok.Data;

import java.time.LocalDate;

/**
 * @author Haytham Mohamed
 **/
@Data
public class PikeRentCommand extends PikeBaseCommand {

	private String location;
	private LocalDate startTime;
	private String rentedBy;

	public PikeRentCommand(String subjectId, String location, String rentedBy) {
		super(subjectId);
		this.location = location;
		this.startTime = LocalDate.now();
		this.rentedBy = rentedBy;
	}
}
