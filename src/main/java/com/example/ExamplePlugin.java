package com.example;

import com.commands.NameUpdate;
import com.commands.PlayerUpdate;
import com.google.common.base.Strings;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Nameable;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NameableNameChanged;
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
	/**
	 * Amount of EXP that must be gained for an update to be submitted.
	 */
	private static final int XP_THRESHOLD = 5000;

	@Inject
	private PlayerUpdate playerUpdate;
	@Inject
	private NameUpdate nameUpdate;
	@Inject
	private Client client;
	@Inject
	private ExampleConfig config;
	@Inject
	private OkHttpClient okHttpClient;

	private long lastAccount;

	private boolean fetchXp;

	private long lastXp;

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}

	@Subscribe
	public void onStartUp()
	{
		fetchXp = true;
		lastAccount = -1L;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState state = gameStateChanged.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			if (lastAccount != client.getAccountHash())
			{
				lastAccount = client.getAccountHash();
				fetchXp = true;
			}
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
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
				playerUpdate.execute();
				lastXp = totalXp;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (fetchXp)
		{
			lastXp = client.getOverallExperience();
			fetchXp = false;
		}
	}

	@Subscribe
	public void onNameableNameChanged(NameableNameChanged nameableNameChanged)
	{

		final Nameable nameable = nameableNameChanged.getNameable();

		String name = nameable.getName();
		String prev = nameable.getPrevName();

		if (Strings.isNullOrEmpty(prev)
			|| name.equals(prev)
			|| prev.startsWith("[#")
			|| name.startsWith("[#"))
		{
			return;
		}

		// TODO:: Implement scheduler to limit name changes to 10/min
		nameUpdate.initialize(prev, name);
		nameUpdate.execute();
	}
}
