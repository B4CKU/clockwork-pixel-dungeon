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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class KnightShield extends ShieldBuff {

	//yoinked from the warrior seal
	private float partialShield;

	@Override
	public synchronized boolean act() {
		if (shielding() < maxShield()) {
			partialShield += 1/30f;
		}

		while (partialShield >= 1){
			incShield();
			partialShield--;
		}

		if (shielding() <= 0 && maxShield() <= 0){
			detach();
		}

		spend(TICK);
		return true;
	}

	public synchronized void supercharge(int maxShield){
		if (maxShield > shielding()){
			setShield(maxShield);
		}
	}

	public synchronized int maxShield() {
		return ((Hero) target).lvl;
	}

	@Override
	//logic edited slightly as buff should not detach
	public int absorbDamage(int dmg) {
		if (shielding() <= 0) return dmg;

		if (shielding() >= dmg){
			decShield(dmg);
			dmg = 0;
		} else {
			dmg -= shielding();
			decShield(shielding());
		}
		return dmg;
	}
}