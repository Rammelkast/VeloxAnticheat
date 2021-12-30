package com.rammelkast.veloxanticheat.checks;

import java.util.Arrays;

import org.bukkit.ChatColor;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;

public abstract class Check<T> {

	protected final PlayerWrapper wrapper;
	protected final String name;
	protected final char type;

	protected double buffer;
	protected int violations;
	protected boolean verbose;

	public Check(final PlayerWrapper wrapper) {
		this.wrapper = wrapper;

		if (!getClass().isAnnotationPresent(CheckInfo.class)) {
			throw new RuntimeException("Check is missing @CheckInfo annotation!");
		}

		final CheckInfo info = getClass().getAnnotation(CheckInfo.class);
		this.name = info.name();
		this.type = info.type();
	}

	public abstract void process(final T object);

	protected double increaseBuffer() {
		return increaseBuffer(1);
	}

	protected double increaseBuffer(final double amount) {
		return (this.buffer += amount);
	}

	protected double decreaseBuffer() {
		return decreaseBuffer(1);
	}

	protected double decreaseBuffer(final double amount) {
		return (this.buffer = Math.max(0, this.buffer - amount));
	}

	protected void resetBuffer() {
		this.buffer = 0;
	}

	protected boolean exempt(final Exemption exemption) {
		return exemption.getAction().apply(this.wrapper);
	}

	protected boolean exempt(final Exemption... exemptions) {
		return Arrays.stream(exemptions).anyMatch(this::exempt);
	}

	protected void verbose(final String data) {
		if (!this.verbose) {
			// Only log when verbose is enabled
			return;
		}

		this.wrapper.getPlayer().sendMessage(ChatColor.RED + "Verbose " + ChatColor.DARK_GRAY + "| " + ChatColor.YELLOW
				+ this.name + " (" + this.type + ") " + ChatColor.DARK_GRAY + "> " + ChatColor.WHITE + data);
	}

	protected void fail(final String data) {
		fail(data, false);
	}

	protected void fail(final String data, final boolean rubberband) {
		this.violations++;

		if (VeloxAnticheat.getInstance().getSettingsManager().isTestMode()) {
			// Test mode logging
			this.wrapper.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> "
					+ ChatColor.GRAY + "Failed " + ChatColor.RED + this.name + ChatColor.GRAY + " (" + this.type + ") "
					+ ChatColor.YELLOW + "[" + this.violations + "x]" + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + data
					+ ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "ping: " + this.wrapper.getPlayer().getPing()
					+ "ms, tps: " + MathLib.roundDouble(VeloxAnticheat.getInstance().getTPS(), 1));
		} else {
			// TODO rubberband
			this.wrapper.registerViolation(this);
		}
	}

	public boolean toggleVerbose() {
		return (this.verbose = !this.verbose);
	}

	public String getName() {
		return this.name;
	}

	public char getType() {
		return this.type;
	}

	public void decreaseViolations(final double factor) {
		this.violations *= factor;
	}

	public int getViolatons() {
		return this.violations;
	}

}
