package com.mtnfog.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JoshuaResponse {

	@SerializedName("data")
	@Expose
	private Data data;
	
	@SerializedName("metadata")
	@Expose
	private List<String> metadata = null;

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public List<String> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<String> metadata) {
		this.metadata = metadata;
	}

}