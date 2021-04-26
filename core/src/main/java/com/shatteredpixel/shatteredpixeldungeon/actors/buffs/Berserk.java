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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class Berserk extends Buff {

	private enum State{
		NORMAL, BERSERK, RECOVERING
	}
	private State state = State.NORMAL;

	private static final float LEVEL_RECOVER_START = 2f;
	private float levelRecovery;
	
	private float power = 0;

	private static final String STATE = "state";
	private static final String LEVEL_RECOVERY = "levelrecovery";
	private static final String POWER = "power";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(STATE, state);
		bundle.put(POWER, power);
		if (state == State.RECOVERING) bundle.put(LEVEL_RECOVERY, levelRecovery);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		state = bundle.getEnum(STATE, State.class);
		power = bundle.getFloat(POWER);
		if (state == State.RECOVERING) levelRecovery = bundle.getFloat(LEVEL_RECOVERY);
	}

	@Override
	public boolean act() {
		if (berserking()){
			ShieldBuff buff = target.buff(KnightShield.class);
			if (target.HP <= 0) {
				int dmg = 1 + (int)Math.ceil(target.shielding() * 0.05f);
				if (buff != null && buff.shielding() > 0) {
					buff.absorbDamage(dmg);
				} else {
					//if there is no shield buff, or it is empty, then try to remove from other shielding buffs
					for (ShieldBuff s : target.buffs(ShieldBuff.class)){
						dmg = s.absorbDamage(dmg);
						if (dmg == 0) break;
					}
				}
				if (target.shielding() <= 0) {
					target.die(this);
					if (!target.isAlive()) Dungeon.fail(this.getClass());
				}
			} else {
				state = State.RECOVERING;
				levelRecovery = LEVEL_RECOVER_START;
				if (Dungeon.hero.pointsInTalent(Talent.BERSERKING_DETERMINATION)==2) levelRecovery = levelRecovery / 2f;
				if (buff != null) buff.absorbDamage(buff.shielding());
				if (((Hero)target).pointsInTalent(Talent.BERSERKING_DETERMINATION) < 2)
					power = 0f;
			}
		} else if (state == State.NORMAL) {
			power -= GameMath.gate(0.1f, power, 1f) * 0.067f * Math.pow((target.HP/(float)target.HT), 2);
			
			if (power <= 0){
				detach();
			}
		}
		spend(TICK);
		return true;
	}

	public float rageAmount(){
		return Math.min(1f, power);
	}

	public int damageFactor(int dmg){
		float bonus = Math.min(1.5f, 1f + (power / 2f));
		return Math.round(dmg * bonus);
	}

	public boolean berserking(){
		if (target.HP == 0 && state == State.NORMAL && power >= 0.8f && Dungeon.hero.hasTalent(Talent.BERSERKING_DETERMINATION)){

			KnightShield shield = target.buff(KnightShield.class);
			if (shield != null){
				state = State.BERSERK;
				//int shieldAmount = shield.maxShield() * 8;
				int shieldAmount = Math.round(target.HT * 0.67f);
				//shieldAmount = Math.round(shieldAmount * (1f + Dungeon.hero.pointsInTalent(Talent.BERSERKING_STAMINA)/6f));
				shield.supercharge(shieldAmount);

				SpellSprite.show(target, SpellSprite.BERSERK);
				Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
				GameScene.flash(0xFF0000);
			}

		}

		return state == State.BERSERK && target.shielding() > 0;
	}
	
	public void damage(int damage){
		if (state == State.RECOVERING && ((Hero)target).pointsInTalent(Talent.BERSERKING_DETERMINATION) < 2) return;
		power = Math.min(1f, power + (damage/(float)target.HT)/3f);
		BuffIndicator.refreshHero(); //show new power immediately
	}

	public void recover(float percent){
		if (levelRecovery > 0){
			levelRecovery -= percent;
			if (levelRecovery <= 0) {
				state = State.NORMAL;
				levelRecovery = 0;
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BERSERK;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (state){
			case NORMAL: default:
				if (power < 0.8f) icon.hardlight(1f, 0.5f, 0f);
				else            icon.hardlight(1f, 0f, 0f);
				break;
			case BERSERK:
				icon.hardlight(1f, 0f, 0f);
				break;
			case RECOVERING:
				icon.hardlight(0, 0, 1f);
				break;
		}
	}
	
	@Override
	public float iconFadePercent() {
		switch (state){
			case NORMAL: default:
				return Math.max(0f, 1f - power);
			case BERSERK:
				return 0f;
			case RECOVERING:
				return 1f - levelRecovery/LEVEL_RECOVER_START;
		}
	}

	@Override
	public String toString() {
		switch (state){
			case NORMAL: default:
				return Messages.get(this, "angered");
			case BERSERK:
				return Messages.get(this, "berserk");
			case RECOVERING:
				return Messages.get(this, "recovering");
		}
	}

	@Override
	public String desc() {
		String desc = "";
		float dispDamage = (damageFactor(10000) / 100f) - 100f;
		switch (state){
			case NORMAL: default:
				desc += Messages.get(this, "angered_desc");
				if (Dungeon.hero.hasTalent(Talent.BERSERKING_DETERMINATION))
					desc += " " + Messages.get(this, "determination");
				desc += "\n\n" + Messages.get(this, "stats", Math.floor(power * 100f), dispDamage );
				break;
			case BERSERK:
				desc += Messages.get(this, "berserk_desc");
				break;
			case RECOVERING:
				desc += Messages.get(this, "recovering_desc", levelRecovery);
				if (Dungeon.hero.pointsInTalent(Talent.BERSERKING_DETERMINATION) == 2)
					desc += "\n\n" + Messages.get(this, "stats", Math.floor(power * 100f), dispDamage );
				else
					desc += "\n\n" + Messages.get(this, "determination_exhaust");
				break;
		}
		return desc;
	}
}
