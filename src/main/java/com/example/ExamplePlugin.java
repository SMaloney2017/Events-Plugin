package com.example;

import com.commands.PlayerUpdate;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private PlayerUpdate playerUpdate;
	@Inject
	private Client client;
	@Inject
	private ExampleConfig config;
	@Inject
	private OkHttpClient okHttpClient;

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState state = gameStateChanged.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			playerUpdate.login();
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			playerUpdate.logout();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (playerUpdate.getFetchXp())
		{
			playerUpdate.setLastXp(client.getOverallExperience());
			playerUpdate.setFetchXp(false);
		}
	}
}
