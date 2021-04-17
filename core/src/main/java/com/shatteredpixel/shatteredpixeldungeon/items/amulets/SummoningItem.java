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


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class SummoningItem extends Item {
    {
        stackable = false;
        defaultAction = AC_SUMMON;
    }

    protected Buff passiveBuff;

    //the current charge
    protected int charge = 100;
    //the build towards next charge, usually rolls over at 1.
    //better to keep charge as an int and use a separate float than casting.
    protected float partialCharge = 0;
    //the maximum charge
    protected int chargeCap = 100;

    //used by some artifacts to keep track of duration of effects or cooldowns to use.
    protected int cooldown = 0;

    protected Class<? extends SummonedAlly> summonedAllyClass = SummonedAlly.class;

    private SummonedAlly summon = null;
    private int summonID = 0;

    public static final String AC_SUMMON = "SUMMON";
    public static final String AC_DIRECT = "DIRECT";

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && charge == chargeCap && !cursed && summonID == 0) {
            actions.add(AC_SUMMON);
        }
        if (summonID != 0){
            actions.add(AC_DIRECT);
        }
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute(hero, action);

        if (action.equals(AC_SUMMON)) {

            if (curUser.buff(MagicImmune.class) != null)   GameScene.show(new WndUseItem(null, this));
            else if (summon != null)                        GLog.i( Messages.get(this, "spawned") );
            else if (cooldown > 0)                         GLog.i( Messages.get(this, "cooldown") );
            else if (charge != chargeCap)                  GLog.i( Messages.get(this, "no_charge") );
            else {
                ArrayList<Integer> spawnPoints = new ArrayList<>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = hero.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
                        spawnPoints.add(p);
                    }
                }

                if (spawnPoints.size() > 0) {
                    try {summon = (SummonedAlly)summonedAllyClass.newInstance();}
                    catch(Exception e) {throw new RuntimeException("Attempted to summon an abstract mob!");}

                    summon.updateItem(this);

                    summonID = summon.id();
                    summon.pos = Random.element(spawnPoints);

                    GameScene.add(summon, 1f);
                    Dungeon.level.occupyCell(summon);

                    CellEmitter.get(summon.pos).start( ShaftParticle.FACTORY, 0.3f, 4 );
                    CellEmitter.get(summon.pos).start( Speck.factory(Speck.LIGHT), 0.2f, 3 );

                    hero.spend(1f);
                    hero.busy();
                    hero.sprite.operate(hero.pos);

                    if (BossHealthBar.isAssigned()) {
                        summon.sayBoss();
                    } else {
                        summon.sayAppeared();
                    }

                    charge = 0;
                    partialCharge = 0;
                    updateQuickslot();

                } else
                    GLog.i( Messages.get(this, "no_space") );
            }

        } else if (action.equals(AC_DIRECT)){
            if (summon == null && summonID != 0){
                Actor a = Actor.findById(summonID);
                if (a != null){
                    summon = (SummonedAlly) a;
                } else {
                    summonID = 0;
                }
            }
            if (summon != null) GameScene.selectCell(ghostDirector);

        }
    }

    @Override
    public boolean collect( Bag container ) {
        if (super.collect(container)){
            passiveBuff = passiveBuff();
            passiveBuff.attachTo((Hero) container.owner);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected void onDetach() {
        if (passiveBuff != null){
            passiveBuff.detach();
            passiveBuff = null;
        }
    }

    protected SummoningItemBuff passiveBuff() {
        return null;
    }

    public void charge(Hero target, float amount){
        //do nothing by default;
    }

    public class SummoningItemBuff extends Buff {

        public void charge(Hero target, float amount){
            SummoningItem.this.charge(target, amount);
        }

        @Override
        public boolean act() {

            spend( TICK );

            if (summon == null && summonID != 0){
                Actor a = Actor.findById(summonID);
                if (a != null){
                    summon = (SummonedAlly) a;
                } else {
                    summonID = 0;
                }
            }

            //rose does not charge while ghost hero is alive
            if (summon != null){
                defaultAction = AC_DIRECT;

                //heals to full over 1000 turns
                LockedFloor lock = target.buff(LockedFloor.class);
                if (summon.HP < summon.HT && (lock == null || lock.regenOn())) {
                    partialCharge += (summon.HT / 1000f) * RingOfEnergy.artifactChargeMultiplier(target);
                    updateQuickslot();

                    if (partialCharge > 1) {
                        summon.HP++;
                        partialCharge--;
                    }
                } else {
                    partialCharge = 0;
                }

                return true;
            } else {
                defaultAction = AC_SUMMON;
            }

            LockedFloor lock = target.buff(LockedFloor.class);
            if (charge < chargeCap && (lock == null || lock.regenOn())) {
                //500 turns to a full charge
                partialCharge += (1/5f * RingOfEnergy.artifactChargeMultiplier(target));
                if (partialCharge > 1){
                    charge++;
                    partialCharge--;
                    if (charge == chargeCap){
                        partialCharge = 0f;
                        GLog.p( Messages.get(this, "charged") );
                    }
                }
            }

            updateQuickslot();

            return true;
        }
    }

    @Override
    public String status() {
        if (summon == null && summonID != 0){
            try {
                Actor a = Actor.findById(summonID);
                if (a != null) {
                    summon = (SummonedAlly) a;
                } else {
                    summonID = 0;
                }
            } catch ( ClassCastException e ){
                ShatteredPixelDungeon.reportException(e);
                summonID = 0;
            }
        }
        if (summon == null){
            //display the current cooldown
            if (cooldown != 0)  return Messages.format( "%d", cooldown );
            //display as percent
            else                return Messages.format( "%d%%", charge );
        } else {
            return (int)((summon.HP+partialCharge)*100) / summon.HT + "%";
        }
    }

    @Override
    public int value() {
        return 100;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    private static final String CHARGE = "charge";
    private static final String PARTIALCHARGE = "partialcharge";

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle(bundle);
        bundle.put( CHARGE , charge );
        bundle.put( PARTIALCHARGE , partialCharge );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        charge = bundle.getInt( CHARGE );
        partialCharge = bundle.getFloat( PARTIALCHARGE );
    }

    public CellSelector.Listener ghostDirector = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;

            Sample.INSTANCE.play( Assets.Sounds.GHOST );

            if (!Dungeon.level.heroFOV[cell]
                    || Actor.findChar(cell) == null
                    || (Actor.findChar(cell) != Dungeon.hero && Actor.findChar(cell).alignment != Char.Alignment.ENEMY)){
                summon.yell(Messages.get(summon, "directed_position_" + Random.IntRange(1, 5)));
                summon.aggro(null);
                summon.state = summon.WANDERING;
                summon.defendingPos = cell;
                summon.movingToDefendPos = true;
                return;
            }

            if (summon.fieldOfView == null || summon.fieldOfView.length != Dungeon.level.length()){
                summon.fieldOfView = new boolean[Dungeon.level.length()];
            }
            Dungeon.level.updateFieldOfView(summon, summon.fieldOfView );

            if (Actor.findChar(cell) == Dungeon.hero){
                summon.yell(Messages.get(summon, "directed_follow_" + Random.IntRange(1, 5)));
                summon.aggro(null);
                summon.state = summon.WANDERING;
                summon.defendingPos = -1;
                summon.movingToDefendPos = false;

            } else if (Actor.findChar(cell).alignment == Char.Alignment.ENEMY){
                summon.yell(Messages.get(summon, "directed_attack_" + Random.IntRange(1, 5)));
                summon.aggro(Actor.findChar(cell));
                summon.setTarget(cell);
                summon.movingToDefendPos = false;

            }
        }

        @Override
        public String prompt() {
            return  "\"" + Messages.get(SummonedAlly.class, "direct_prompt") + "\"";
        }
    };

    public static class SummonedAlly extends NPC {

        {
            alignment = Alignment.ALLY;
            intelligentAlly = true;
            WANDERING = new Wandering();

            state = HUNTING;

            //before other mobs
            actPriority = MOB_PRIO + 1;
        }

        private SummoningItem item = null;

        public SummonedAlly(){
            super();
            updateItem();
        }

        public void updateItem(SummoningItem sumItem){
            item = sumItem;
            this.updateItem();
        }

        public void updateItem(){}

        private int defendingPos = -1;
        private boolean movingToDefendPos = false;

        public void clearDefensingPos(){
            defendingPos = -1;
            movingToDefendPos = false;
        }

        @Override
        protected boolean act() {
            updateItem();
            if (item == null){
                damage(1, this);
            }

            if (!isAlive())
                return true;
            if (!Dungeon.hero.isAlive()){
                sayHeroKilled();
                sprite.die();
                destroy();
                return true;
            }
            return super.act();
        }

        @Override
        protected Char chooseEnemy() {
            Char enemy = super.chooseEnemy();

            int targetPos = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;

            //will never attack something far from their target
            if (enemy != null
                    && Dungeon.level.mobs.contains(enemy)
                    && (Dungeon.level.distance(enemy.pos, targetPos) <= 8)){
                return enemy;
            }

            return null;
        }

        @Override
        public void damage(int dmg, Object src) {
            super.damage( dmg, src );
            //for the status indicator
            Item.updateQuickslot();
        }

        private void setTarget(int cell) {
            target = cell;
        }

        @Override
        public void die(Object cause) {
            sayDefeated();
            super.die(cause);
        }

        @Override
        public void destroy() {
            updateItem();
            if (item != null) {
                item.summon = null;
                item.charge = 0;
                item.partialCharge = 0;
                item.summonID = -1;
                item.defaultAction = AC_SUMMON;
            }
            super.destroy();
        }

        public void sayAppeared(){
            /*int depth = (Dungeon.depth - 1) / 5;

            //only some lines are said on the first floor of a depth
            int variant = Dungeon.depth % 5 == 1 ? Random.IntRange(1, 3) : Random.IntRange(1, 6);

            switch(depth){
                case 0:
                    yell( Messages.get( this, "dialogue_sewers_" + variant ));
                    break;
                case 1:
                    yell( Messages.get( this, "dialogue_prison_" + variant ));
                    break;
                case 2:
                    yell( Messages.get( this, "dialogue_caves_" + variant ));
                    break;
                case 3:
                    yell( Messages.get( this, "dialogue_city_" + variant ));
                    break;
                case 4: default:
                    yell( Messages.get( this, "dialogue_halls_" + variant ));
                    break;
            }
            if (ShatteredPixelDungeon.scene() instanceof GameScene) {
                Sample.INSTANCE.play( Assets.Sounds.GHOST );
            }*/
        }

        public void sayBoss(){
            /*int depth = (Dungeon.depth - 1) / 5;

            switch(depth){
                case 0:
                    yell( Messages.get( this, "seen_goo_" + Random.IntRange(1, 3) ));
                    break;
                case 1:
                    yell( Messages.get( this, "seen_tengu_" + Random.IntRange(1, 3) ));
                    break;
                case 2:
                    yell( Messages.get( this, "seen_dm300_" + Random.IntRange(1, 3) ));
                    break;
                case 3:
                    yell( Messages.get( this, "seen_king_" + Random.IntRange(1, 3) ));
                    break;
                case 4: default:
                    yell( Messages.get( this, "seen_yog_" + Random.IntRange(1, 3) ));
                    break;
            }
            Sample.INSTANCE.play( Assets.Sounds.GHOST );*/
        }

        public void sayDefeated(){
            /*if (BossHealthBar.isAssigned()){
                yell( Messages.get( this, "defeated_by_boss_" + Random.IntRange(1, 3) ));
            } else {
                yell( Messages.get( this, "defeated_by_enemy_" + Random.IntRange(1, 3) ));
            }
            Sample.INSTANCE.play( Assets.Sounds.GHOST );*/
        }

        public void sayHeroKilled(){
            /*if (Dungeon.bossLevel()){
                yell( Messages.get( this, "hero_killed_boss_" + Random.IntRange(1, 3) ));
            } else {
                yell( Messages.get( this, "hero_killed_" + Random.IntRange(1, 3) ));
            }
            Sample.INSTANCE.play( Assets.Sounds.GHOST );*/
        }

        public void sayAnhk(){
            /*yell( Messages.get( this, "blessed_ankh_" + Random.IntRange(1, 3) ));
            Sample.INSTANCE.play( Assets.Sounds.GHOST );*/
        }

        private static final String DEFEND_POS = "defend_pos";
        private static final String MOVING_TO_DEFEND = "moving_to_defend";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(DEFEND_POS, defendingPos);
            bundle.put(MOVING_TO_DEFEND, movingToDefendPos);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            if (bundle.contains(DEFEND_POS)) defendingPos = bundle.getInt(DEFEND_POS);
            movingToDefendPos = bundle.getBoolean(MOVING_TO_DEFEND);
        }

        {
            immunities.add( ScrollOfRetribution.class );
            immunities.add( ScrollOfPsionicBlast.class );
            immunities.add( Corruption.class );
        }

        private class Wandering extends Mob.Wandering {

            @Override
            public boolean act( boolean enemyInFOV, boolean justAlerted ) {
                if ( enemyInFOV && !movingToDefendPos ) {

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
                        if (pos == defendingPos) movingToDefendPos = false;
                        return moveSprite( oldPos, pos );
                    } else {
                        spend( TICK );
                    }

                }
                return true;
            }

        }

    }

}
