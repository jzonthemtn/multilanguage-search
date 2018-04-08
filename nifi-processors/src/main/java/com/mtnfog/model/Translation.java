/*
 * (C) Copyright 2018 Mountain Fog, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mtnfog.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Translation {

	@SerializedName("translatedText")
	@Expose
	private String translatedText;

	@SerializedName("raw_nbest")
	@Expose
	private List<RawNbest> rawNbest = null;

	public String getTranslatedText() {
		return translatedText;
	}

	public void setTranslatedText(String translatedText) {
		this.translatedText = translatedText;
	}

	public List<RawNbest> getRawNbest() {
		return rawNbest;
	}

	public void setRawNbest(List<RawNbest> rawNbest) {
		this.rawNbest = rawNbest;
	}

}