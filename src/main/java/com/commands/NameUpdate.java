package com.commands;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class NameUpdate extends CommandAbstract
{
	private final OkHttpClient okHttpClient;

	private String oldUsername;

	private String newUsername;

	@Inject
	public NameUpdate(
		OkHttpClient okHttpClient
	)
	{
		this.okHttpClient = okHttpClient;
	}

	public void initialize(String oldUsername, String newUsername)
	{
		this.oldUsername = oldUsername;
		this.newUsername = newUsername;
	}

	@Override
	public void initialize()
	{

	}

	@Override
	public boolean validate()
	{
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("api")
			.addPathSegment("player")
			.addPathSegment("player_names.php")
			.addQueryParameter("player", newUsername.replace("\u00A0", "_"));

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url.build())
			.build();

		try
		{
			Response response = okHttpClient.newCall(request).execute();
			String body = response.body().string();
			// TODO:: Is better validation possible?
			return body.contains("User not found in database");
		}
		catch (IOException e)
		{
			return false;
		}
	}

	@Override
	public void execute()
	{
		if (!validate())
		{
			return;
		}

		Request request = buildRequest();
		sendRequest(request);

		reset();
	}

	@Override
	public void reset()
	{
		oldUsername = null;
		newUsername = null;
	}

	private Request buildRequest()
	{
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("new_name")
			.addPathSegment("name_change.php");

		RequestBody body = new FormBody.Builder()
			.add("oldrsn", oldUsername)
			.add("newrsn", newUsername)
			.build();

		return new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url.build())
			.post(body)
			.build();
	}

	public void sendRequest(Request request)
	{
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Error submitting {} update, caused by {}.", "TempleOSRS", e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				response.close();
			}
		});
	}
}
