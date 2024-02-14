package com.commands;

import com.example.ExampleConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanSettings;
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
public class GroupUpdate
{
	private final Client client;

	private final ExampleConfig config;

	private final OkHttpClient okHttpClient;

	@Inject
	public GroupUpdate(
		OkHttpClient okHttpClient,
		Client client,
		ExampleConfig config
	)
	{
		this.okHttpClient = okHttpClient;
		this.client = client;
		this.config = config;
	}

	private void update()
	{
		ClanSettings localClan = client.getClanSettings();

		if (Objects.isNull(localClan))
		{
			return;
		}

		List<String> members = new ArrayList<>();

		for (ClanMember member : localClan.getMembers())
		{
			members.add(member.getName().replace('\u00A0', ' '));
		}

		Request request = buildRequest(
			String.valueOf(config.groupid()),
			config.groupkey(),
			members
		);

		sendRequest("TempleOSRS", request);
	}

	private Request buildRequest(String id, String key, List<String> members)
	{
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("api")
			.addPathSegment("edit_group.php");

		RequestBody body = new FormBody.Builder()
			.add("id", id)
			.add("key", key)
			.add("memberlist", String.valueOf(members)).build();

		return new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url.build())
			.post(body)
			.build();
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
}
