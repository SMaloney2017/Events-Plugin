package com.commands;

import com.example.ExampleConfig;
import java.io.IOException;
import java.util.EnumSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Singleton
public class PlayerUpdate extends CommandAbstract
{
	private final Client client;

	private final ExampleConfig config;

	private final OkHttpClient okHttpClient;

	public String username;

	public long lastAccount;

	@Inject
	public PlayerUpdate(
		OkHttpClient okHttpClient,
		Client client,
		ExampleConfig config
	)
	{
		this.okHttpClient = okHttpClient;
		this.client = client;
		this.config = config;
	}

	@Override
	public void initialize()
	{
		Player player = client.getLocalPlayer();
		username = player.getName().replace("\u00A0", "_");
	}

	@Override
	public boolean validate()
	{
		EnumSet<WorldType> worldTypes = client.getWorldType();
		return !config.templeosrs()
			|| worldTypes.contains(WorldType.SEASONAL)
			|| worldTypes.contains(WorldType.DEADMAN)
			|| worldTypes.contains(WorldType.NOSAVE_MODE)
			|| username.isEmpty();
	}

	@Override
	public void execute()
	{
		initialize();

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
		username = null;
		lastAccount = -1L;
	}

	public Request buildRequest()
	{
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("php")
			.addPathSegment("add_datapoint.php")
			.addQueryParameter("player", username)
			.addQueryParameter("accountHash", String.valueOf(lastAccount));

		if (client.getWorldType().contains(WorldType.FRESH_START_WORLD))
		{
			url.addQueryParameter("worldType", "fsw");
		}

		return new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url.build())
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
