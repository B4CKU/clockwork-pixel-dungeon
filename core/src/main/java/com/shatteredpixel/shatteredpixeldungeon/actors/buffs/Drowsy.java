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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import com.watabou.utils.Bundle;

public class Drowsy extends Buff {

	public static final float DURATION = 5f;

	{
		type = buffType.NEUTRAL;
		announced = true;
	}

	//whether the target gets affected with regular sleep or deep magical sleep at the end of the debuff
	public boolean regularSleep = false;

	@Override
	public int icon() {
		return BuffIndicator.DROWSY;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	public boolean attachTo(Char target ) {
		if (!target.isImmune(Sleep.class) && super.attachTo(target)) {
			if (cooldown() == 0) {
				spend(DURATION);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean act(){
		if (regularSleep) ((Mob) target).state = ((Mob) target).SLEEPING;
		else Buff.affect(target, MagicalSleep.class);

		detach();
		return true;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		String desc =  Messages.get(this, "desc", dispTurns(visualcooldown()));
		if(regularSleep) desc += "\n\n" + Messages.get(this, "regular");
		else desc += "\n\n" + Messages.get(this, "magical");

		return desc;
	}

	private static final String REGULAR_SLEEP = "regular_sleep";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( REGULAR_SLEEP, regularSleep );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		regularSleep = bundle.getBoolean(REGULAR_SLEEP);
	}

}
