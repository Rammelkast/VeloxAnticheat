/**
 * Velox Anticheat | Simple, stable and accurate anticheat
 * Copyright (C) 2021-2022 Marco Moesman ("Rammelkast")
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rammelkast.veloxanticheat.utils;

import java.util.List;
import org.bukkit.ChatColor;

/**
 * @author Elevated (from Frequency)
 */
public final class GraphLib {

	public static class GraphResult {
		private final String graph;
		private final int positives, negatives;

		public GraphResult(final String graph, final int positives, final int negatives) {
			this.graph = graph;
			this.positives = positives;
			this.negatives = negatives;
		}

		public String getGraph() {
			return this.graph;
		}

		public int getPositives() {
			return this.positives;
		}

		public int getNegatives() {
			return this.negatives;
		}
	}

	public static GraphResult getGraph(final List<Double> values) {
		final StringBuilder graph = new StringBuilder();
		double largest = 0;

		for (final double value : values) {
			if (value > largest) {
				largest = value;
			}
		}

		int GRAPH_HEIGHT = 2;
		int positives = 0, negatives = 0;

		for (int i = GRAPH_HEIGHT - 1; i > 0; i -= 1) {
			final StringBuilder builder = new StringBuilder();
			for (final double index : values) {
				final double value = GRAPH_HEIGHT * index / largest;
				if (value > i && value < i + 1) {
					++positives;
					builder.append(String.format("%s+", ChatColor.GREEN));
				} else {
					++negatives;
					builder.append(String.format("%s-", ChatColor.RED));
				}
			}
			graph.append(builder.toString());
		}

		return new GraphResult(graph.toString(), positives, negatives);
	}
}