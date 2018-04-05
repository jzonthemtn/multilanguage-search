package com.mtnfog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RawNbest {

	@SerializedName("hyp")
	@Expose
	private String hyp;
	
	@SerializedName("totalScore")
	@Expose
	private Double totalScore;

	public String getHyp() {
		return hyp;
	}

	public void setHyp(String hyp) {
		this.hyp = hyp;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

}