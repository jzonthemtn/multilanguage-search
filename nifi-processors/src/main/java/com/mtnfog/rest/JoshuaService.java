package com.mtnfog.rest;

import com.mtnfog.model.JoshuaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JoshuaService {

	@GET("/translate")
	Call<JoshuaResponse> translate(@Query("q") String input);

}
