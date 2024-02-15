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
public class GroupUpdate extends CommandAbstract
{
	private final Client client;

	private final ExampleConfig config;

	private final OkHttpClient okHttpClient;

	private List<String> members;

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

	@Override
	public void initialize()
	{
		this.members = getLocalClanMembers();
	}

	@Override
	public boolean validate()
	{
		ClanSettings localClan = client.getClanSettings();
		if (Objects.isNull(localClan))
		{
			return false;
		}

		// TODO:: Refactor this into its own getGroupData command
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("api")
			.addPathSegment("group_info.php")
			.addQueryParameter("id", String.valueOf(config.groupid()));

		Request request = new Request.Builder()
			.header("User-Agent", "RuneLite")
			.url(url.build())
			.build();

		try
		{
			Response response = okHttpClient.newCall(request).execute();
			String body = response.body().string();
			// TODO:: Is better validation possible?
			return body.contains(localClan.getName());
		}
		catch (Exception e)
		{
			return false;
		}
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
		members = null;
	}

	private Request buildRequest()
	{
		HttpUrl.Builder url = new HttpUrl.Builder()
			.scheme("https")
			.host("templeosrs.com")
			.addPathSegment("api")
			.addPathSegment("edit_group.php");

		RequestBody body = new FormBody.Builder()
			.add("id", String.valueOf(config.groupid()))
			.add("key", config.groupkey())
			.add("memberlist", String.valueOf(members))
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

	public List<String> getLocalClanMembers()
	{
		List<String> members = new ArrayList<>();
		ClanSettings localClan = client.getClanSettings();

		if (Objects.isNull(localClan) || localClan.getMembers().isEmpty())
		{
			return members;
		}

		for (ClanMember member : localClan.getMembers())
		{
			String name = member.getName().replace("\u00A0", "_");
			members.add(name);
		}

		return members;
	}
}
