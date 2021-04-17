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

package com.shatteredpixel.shatteredpixeldungeon.items.amulets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EarthGuardianSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class BoneAmulet extends SummoningItem {
    {
        image = ItemSpriteSheet.ARTIFACT_ROSE1;
        summonedAllyClass = Wolf.class;
    }

    public static class Wolf extends SummonedAlly {
        {
            spriteClass = EarthGuardianSprite.class;

            HP = HT = 20;
            baseSpeed = 2f;

        }

        @Override
        public void updateItem(){
            //same as the hero
            defenseSkill = (Dungeon.hero.lvl+4);
            HT = 20 + 5 * Dungeon.depth;
        }

        @Override
        public int attackSkill(Char target) {
            //same as the hero
            return 2*defenseSkill + 5;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(2, 4 + Dungeon.depth/2);
        }

        @Override
        public int drRoll() {
            return Random.NormalIntRange(0, Dungeon.depth/4);
        }
    }

}
