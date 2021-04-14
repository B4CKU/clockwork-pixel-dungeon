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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Monk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClockworkMinionSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TinkerersBag extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_BAG;

		exp = 0;
		levelCap = 4;

		charge = Math.min(level()+4, 10);
		partialCharge = 0;
		chargeCap = Math.min(level()+4, 10);

		defaultAction = AC_USE;

		unique = true;
		bones = false;
	}

	private TinkerersBag.ClockworkMinion minion = null;

	public static final String AC_USE = "USE";

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 0 && !cursed)
			actions.add(AC_USE);
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals( AC_USE )) {
			if (!isEquipped(hero)) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (cursed)       GLog.i( Messages.get(this, "cursed") );
			else if (charge <= 0)  GLog.i( Messages.get(this, "no_charge") );
			else {
				GameScene.selectCell(minionDirector);
			}
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new bagRecharge();
	}

	@Override
	public void charge(Hero target, float amount) {
		if (charge < chargeCap){
			partialCharge += 0.25f*amount;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				updateQuickslot();
			}
		}
	}


	@Override
	public Item upgrade() {
		chargeCap = Math.min(chargeCap + 1, 10);
		return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped(Dungeon.hero)) {
			desc += "\n\n" + Messages.get(this, "stats", level(), (int)Math.round(Math.pow(2,level()+1)), level(), (int)Math.round(Math.pow(2,level())), ((2 * level() + 1) * 8));
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
		}
		return desc;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
	}

	@Override
	public int value() {
		return 0;
	}

	public class bagRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			//LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed /*&& (lock == null || lock.regenOn())*/) {
				float chargeGain = 1 / (10f + (40f * (float)Math.pow(0.875f,(chargeCap - charge))));
				chargeGain *= RingOfEnergy.artifactChargeMultiplier(target);
				partialCharge += chargeGain;

				if (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

			updateQuickslot();

			spend( TICK );

			return true;
		}
	}

	public CellSelector.Listener minionDirector = new CellSelector.Listener(){

		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;


			//dodać naprawę minionów gdy uzyje sie tego wręcz
			summonMinion(cell, curUser);


		}

		@Override
		public String prompt() {
			return  Messages.get(TinkerersBag.class, "prompt");
		}
	};

	private void summonMinion(Integer dest, final Hero hero){



		final Ballistica minion_ballistica = new Ballistica(curUser.pos, dest, Ballistica.STOP_TARGET);
		Integer destpos = minion_ballistica.path.get(1);

		if ((Actor.findChar(destpos) == null && (Dungeon.level.passable[destpos] || Dungeon.level.avoid[destpos]))) {
			minion = new TinkerersBag.ClockworkMinion();
			minion.pos = destpos;
			minion.setInfo(hero, level());
			minion.defendingPos = dest;

			GameScene.add(minion, 1f);
			Dungeon.level.occupyCell(minion);
			Buff.affect( minion, ClockworkMinion.Charge.class );

			Sample.INSTANCE.play(Assets.Sounds.EVOKE);
			minion.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 10);
			hero.sprite.operate(hero.pos);
			hero.spend( 1f );
			hero.busy();

			Talent.onArtifactUsed(hero);
			charge--;
			updateQuickslot();

		} else
			GLog.i( Messages.get(this, "no_space") );
	}

	public static class ClockworkMinion extends NPC {

		{
			spriteClass = ClockworkMinionSprite.class;

			alignment = Alignment.ALLY;
			state = HUNTING;
			//intelligentAlly = true;

			viewDistance = 1;

			//before other mobs
			actPriority = MOB_PRIO + 1;

			HP = HT = 0;

			WANDERING = new Wandering();

			properties.add(Property.ELECTRIC);
			properties.add(Property.INORGANIC);

		}

		private int bagLevel = -1;
		private int defendingPos = -1;

		@Override
		protected boolean act() {
			Charge f = buff(Charge.class);
			f.duration --;
			if (!f.powered) {
				if(f.duration > 0) {
					spend(TICK);
				} else {
					die(null);
				}
				return true;
			}
			if (f.duration < 0) {
				f.powered = false;
				f.duration = 12f;
				sprite.add(CharSprite.State.PARALYSED);
			}
			return super.act();
		}

		private void setInfo(Hero hero, int bagLevel){
			if (bagLevel > this.bagLevel) {
				this.bagLevel = bagLevel;
				HT = (2 * bagLevel + 1) * 8;
			}
			HP = HT;
			//half of hero's evasion
			defenseSkill = (hero.lvl + 4)/2;
		}

		@Override
		public int attackSkill(Char target) {
			//same as the hero
			return 2*defenseSkill + 5;
		}

		@Override
		public int attackProc(Char enemy, int damage) {
			if (enemy instanceof Mob) ((Mob)enemy).aggro(this);
			return super.attackProc(enemy, damage);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(bagLevel, (int)Math.round(Math.pow(2,bagLevel+1)));
		}

		@Override
		public int drRoll() {
			return Random.NormalIntRange(bagLevel, (int)Math.round(Math.pow(2,bagLevel)));
		}

		@Override
		public String description() {
			return Messages.get(this, "desc");
		}

		{
			immunities.add( Corruption.class );
		}

		private static final String BAGLEVEL = "baglevel";
		private static final String DEFENDINGPOS = "defendingpos";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(BAGLEVEL, bagLevel);
			bundle.put(DEFENDINGPOS, defendingPos);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			bagLevel= bundle.getInt(BAGLEVEL);
			defendingPos = bundle.getInt(DEFENDINGPOS);
		}

		private class Wandering extends Mob.Wandering {

			@Override
			public boolean act( boolean enemyInFOV, boolean justAlerted ) {
				if ( enemyInFOV ) {

					enemySeen = true;

					notice();
					alerted = true;
					state = HUNTING;
					target = enemy.pos;

				} else {

					enemySeen = false;

					int oldPos = pos;
					target = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
					//always move towards the hero when wandering
					if (getCloser( target )) {
						//moves 2 tiles at a time when returning to the hero
						if (defendingPos == -1 && !Dungeon.level.adjacent(target, pos)){
							getCloser( target );
						}
						spend( 1 / speed() );
						if (pos == defendingPos);//tu coś będzie
						return moveSprite( oldPos, pos );
					} else {
						spend( TICK );
					}

				}
				return true;
			}

		}

		public static class Charge extends Buff {

			private float duration = 12f;
			private boolean powered = true;

			@Override
			public int icon() {
				return BuffIndicator.RECHARGING;
			}

			@Override
			public void tintIcon(Image icon) {
				if(powered) icon.hardlight(1, 1, 0);
				else icon.hardlight(1, 0, 0);
			}

			@Override
			public String toString() {
				return Messages.get(this, "name");
			}

			@Override
			public String desc() {
				String desc = Messages.get(this, "desc", duration);
				if(powered) desc  += "\n\n" + Messages.get(this, "powered", duration);
				else desc += "\n\n" + Messages.get(this, "unpowered", duration);
				return desc;

			}

			private static final String DURATION	= "duration";
			private static final String POWERED     = "powered";

			@Override
			public void storeInBundle( Bundle bundle ) {
				super.storeInBundle( bundle );
				bundle.put( DURATION, duration );
				bundle.put( POWERED, powered );
			}

			@Override
			public void restoreFromBundle( Bundle bundle ) {
				super.restoreFromBundle( bundle );
				duration = bundle.getInt( DURATION );
				powered = bundle.getBoolean( POWERED );
			}

		}
	}
}
