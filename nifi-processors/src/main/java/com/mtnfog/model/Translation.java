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