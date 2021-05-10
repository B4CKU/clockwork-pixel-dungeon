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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ShamblerSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WraithSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Shambler extends NPC {

	private static final float SPAWN_DELAY	= 0f;
	
	private int level;
	
	{
		spriteClass = ShamblerSprite.class;
		
		HP = HT = 1;
		EXP = 0;

		maxLvl = -2;

		alignment = Alignment.ALLY;
		state = HUNTING;

		WANDERING = new Wandering();

		//before other mobs
		actPriority = MOB_PRIO + 1;

		properties.add(Property.UNDEAD);

	}
	
	private static final String LEVEL = "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
		adjustStats( level );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1 + level/2, 2 + level );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10 + level;
	}
	
	public void adjustStats( int level ) {
		this.level = level;
		defenseSkill = 5 + level;
		enemySeen = true;
	}

	{
		immunities.add( ToxicGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Burning.class );
		immunities.add( Corruption.class );
	}

	public static void spawnFew( int pos, int amount) {
		ArrayList<Integer> positions = new ArrayList<>();
		for (int i : PathFinder.NEIGHBOURS8){
			if(!Dungeon.level.solid[pos +i] && Actor.findChar( pos + i) == null && Dungeon.level.passable[pos+i])
			positions.add(i);
		}
		Random.shuffle( positions );
		int amountNow = 0;
		for (int n : positions) {
			if (spawnAt( pos + n ) != null) amountNow++;
			if (amountNow >= amount) break;
		}
	}
	
	public static Shambler spawnAt(int pos ) {
		if (!Dungeon.level.solid[pos] && Actor.findChar( pos ) == null) {
			
			Shambler w = new Shambler();
			w.adjustStats( Dungeon.depth );
			w.pos = pos;
			w.state = w.HUNTING;
			GameScene.add( w, SPAWN_DELAY );
			Dungeon.level.pressCell(w.pos);

			if (w.sprite.visible) {
				w.sprite.emitter().burst(Speck.factory(Speck.RATTLE), 5);
			}

			return w;
		} else {
			return null;
		}
	}

	private class Wandering extends Mob.Wandering{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV){
				die(null);
				return true;
			} else {
				return super.act(enemyInFOV, justAlerted);
			}
		}

	}
}
