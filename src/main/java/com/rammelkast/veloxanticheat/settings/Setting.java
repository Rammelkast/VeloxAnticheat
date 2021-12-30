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
/**
 * 
 */
package com.rammelkast.veloxanticheat.settings;

public final class Setting {
	
    private final String name;
    private final SettingType type;
    private Object value;
	
	public Setting(final String name, final SettingType type) {
        this.name = name;
        this.type = type;
    }

	public String getName() {
		return this.name;
	}
	
	public SettingType getType() {
		return this.type;
	}
	
	public void setValue(final Object value) {
		this.value = value;
	}
	
    public boolean getBoolean() {
        return (boolean) this.value;
    }

    public double getDouble() {
        return (double) this.value;
    }

    public int getInt() {
        return (int) this.value;
    }

    public long getLong() {
        return (long) this.value;
    }

    public String getString() {
        return (String) this.value;
    }
	
	public enum SettingType {
        INTEGER,
        DOUBLE,
        BOOLEAN,
        STRING,
        LONG
    }
	
}