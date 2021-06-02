/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class AlchemicalAura extends Buff {
	{
		type = buffType.POSITIVE;
		announced = false;
	}

	private int energy = 0;
	private float partialEnergy = 0;

	public void gainEnergy(float levelPortion) {
		int energyCap = (target instanceof Hero ? ((Hero)target).lvl : 0);

		float energyGain = levelPortion * 6;

		if (energy < energyCap) {

			partialEnergy += energyGain;

			while (partialEnergy >= 1) {

				energy++;
				partialEnergy -= 1;

				if (energy == energyCap){
					partialEnergy = 0;
				}

			}
		} else {
			partialEnergy = 0;
		}

	}

	public static int heroGetEnergy() {
		if(Dungeon.hero != null) {
			AlchemicalAura aura = Dungeon.hero.buff(AlchemicalAura.class);
			if (aura != null) {
				return aura.getEnergy();
			}
		}
		return 0;
	}

	public int getEnergy() {
		return energy;
	}

	public static int heroUseEnergy( int energyUsed) {
		if(Dungeon.hero != null) {
			AlchemicalAura aura = Dungeon.hero.buff(AlchemicalAura.class);
			if (aura != null) {
				return aura.useEnergy(energyUsed);
			}
		}
		return energyUsed;
	}

	public int useEnergy(int energyUsed) {
		int energyRequired = Math.max(0, energyUsed - energy);
		energy = Math.max(0, energy - energyUsed);
		return energyRequired;
	}

	@Override
	public int icon() {
		return BuffIndicator.SACRIFICE;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", energy);
	}

	private static final String ENERGY = "energy";
	private static final String PARTIALENERGY = "partialenergy";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( ENERGY, energy );
		bundle.put( PARTIALENERGY, partialEnergy );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		energy = bundle.getInt(ENERGY);
		partialEnergy = bundle.getFloat(PARTIALENERGY);
	}
}