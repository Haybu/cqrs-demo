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


package io.agilehandy.common.api;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * @author Haytham Mohamed
 **/
@Value
public class PikeRentCommand extends PikeBaseCommand {

	private LocalDateTime startTime;
	private String rentedBy;

	public PikeRentCommand(String subjectId, String location, String rentedBy) {
		super(subjectId);
		this.startTime = LocalDateTime.now();
		this.rentedBy = rentedBy;
	}
}