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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RotLasher;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PoisonParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LotusSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotLasherSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class WandOfRegrowth extends Wand {

	{
		image = ItemSpriteSheet.WAND_REGROWTH;

		collisionProperties = Ballistica.STOP_TARGET | Ballistica.STOP_SOLID;
	}

	@Override
	protected void onZap(Ballistica bolt) {
		Sample.INSTANCE.play(Assets.Sounds.PLANT);

		Char cha = Actor.findChar(bolt.collisionPos);
		plantGrass(bolt.collisionPos);
		if(cha != null) {Buff.affect(cha, Vines.class).set(3f+((float)buffedLvl()/3), 2+buffedLvl());}
		else if (curCharges > 1 && Dungeon.level.passable[bolt.collisionPos]) {
			curCharges--;
			VineLasher vinelasher = new VineLasher(buffedLvl());
			vinelasher.pos = bolt.collisionPos;
			GameScene.add(vinelasher, 1f);
			Dungeon.level.occupyCell(vinelasher);
		}
		Dungeon.level.pressCell(bolt.collisionPos);

		/*for (int i : PathFinder.NEIGHBOURS9) {
			Char ch = Actor.findChar(bolt.collisionPos + i);
			if (ch != null) {
				processSoulMark(ch, chargesPerCast());
			}
		}*/

		ArrayList<Integer> positions = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS9){
			positions.add(i);
		}
		Random.shuffle( positions );
		int plants = 1 + buffedLvl()/2;
		for (int i : positions){
			if (plantGrass(bolt.collisionPos + i)){
				if (plants > 0) {
					plants--;
					Char ch = Actor.findChar(bolt.collisionPos + i);
					if (ch != null) {
						processSoulMark(ch, chargesPerCast());
					}
				}
				else {break;}
			}
		}
	}

	private boolean plantGrass(int cell){
		int t = Dungeon.level.map[cell];
		if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
				|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
				&& Dungeon.level.plants.get(cell) == null){
			Level.set(cell, Terrain.HIGH_GRASS);
			GameScene.updateMap(cell);
			CellEmitter.get( cell ).burst( LeafParticle.LEVEL_SPECIFIC, 4 );
			return true;
		}
		return false;
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(
				curUser.sprite.parent,
				MagicMissile.FOLIAGE_CONE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//like pre-nerf vampiric enchantment, except with herbal healing buff, only in grass
		boolean grass = false;
		int terr = Dungeon.level.map[attacker.pos];
		if (terr == Terrain.GRASS || terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS){
			grass = true;
		}
		terr = Dungeon.level.map[defender.pos];
		if (terr == Terrain.GRASS || terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS){
			grass = true;
		}

		if (grass) {
			int level = Math.max(0, staff.buffedLvl());

			// lvl 0 - 16%
			// lvl 1 - 21%
			// lvl 2 - 25%
			int healing = Math.round(damage * (level + 2f) / (level + 6f) / 2f);
			Buff.affect(attacker, Sungrass.Health.class).boost(healing);
		}

	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", 2+buffedLvl(), 2+buffedLvl());
		else
			return Messages.get(this, "stats_desc", 2, 2);
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( ColorMath.random(0x004400, 0x88CC44) );
		particle.am = 1f;
		particle.setLifespan(1f);
		particle.setSize( 1f, 1.5f);
		particle.shuffleXY(0.5f);
		float dst = Random.Float(11f);
		particle.x -= dst;
		particle.y += dst;
	}

	//Roots are based on a FlavourBuff class, so we need some copypasta here
	public static class Vines extends Buff implements Hero.Doom {

		protected float left;
		private int damage;

		private static final String LEFT	= "left";
		private static final String DAMAGE	= "damage";

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( LEFT, left );
			bundle.put( DAMAGE, damage );

		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			left = bundle.getFloat( LEFT );
			damage = bundle.getInt( DAMAGE );
		}

		public void set( float duration, int strength ) {
			this.left = Math.max(duration, left);
			this.damage = Math.max(strength, damage);
		}

		public void extend( float duration ) {
			this.left += duration;
		}

		@Override
		public int icon() {
			return BuffIndicator.ROOTS;
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String heroMessage() {
			return Messages.get(this, "heromsg");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", damage, dispTurns(left));
		}

		@Override
		public boolean attachTo( Char target ) {
			if (/*!target.flying &&*/ super.attachTo( target )) {
				target.rooted = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void detach() {
			target.rooted = false;
			super.detach();
		}

		@Override
		public boolean act() {
			if (target.isAlive()) {

				if(target.sprite.visible){
					CellEmitter.get( target.pos ).burst( LeafParticle.LEVEL_SPECIFIC, 4 );
					Sample.INSTANCE.play(Assets.Sounds.HIT);
				}
				target.damage( damage, this );
				spend( TICK );

				if ((left -= TICK) <= 0) {
					detach();
				}

			} else {

				detach();

			}

			return true;
		}

		@Override
		public void onDeath() {
			Dungeon.fail( getClass() );
			GLog.n(Messages.get(this, "ondeath"));
		}
	}

	public static class Dewcatcher extends Plant{

		{
			image = 13;
		}

		@Override
		public void activate( Char ch ) {

			int nDrops = Random.NormalIntRange(3, 6);

			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+i]
						&& pos+i != Dungeon.level.entrance
						&& pos+i != Dungeon.level.exit){
					candidates.add(pos+i);
				}
			}

			for (int i = 0; i < nDrops && !candidates.isEmpty(); i++){
				Integer c = Random.element(candidates);
				Dungeon.level.drop(new Dewdrop(), c).sprite.drop(pos);
				candidates.remove(c);
			}

		}

		//seed is never dropped, only care about plant class
		public static class Seed extends Plant.Seed {
			{
				plantClass = Dewcatcher.class;
			}
		}
	}

	public static class Seedpod extends Plant{

		{
			image = 14;
		}

		@Override
		public void activate( Char ch ) {

			int nSeeds = Random.NormalIntRange(2, 4);

			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+i]
						&& pos+i != Dungeon.level.entrance
						&& pos+i != Dungeon.level.exit){
					candidates.add(pos+i);
				}
			}

			for (int i = 0; i < nSeeds && !candidates.isEmpty(); i++){
				Integer c = Random.element(candidates);
				Dungeon.level.drop(Generator.random(Generator.Category.SEED), c).sprite.drop(pos);
				candidates.remove(c);
			}

		}

		//seed is never dropped, only care about plant class
		public static class Seed extends Plant.Seed {
			{
				plantClass = Seedpod.class;
			}
		}

	}

	public static class Lotus extends NPC {

		{
			alignment = Alignment.ALLY;
			properties.add(Property.IMMOVABLE);

			spriteClass = LotusSprite.class;

			viewDistance = 1;
		}

		private int wandLvl = 0;

		private void setLevel( int lvl ){
			wandLvl = lvl;
			HP = HT = 25 + 3*lvl;
		}

		public boolean inRange(int pos){
			return Dungeon.level.trueDistance(this.pos, pos) <= wandLvl;
		}

		public float seedPreservation(){
			return 0.40f + 0.04f*wandLvl;
		}

		@Override
		public boolean canInteract(Char c) {
			return false;
		}

		@Override
		protected boolean act() {
			super.act();

			if (--HP <= 0){
				destroy();
				sprite.die();
			}

			return true;
		}

		@Override
		public void damage( int dmg, Object src ) {
		}

		@Override
		public void add( Buff buff ) {
		}

		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
			GameScene.updateFog(pos, viewDistance+1);
		}

		@Override
		public boolean isInvulnerable(Class effect) {
			return true;
		}

		{
			immunities.add(Corruption.class);
			immunities.add(Doom.class);
		}

		@Override
		public String description() {
			return Messages.get(this, "desc", wandLvl, (int)(seedPreservation()*100), (int)(seedPreservation()*100) );
		}

		private static final String WAND_LVL = "wand_lvl";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WAND_LVL, wandLvl);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			wandLvl = bundle.getInt(WAND_LVL);
		}
	}

	public static class VineLasher extends NPC {
		{
			spriteClass = RotLasherSprite.class;

			HP = HT = 40;
			defenseSkill = 0;
			state = WANDERING = new Waiting();

			alignment = Alignment.ALLY;

			//before other mobs
			actPriority = MOB_PRIO + 1;
		}

		private int wandLevel;
		private static final String WAND_LEVEL = "wand_level";

		public VineLasher(int wandLvl){
			wandLevel = wandLvl;
			HP = HT = 12 + 4 * wandLevel;
		}

		public VineLasher(){}

		@Override
		protected boolean act() {
			super.act();
			HP -= (int)(HT * 0.1);
			if (HP <= 0){
				destroy();
				sprite.die();
			}
			return true;
		}

		@Override
		public void damage(int dmg, Object src) {
			if (src instanceof Burning) {
				destroy();
				sprite.die();
			} else {
				super.damage(dmg, src);
			}
		}

		@Override
		public boolean reset() {
			return true;
		}

		@Override
		protected boolean getCloser(int target) {
			return true;
		}

		@Override
		protected boolean getFurther(int target) {
			return true;
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(1, 2+wandLevel);
		}

		@Override
		public int attackSkill( Char target ) {
			return Dungeon.depth + 9;
		}

		@Override
		public int drRoll() {
			return Random.NormalIntRange(0, wandLevel);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WAND_LEVEL, wandLevel);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			wandLevel = bundle.getInt(WAND_LEVEL);
			HP = HT = 10 + 3 * wandLevel;
		}

		@Override
		public String description() {
			return Messages.get(this, "desc", wandLevel, 2+wandLevel);
		}

		{
			properties.add(Property.IMMOVABLE);
		}

		{
			immunities.add( ToxicGas.class );
			immunities.add( Corruption.class );
			immunities.add( Vines.class );
		}

		private class Waiting extends Mob.Wandering{}


	}

}
