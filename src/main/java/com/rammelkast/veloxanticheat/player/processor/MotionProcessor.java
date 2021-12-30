package com.rammelkast.veloxanticheat.player.processor;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;
import com.rammelkast.veloxanticheat.utils.TickTimer;
import com.rammelkast.veloxanticheat.utils.TimedLocation;

public final class MotionProcessor extends Processor {

	// Current and previous motion
	private Motion current, previous;
	
	// Collision states
	private boolean touchingLiquid;
	private boolean touchingClimbable;
	private boolean touchingBed;
	private boolean touchingSlime;
	private boolean touchingIce;
	private boolean headCollision;
	private boolean freeInAir;
	
	// Timings
	private int groundTime, airTime;
	private int packetStreak;
	private double lagFactor;
	
	// Tick timers
	public final TickTimer slimeTimer = new TickTimer();
	public final TickTimer iceTimer = new TickTimer();
	public final TickTimer bopTimer = new TickTimer();
	public final TickTimer teleportTimer = new TickTimer();
	public final TickTimer flyingTimer = new TickTimer();

	// 'Last / previous' states - local use only
	private boolean wasOnGround;
	private long lastMotionTime;
	
	@Override
	public void process(final PlayerWrapper wrapper, final Event event) {
		if (!(event instanceof PlayerMoveEvent)) {
			return;
		}
		
		final PlayerMoveEvent move = (PlayerMoveEvent) event;
		if (move.isCancelled()) {
			// Return if cancelled
			return;
		}
		
		final Player player = wrapper.getPlayer();
		@SuppressWarnings("deprecation")
		final boolean onGround = player.isOnGround();
		final Motion motion = new Motion(move.getFrom(), move.getTo(), this.wasOnGround, onGround);
		final long now = MathLib.now();
		
		if (now - this.lastMotionTime < 2L) {
			this.packetStreak++;
		} else {
			this.packetStreak = 0;
		}

		if (this.packetStreak > 0) {
			this.lagFactor = Math.min(this.lagFactor + (this.packetStreak > 1 ? 2.0 : 1.0), 5.0);
		} else {
			this.lagFactor = Math.max(0, this.lagFactor - 0.2);
		}

		// Set last ground state and motion time (we do this before because of the coming returns)
		this.wasOnGround = onGround;
		this.lastMotionTime = now;
		
		// Tick timers
		this.slimeTimer.tick();
		this.iceTimer.tick();
		this.bopTimer.tick();
		this.teleportTimer.tick();
		this.flyingTimer.tick();
		
		if (motion.toVector().length() == 0.0 && (motion.getYaw() == 0.0f && motion.getPitch() == 0.0f)) {
			// No change in position -> do not continue
			return;
		}
		
		if (player.isInsideVehicle() || player.isFlying()) {
			// Inside vehicle or flying -> do not continue
			if (player.isFlying()) {
				this.flyingTimer.reset();
			}
			return;
		}

		// Update current and previous motion
		this.previous = this.current == null ? motion : this.current;
		this.current = motion;

		final BoundingBox box = player.getBoundingBox();
		{
			// Expand and move bounding box
			box.expand(0.35, 0.07, 0.35).shift(0.0, -0.55, 0.0);

			// Compute collisions
			this.touchingLiquid = collides(player.getWorld(), box, false,
					material -> (material == Material.WATER || material == Material.LAVA));
			this.touchingClimbable = collides(player.getWorld(), box, false,
					material -> (material == Material.LADDER || material == Material.VINE
							|| material == Material.SCAFFOLDING || material == Material.HONEY_BLOCK
							|| material == Material.POWDER_SNOW || material == Material.COBWEB));
			this.touchingBed = collides(player.getWorld(), box, false, material -> material.name().endsWith("BED"));
			this.touchingSlime = collides(player.getWorld(), box, false, material -> material == Material.SLIME_BLOCK);
			this.touchingIce = collides(player.getWorld(), box, false, material -> material.name().endsWith("ICE"));
			this.freeInAir = collides(player.getWorld(), box, true, material -> material == Material.AIR);

			// Head collision
			final BoundingBox head = new BoundingBox().shift(motion.getTo()).expand(0.35, 0.25, 0.35).shift(0.0, 1.8,
					0.0);
			this.headCollision = collides(player.getWorld(), head, false, material -> material != Material.AIR);
		}

		// Update timers
		if (this.touchingSlime) {
			this.slimeTimer.reset();
		}

		if (this.touchingIce) {
			this.iceTimer.reset();
		}

		if (this.headCollision) {
			this.bopTimer.reset();
		}

		// Update variables
		if (onGround) {
			this.airTime = 0;
			this.groundTime++;
		} else {
			this.groundTime = 0;
			this.airTime++;
		}
		
		// Pass new location to timed list
		wrapper.getLocations().add(new TimedLocation(motion.getTo(), MathLib.now()));
		
		// Pass motion to checks
		wrapper.getChecks().stream().filter(MotionCheck.class::isInstance)
				.filter(check -> VeloxAnticheat.getInstance().getSettingsManager().isCheckEnabled(check))
				.forEach(check -> ((MotionCheck) check).process(motion));
	}
	
	private boolean collides(final World world, final BoundingBox box, final boolean matchAll, final Predicate<Material> predicate) {
		final int minX = (int) Math.floor(box.getMinX());
		final int maxX = (int) Math.ceil(box.getMaxX());
		final int minY = (int) Math.floor(box.getMinY());
		final int maxY = (int) Math.ceil(box.getMaxY());
		final int minZ = (int) Math.floor(box.getMinZ());
		final int maxZ = (int) Math.ceil(box.getMaxZ());
		final ArrayList<Block> blocks = new ArrayList<>();
        {
        	blocks.add(world.getBlockAt(minX, minY, minZ));
        }
        
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					blocks.add(world.getBlockAt(i, j, k));
				}
			}
		}
		return matchAll ? blocks.stream().allMatch(block -> predicate.test(block.getType()))
				: blocks.stream().anyMatch(block -> predicate.test(block.getType()));
	}

	/**
	 * Gets the current motion
	 * 
	 * @return the current motion
	 */
	public Motion getCurrent() {
		return this.current;
	}
	
	/**
	 * Gets the previous motion
	 * 
	 * @return the previous motion
	 */
	public Motion getPrevious() {
		return this.previous;
	}
	
	/**
	 * Gets if the player is colliding with a liquid
	 * 
	 * @return true if colliding with a liquid
	 */
	public boolean isTouchingLiquid() {
		return this.touchingLiquid;
	}
	
	/**
	 * Gets if the player is colliding with a climbable
	 * 
	 * @return true if colliding with a climbable
	 */
	public boolean isTouchingClimbable() {
		return this.touchingClimbable;
	}

	/**
	 * Gets if the player is colliding with a bed
	 * 
	 * @return true if colliding with a bed
	 */
	public boolean isTouchingBed() {
		return this.touchingBed;
	}
	
	/**
	 * Gets if the player is colliding with a slime block
	 * 
	 * @return true if colliding with a slime block
	 */
	public boolean isTouchingSlime() {
		return this.touchingSlime;
	}
	
	/**
	 * Gets if the players head is colliding with a block
	 * 
	 * @return true if head collides
	 */
	public boolean hasHeadCollision() {
		return this.headCollision;
	}
	
	/**
	 * Gets if the player is free in air and has absolutely zero collisions
	 * 
	 * @return true if free in air
	 */
	public boolean isFreeInAir() {
		return this.freeInAir;
	}
	
	/**
	 * Gets the amount of ticks spent on ground
	 * 
	 * @return ticks on ground
	 */
	public int getGroundTicks() {
		return this.groundTime;
	}
	
	/**
	 * Gets the amount of ticks spent in air
	 * 
	 * @return ticks in air
	 */
	public int getAirTicks() {
		return this.airTime;
	}
	
	/**
	 * Gets the length of the current packet streak
	 * 
	 * @return length of the packet streak
	 */
	public int getPacketStreak() {
		return this.packetStreak;
	}
	
	/**
	 * Gets the lag factor
	 * 
	 * @return the lag factor
	 */
	public double getLagFactor() {
		return this.lagFactor;
	}
	
}
