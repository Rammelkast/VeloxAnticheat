package com.rammelkast.veloxanticheat.player.processor;

import org.bukkit.event.Event;

import com.rammelkast.veloxanticheat.player.PlayerWrapper;

public abstract class Processor {

	public abstract void process(final PlayerWrapper wrapper, final Event event);
	
}
