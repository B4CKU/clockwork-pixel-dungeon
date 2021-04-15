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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LotusSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RotLasherSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

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
		//..cripples enemies hit..
		if(cha != null) {
			Buff.prolong( cha, Cripple.class, 8f);
			processSoulMark(cha, chargesPerCast());
		}
		//..or spawns a vine lasher if there is none and the ground is suitable..
		else if (Dungeon.level.passable[bolt.collisionPos]) {
			VineLasher vinelasher = new VineLasher(buffedLvl());
			vinelasher.pos = bolt.collisionPos;
			GameScene.add(vinelasher, 0f);
			Dungeon.level.occupyCell(vinelasher);
			//CellEmitter.get( vinelasher.pos ).burst( LeafParticle.GENERAL, 6 );
			vinelasher.sprite.emitter().burst(LeafParticle.GENERAL, 6);
		}
		//We don't want the lasher inside the grass and also it could be used for triggering traps
		Dungeon.level.pressCell(bolt.collisionPos);

		//gets 8 nearby cells, shuffles them and plants up to 1+(lvl/2) grass patches around
		//marks mobs that had grass grown under them
		ArrayList<Integer> positions = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8){
			positions.add(i);
		}
		Random.shuffle( positions );
		int plants = 1 + (buffedLvl()/2);
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
		//gives target a single turn of cripple, allowing lashers to attack them and gives adrenaline to the plants, so that they attack faster
		Buff.affect(defender, Cripple.class, 1f);

		for (Char ch : Actor.chars()){
			if (ch instanceof VineLasher){
				//if a lasher doesn't have adrenaline, apply a single turn of it, but without announcing (because how spammy it is)
				Adrenaline a = ch.buff(Adrenaline.class);
				if (a == null){
					new Adrenaline(){
					{announced = false;}
					public static final float DURATION = 1f;
				}.attachTo(ch);
				//i hate that solution
				}

				ch.sprite.emitter().burst(LeafParticle.GENERAL, 3);
			}
		}

	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", 1+buffedLvl(), 4+2*buffedLvl());
		else
			return Messages.get(this, "stats_desc", 1, 4);
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
			state = WANDERING;

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
			damage( Math.max( 1, (int)(HT * 0.05)), this);
			if (!isAlive()) return true;
			return super.act();
		}

		@Override
		public void damage(int dmg, Object src) {
			if (src instanceof Burning) {
				die(this);
			} else {
				super.damage(dmg, src);
			}
		}

		@Override
		protected boolean getCloser(int target) {
			return false;
		}

		@Override
		protected boolean getFurther(int target) {
			return false;
		}

		public int minDmg(int lvl){
			return 1+lvl;
		}

		public int maxDmg(int lvl){
			return 4+2*lvl;
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(minDmg(wandLevel), maxDmg(wandLevel));
		}

		//increases the duration of roots and cripple and if the target has more than 10 turns of cripple, roots them
		@Override
		public int attackProc(Char enemy, int damage) {
			damage = super.attackProc( enemy, damage );
			if (enemy.buff(Roots.class) != null) {
				Buff.affect( enemy, Roots.class, 1f );
			}
			else {
				Cripple cripple = enemy.buff(Cripple.class);
				if (cripple != null && cripple.cooldown() > 10f) {
					Buff.affect( enemy, Roots.class, Cripple.DURATION);
					Buff.detach( enemy, Cripple.class );
				}
				else Buff.affect( enemy, Cripple.class, 1f);
			}


			return super.attackProc(enemy, damage);
		}

		//basically hero accuracy given the hero levels up once every depth
		@Override
		public int attackSkill( Char target ) {
			return Dungeon.depth + 9;
		}

		@Override
		public int drRoll() {
			return Random.NormalIntRange(0, wandLevel);
		}

		//can attack enemies within two tiles range, but only if they are stunned, crippled or paralyzed
		@Override
		protected boolean canAttack(Char enemy) {
			return (enemy.buff(Cripple.class) != null || enemy.buff(Paralysis.class) != null || enemy.buff(Roots.class) != null) && (fieldOfView[enemy.pos] && Dungeon.level.distance(pos, enemy.pos) <= 2);
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
			//HP = HT = 10 + 3 * wandLevel;
		}

		@Override
		public boolean canInteract(Char c) {
			return true;
		}

		@Override
		public boolean interact( Char c ) {
			if (c != Dungeon.hero){
				return true;
			}
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new WndOptions( Messages.get(VineLasher.this, "dismiss_title"),
							Messages.get(VineLasher.this, "dismiss_body"),
							Messages.get(VineLasher.this, "dismiss_confirm"),
							Messages.get(VineLasher.this, "dismiss_cancel") ){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								die(null);
							}
						}
					});
				}
			});
			return true;
		}

		@Override
		public String description() {
			return Messages.get(this, "desc", minDmg(wandLevel), maxDmg(wandLevel));
		}

		{
			properties.add(Property.IMMOVABLE);
		}

		{
			immunities.add( ToxicGas.class );
			immunities.add( Corruption.class );
		}
	}
}
