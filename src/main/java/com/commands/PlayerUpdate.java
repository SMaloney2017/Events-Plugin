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
public class PlayerUpdate
{
	/**
	 * Amount of EXP that must be gained for an update to be submitted.
	 */
	private static final int XP_THRESHOLD = 5000;

	private final Client client;

	private final ExampleConfig config;

	private final OkHttpClient okHttpClient;

	private long lastAccount;

	private boolean fetchXp;

	private long lastXp;

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

		fetchXp = true;
		lastAccount = -1L;
	}

	public void login()
	{
		if (lastAccount != client.getAccountHash())
		{
			lastAccount = client.getAccountHash();
			fetchXp = true;
		}
	}

	public void logout()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		long totalXp = client.getOverallExperience();
		// Don't submit update unless xp threshold is reached
		if (Math.abs(totalXp - lastXp) > XP_THRESHOLD)
		{
			log.debug("Submitting update for {} accountHash {}", local.getName(), lastAccount);
			update(lastAccount, local.getName());
			lastXp = totalXp;
		}
	}

	private void update(long accountHash, String username)
	{
		EnumSet<WorldType> worldTypes = client.getWorldType();
		username = username.replace(" ", "_");
		updateTempleosrs(accountHash, username, worldTypes);
	}

	private void updateTempleosrs(long accountHash, String username, EnumSet<WorldType> worldTypes)
	{
		if (config.templeosrs()
			&& !worldTypes.contains(WorldType.SEASONAL)
			&& !worldTypes.contains(WorldType.DEADMAN)
			&& !worldTypes.contains(WorldType.NOSAVE_MODE))
		{
			HttpUrl.Builder url = new HttpUrl.Builder()
				.scheme("https")
				.host("templeosrs.com")
				.addPathSegment("php")
				.addPathSegment("add_datapoint.php")
				.addQueryParameter("player", username)
				.addQueryParameter("accountHash", Long.toString(accountHash));

			if (worldTypes.contains(WorldType.FRESH_START_WORLD))
			{
				url.addQueryParameter("worldType", "fsw");
			}

			Request request = new Request.Builder()
				.header("User-Agent", "RuneLite")
				.url(url.build())
				.build();

			sendRequest("TempleOSRS", request);
		}
	}

	private void sendRequest(String platform, Request request)
	{
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Error submitting {} update, caused by {}.", platform, e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				response.close();
			}
		});
	}

	public long getLastXp()
	{
		return this.lastXp;
	}

	public void setLastXp(long lastXp)
	{
		this.lastXp = lastXp;
	}

	public boolean getFetchXp()
	{
		return fetchXp;
	}

	public void setFetchXp(boolean fetchXp)
	{
		this.fetchXp = fetchXp;
	}
}
