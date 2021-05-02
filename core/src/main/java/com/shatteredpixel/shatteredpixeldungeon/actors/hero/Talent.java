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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArmorDamage;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnhancedRings;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.KnightShield;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.WandEmpower;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.RunicHarpoon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public enum Talent {

	//Warrior T1
	HEARTY_MEAL(0), ARMSMASTERS_INTUITION(1), TEST_SUBJECT(2), IRON_WILL(3),
	//Warrior T2
	IRON_STOMACH(4), RESTORED_WILLPOWER(5), RUNIC_TRANSFERENCE(6), LETHAL_MOMENTUM(7), IMPROVISED_PROJECTILES(8),
	//Warrior T3
	HOLD_FAST(9, 3), STRONGMAN(10, 3),
	//Berserker T3
	ENDLESS_RAGE(11, 3), BERSERKING_STAMINA(12, 3), ENRAGED_CATALYST(13, 3),
	//Gladiator T3
	CLEAVE(14, 3), LETHAL_DEFENSE(15, 3), ENHANCED_COMBO(16, 3),

	//Mage T1
	EMPOWERING_MEAL(32), SCHOLARS_INTUITION(33), TESTED_HYPOTHESIS(34), BACKUP_BARRIER(35),
	//Mage T2
	ENERGIZING_MEAL(36), ENERGIZING_UPGRADE(37), WAND_PRESERVATION(38), ARCANE_VISION(39), SHIELD_BATTERY(40),
	//Mage T3
	EMPOWERING_SCROLLS(41, 3), ALLY_WARP(42, 3),
	//Battlemage T3
	EMPOWERED_STRIKE(43, 3), MYSTICAL_CHARGE(44, 3), EXCESS_CHARGE(45, 3),
	//Warlock T3
	SOUL_EATER(46, 3), SOUL_SIPHON(47, 3), NECROMANCERS_MINIONS(48, 3),

	//Rogue T1
	CACHED_RATIONS(64), THIEFS_INTUITION(65), SUCKER_PUNCH(66), PROTECTIVE_SHADOWS(67),
	//Rogue T2
	MYSTICAL_MEAL(68), MYSTICAL_UPGRADE(69), WIDE_SEARCH(70), SILENT_STEPS(71), ROGUES_FORESIGHT(72),
	//Rogue T3
	ENHANCED_RINGS(73, 3), LIGHT_CLOAK(74, 3),
	//Assassin T3
	ENHANCED_LETHALITY(75, 3), ASSASSINS_REACH(76, 3), BOUNTY_HUNTER(77, 3),
	//Freerunner T3
	EVASIVE_ARMOR(78, 3), PROJECTILE_MOMENTUM(79, 3), SPEEDY_STEALTH(80, 3),

	//Huntress T1
	NATURES_BOUNTY(96), SURVIVALISTS_INTUITION(97), FOLLOWUP_STRIKE(98), NATURES_AID(99),
	//Huntress T2
	INVIGORATING_MEAL(100), RESTORED_NATURE(101), REJUVENATING_STEPS(102), HEIGHTENED_SENSES(103), DURABLE_PROJECTILES(104),
	//Huntress T3
	POINT_BLANK(105, 3), SEER_SHOT(106, 3),
	//Sniper T3
	FARSIGHT(107, 3), SHARED_ENCHANTMENT(108, 3), SHARED_UPGRADES(109, 3),
	//Warden T3
	DURABLE_TIPS(110, 3), BARKSKIN(111, 3), SHIELDING_DEW(112, 3),

	//Adventurer T1
	ARMOR_MASTERY(1,1), WEAPON_MASTERY(2, 1), ARCANE_MASTERY(3, 1),
	AGILITY_MASTERY(4, 1), CRAFT_MASTERY(5, 1), ELEMENTAL_MASTERY(6, 1),

	//Adventurer Skilltrees
	STAFFCRAFTING(7, 1, WEAPON_MASTERY, ARCANE_MASTERY), STAFFCRAFTING_CHANNELING(8, 2, STAFFCRAFTING), STAFFCRAFTING_EMPOWERED(9, 2, STAFFCRAFTING),
	BERSERKING(10 , 1, ARMOR_MASTERY, WEAPON_MASTERY), BERSERKING_ANGER(11 , 2, BERSERKING), BERSERKING_DETERMINATION(12 , 2, BERSERKING),
	DREAMWEAVER(13, 1, AGILITY_MASTERY, ELEMENTAL_MASTERY), DREAMWEAVER_SANDMAN(14, 2, DREAMWEAVER), DREAMWEAVER_NIGHTMARE(15, 2, DREAMWEAVER),
	DARKARTS(16 , 1, ARMOR_MASTERY, ARCANE_MASTERY), DARKARTS_POWER(32 , 2, DARKARTS), DARKARTS_NECROMANCER(33 , 2, DARKARTS),
	SPELLSHOT(34, 1, ARCANE_MASTERY, AGILITY_MASTERY), SPELLSHOT_ENCHANT(35 , 2, SPELLSHOT), SPELLSHOT_HARPOON(36 , 2, SPELLSHOT),
	ARMOR_AGILITY(0, 1, ARMOR_MASTERY, AGILITY_MASTERY),
	ARMOR_CRAFT(0, 1, ARMOR_MASTERY, CRAFT_MASTERY),
	ARMOR_ELEMENTS(0, 1, ARMOR_MASTERY, ELEMENTAL_MASTERY),
	WEAPON_AGILITY(0, 1, WEAPON_MASTERY, AGILITY_MASTERY),
	WEAPON_CRAFT(0, 1, WEAPON_MASTERY, CRAFT_MASTERY),
	WEAPON_ELEMENTS(0, 1, WEAPON_MASTERY, ELEMENTAL_MASTERY),
	ARCANE_CRAFT(0, 1, ARCANE_MASTERY, CRAFT_MASTERY),
	ARCANE_ELEMENTS(0, 1, ARCANE_MASTERY, ELEMENTAL_MASTERY),
	AGILITY_CRAFT(0, 1, AGILITY_MASTERY, CRAFT_MASTERY),
	CRAFT_ELEMENTS(0, 1, CRAFT_MASTERY, ELEMENTAL_MASTERY);


	public static class ImprovisedProjectileCooldown extends FlavourBuff{};
	public static class LethalMomentumTracker extends FlavourBuff{};
	public static class WandPreservationCounter extends CounterBuff{};
	public static class BountyHunterTracker extends FlavourBuff{};
	public static class RejuvenatingStepsCooldown extends FlavourBuff{};
	public static class SeerShotCooldown extends FlavourBuff{};
	public static class SteadyAimCooldown extends FlavourBuff{};
	public static class StaffcraftingEmpoweredStrikeTracker extends FlavourBuff{};
	public static class SandmanCooldown extends FlavourBuff{};

	int icon;
	int maxPoints;
	Talent requirementOne;
	Talent requirementTwo;

	// tiers 1/2/3/4 start at levels 2/8/16/30
	public static int[] tierLevelThresholds = new int[]{0, 2, 8, 16, 30, 31};

	Talent( int icon ){
		this(icon, 2);
	}

	Talent( int icon, int maxPoints ){
		this.icon = icon;
		this.maxPoints = maxPoints;
	}

	Talent( int icon, int maxPoints, Talent requirementOne){
		this.icon = icon;
		this.maxPoints = maxPoints;
		this.requirementOne = requirementOne;
	}

	Talent( int icon, int maxPoints, Talent requirementOne, Talent requirementTwo ){
		this.icon = icon;
		this.maxPoints = maxPoints;
		this.requirementOne = requirementOne;
		this.requirementTwo = requirementTwo;
	}

	public int icon(){
		return icon;
	}

	public int maxPoints(){
		return maxPoints;
	}

	public Talent requirementOne(){
		return requirementOne;
	}

	public Talent requirementTwo(){
		return requirementTwo;
	}

	public String title(){
		return Messages.get(this, name() + ".title");
	}

	public String desc(){
		return Messages.get(this, name() + ".desc");
	}

	public static void onTalentUpgraded( Hero hero, Talent talent){
		int points = hero.pointsInTalent(talent);
		switch(talent) {
			case NATURES_BOUNTY:
				if (points == 1) Buff.count(hero, NatureBerriesAvailable.class, 4);
				else                                           Buff.count(hero, NatureBerriesAvailable.class, 2);
				break;
			case ARMSMASTERS_INTUITION:
				if (points == 2) {
					if (hero.belongings.weapon != null) hero.belongings.weapon.identify();
					if (hero.belongings.armor != null)  hero.belongings.armor.identify();
				}
				break;
			case THIEFS_INTUITION:
				if (points == 1) {
					if (hero.belongings.ring instanceof Ring) hero.belongings.ring.setKnown();
					if (hero.belongings.misc instanceof Ring) ((Ring) hero.belongings.misc).setKnown();
				} else if (points == 2) {
					if (hero.belongings.ring instanceof Ring) hero.belongings.ring.identify();
					if (hero.belongings.misc instanceof Ring) hero.belongings.misc.identify();
					for (Item item : Dungeon.hero.belongings){
						if (item instanceof Ring){
							((Ring) item).setKnown();
						}
					}
				}
				break;
			case FARSIGHT:
				Dungeon.observe();
				break;
			case WEAPON_MASTERY:
				if (hero.belongings.weapon != null) hero.belongings.weapon.identify();
				break;
			case ARMOR_MASTERY:
				if (hero.belongings.armor != null)  hero.belongings.armor.identify();
				Buff.affect(hero, KnightShield.class);
				break;
			case AGILITY_MASTERY:
				if (hero.belongings.ring instanceof Ring) hero.belongings.ring.identify();
				if (hero.belongings.misc instanceof Ring) hero.belongings.misc.identify();
				break;
			case CRAFT_MASTERY:
				if (!(Scroll.getUnknown().isEmpty())) {
					Scroll s = Reflection.newInstance(Random.element(Scroll.getUnknown()));
					s.identify();
					GLog.p( Messages.get(Hero.class, "identify", s) );
				}
				break;
			case ELEMENTAL_MASTERY:
				if (!(Potion.getUnknown().isEmpty())) {
					Potion p = Reflection.newInstance(Random.element(Potion.getUnknown()));
					p.identify();
					GLog.p( Messages.get(Hero.class, "identify", p) );
				}
				break;
			case STAFFCRAFTING:
				MagesStaff staff = new MagesStaff(new WandOfMagicMissile());
				if (!staff.identify().collect()) {
					Dungeon.level.drop(staff, hero.pos).sprite.drop();;
				}
				break;
			case SPELLSHOT_HARPOON:
				if (points == 1) {
					RunicHarpoon harpoon = new RunicHarpoon();
					if (!harpoon.collect()) {
						Dungeon.level.drop(harpoon, hero.pos).sprite.drop();
					}
				}
				break;
		}


	}

	public static class CachedRationsDropped extends CounterBuff{};
	public static class NatureBerriesAvailable extends CounterBuff{};

	public static void onLevelUp( Hero hero ){
		if (hero.hasTalent(CRAFT_MASTERY)){
			if (!(Scroll.getUnknown().isEmpty())) {
				Scroll s = Reflection.newInstance(Random.element(Scroll.getUnknown()));
				s.identify();
				GLog.p( Messages.get(Hero.class, "identify", s) );
			}
		}
		if (hero.hasTalent(ELEMENTAL_MASTERY)){
			if (!(Potion.getUnknown().isEmpty())) {
				Potion p = Reflection.newInstance(Random.element(Potion.getUnknown()));
				p.identify();
				GLog.p( Messages.get(Hero.class, "identify", p) );
			}
		}

	}

	public static void onFoodEaten( Hero hero, float foodVal, Item foodSource ){
		if (hero.hasTalent(HEARTY_MEAL)){
			//3/5 HP healed, when hero is below 25% health
			if (hero.HP <= hero.HT/4) {
				hero.HP = Math.min(hero.HP + 1 + 2 * hero.pointsInTalent(HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1+hero.pointsInTalent(HEARTY_MEAL));
			//2/3 HP healed, when hero is below 50% health
			} else if (hero.HP <= hero.HT/2){
				hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(HEARTY_MEAL), hero.HT);
				hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(HEARTY_MEAL));
			}
		}
		if (hero.hasTalent(IRON_STOMACH)){
			if (hero.cooldown() > 0) {
				Buff.affect(hero, WarriorFoodImmunity.class, hero.cooldown());
			}
		}
		if (hero.hasTalent(EMPOWERING_MEAL)){
			//2/3 bonus wand damage for next 3 zaps
			Buff.affect( hero, WandEmpower.class).set(1 + hero.pointsInTalent(EMPOWERING_MEAL), 3);
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(ENERGIZING_MEAL)){
			//5/8 turns of recharging
			Buff.prolong( hero, Recharging.class, 2 + 3*(hero.pointsInTalent(ENERGIZING_MEAL)) );
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(MYSTICAL_MEAL)){
			//3/5 turns of recharging
			Buff.affect( hero, ArtifactRecharge.class).set(1 + 2*(hero.pointsInTalent(MYSTICAL_MEAL))).ignoreHornOfPlenty = foodSource instanceof HornOfPlenty;
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(INVIGORATING_MEAL)){
			//effectively 1/2 turns of haste
			Buff.prolong( hero, Haste.class, 0.67f+hero.pointsInTalent(INVIGORATING_MEAL));
		}
	}

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}

	public static float itemIDSpeedFactor( Hero hero, Item item ){
		// 1.75x/2.5x speed with huntress talent
		float factor = 1f + hero.pointsInTalent(SURVIVALISTS_INTUITION)*0.75f;

		// 2x/instant for Warrior (see onItemEquipped)
		if (item instanceof MeleeWeapon || item instanceof Armor){
			factor *= 1f + hero.pointsInTalent(ARMSMASTERS_INTUITION);
		}
		// 3x/instant for mage (see Wand.wandUsed())
		if (item instanceof Wand){
			factor *= 1f + 2*hero.pointsInTalent(SCHOLARS_INTUITION);
		}
		// 2x/instant for rogue (see onItemEquipped), also id's type on equip/on pickup
		if (item instanceof Ring){
			factor *= 1f + hero.pointsInTalent(THIEFS_INTUITION);
		}
		return factor;
	}

	public static void onHealingPotionUsed( Hero hero ){
		if (hero.hasTalent(RESTORED_WILLPOWER)){
			BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
			if (shield != null){
				int shieldToGive = Math.round(shield.maxShield() * 0.33f*(1+hero.pointsInTalent(RESTORED_WILLPOWER)));
				shield.supercharge(shieldToGive);
			}
		}
		if (hero.hasTalent(RESTORED_NATURE)){
			ArrayList<Integer> grassCells = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				grassCells.add(hero.pos+i);
			}
			Random.shuffle(grassCells);
			for (int cell : grassCells){
				Char ch = Actor.findChar(cell);
				if (ch != null){
					Buff.affect(ch, Roots.class, 1f + hero.pointsInTalent(RESTORED_NATURE));
				}
				if (Dungeon.level.map[cell] == Terrain.EMPTY ||
						Dungeon.level.map[cell] == Terrain.EMBERS ||
						Dungeon.level.map[cell] == Terrain.EMPTY_DECO){
					Level.set(cell, Terrain.GRASS);
					GameScene.updateMap(cell);
				}
				CellEmitter.get(cell).burst(LeafParticle.LEVEL_SPECIFIC, 4);
			}
			if (hero.pointsInTalent(RESTORED_NATURE) == 1){
				grassCells.remove(0);
				grassCells.remove(0);
				grassCells.remove(0);
			}
			for (int cell : grassCells){
				int t = Dungeon.level.map[cell];
				if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
						|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
						&& Dungeon.level.plants.get(cell) == null){
					Level.set(cell, Terrain.HIGH_GRASS);
					GameScene.updateMap(cell);
				}
			}
			Dungeon.observe();
		}
	}

	public static void onUpgradeScrollUsed( Hero hero ){
		if (hero.hasTalent(ENERGIZING_UPGRADE)){
			MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
			if (staff != null){
				staff.gainCharge(1 + hero.pointsInTalent(ENERGIZING_UPGRADE), true);
				ScrollOfRecharging.charge( Dungeon.hero );
				SpellSprite.show( hero, SpellSprite.CHARGE );
			}
		}
		if (hero.hasTalent(MYSTICAL_UPGRADE)){
			CloakOfShadows cloak = hero.belongings.getItem(CloakOfShadows.class);
			if (cloak != null){
				cloak.overCharge(1 + hero.pointsInTalent(MYSTICAL_UPGRADE));
				ScrollOfRecharging.charge( Dungeon.hero );
				SpellSprite.show( hero, SpellSprite.CHARGE );
			}
		}
	}

	public static void onArtifactUsed( Hero hero ){
		if (hero.hasTalent(ENHANCED_RINGS)){
			Buff.prolong(hero, EnhancedRings.class, 3f*hero.pointsInTalent(ENHANCED_RINGS));
		}
	}

	public static void onItemEquipped( Hero hero, Item item ){
		if (hero.pointsInTalent(ARMSMASTERS_INTUITION) == 2 && (item instanceof Weapon || item instanceof Armor)){
			item.identify();
		}
		if (hero.hasTalent(THIEFS_INTUITION) && item instanceof Ring){
			if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
				item.identify();
			} else {
				((Ring) item).setKnown();
			}
		}
		if (hero.hasTalent(WEAPON_MASTERY) && (item instanceof Weapon)){
			item.identify();
		}
		if (hero.hasTalent(ARMOR_MASTERY) && (item instanceof Armor)){
			item.identify();
		}
		if (hero.hasTalent(AGILITY_MASTERY) && (item instanceof Ring)){
			item.identify();
		}
	}

	public static void onItemCollected( Hero hero, Item item ){
		if (hero.pointsInTalent(THIEFS_INTUITION) == 2){
			if (item instanceof Ring) ((Ring) item).setKnown();
		}
	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	public static void onItemIdentified( Hero hero, Item item ){
		if (hero.hasTalent(TEST_SUBJECT)){
			//heal for 2/3 HP
			hero.HP = Math.min(hero.HP + 1 + hero.pointsInTalent(TEST_SUBJECT), hero.HT);
			Emitter e = hero.sprite.emitter();
			if (e != null) e.burst(Speck.factory(Speck.HEALING), hero.pointsInTalent(TEST_SUBJECT));
		}
		if (hero.hasTalent(TESTED_HYPOTHESIS)){
			//2/3 turns of wand recharging
			Buff.affect(hero, Recharging.class, 1f + hero.pointsInTalent(TESTED_HYPOTHESIS));
			ScrollOfRecharging.charge(hero);
		}
	}

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if (hero.hasTalent(Talent.SUCKER_PUNCH)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(SuckerPunchTracker.class) == null){
			dmg += Random.IntRange(hero.pointsInTalent(Talent.SUCKER_PUNCH) , 2);
			Buff.affect(enemy, SuckerPunchTracker.class);
		}

		if (hero.hasTalent(Talent.FOLLOWUP_STRIKE)) {
			if (hero.belongings.weapon instanceof MissileWeapon) {
				Buff.affect(enemy, FollowupStrikeTracker.class);
			} else if (enemy.buff(FollowupStrikeTracker.class) != null){
				dmg += 1 + hero.pointsInTalent(FOLLOWUP_STRIKE);
				if (!(enemy instanceof Mob) || !((Mob) enemy).surprisedBy(hero)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
				}
				enemy.buff(FollowupStrikeTracker.class).detach();
			}
		}

		if (hero.hasTalent(Talent.WEAPON_MASTERY)) {
			ArmorDamage armordmg = enemy.buff(ArmorDamage.class);
			if( armordmg != null && armordmg.cooldown() > ArmorDamage.DURATION) {
				Buff.affect( enemy, ArmorDamage.ArmorBreak.class, ArmorDamage.ArmorBreak.DURATION );
				Buff.detach( enemy, ArmorDamage.class );
			}
			else if (enemy.buff(ArmorDamage.ArmorBreak.class) == null) Buff.affect( enemy, ArmorDamage.class, ArmorDamage.DURATION/2f );
		}

		if (hero.hasTalent(Talent.ARCANE_MASTERY) && hero.belongings.weapon instanceof MeleeWeapon) {
			for (Wand.Charger c : hero.buffs(Wand.Charger.class)){
				c.gainCharge(0.25f);
			}
		}

		//this is kinda ugly, but it will do for now
		if (hero.pointsInTalent(Talent.BERSERKING_ANGER) == 2 && hero.buff(AngerIssuesTracker.class) == null && hero.belongings.weapon instanceof MeleeWeapon) {
			Berserk berserk = hero.buff(Berserk.class);
			if (berserk != null && berserk.rageAmount() >= 0.8f) {

				//get all possible targets, check how far away they are from us, then if the list is not empty, randomize it and deal damage to one of the targets
				ArrayList<Char> targets = new ArrayList<>();
				for (int i : PathFinder.NEIGHBOURS8) {
					Char potential_target = Actor.findChar(enemy.pos + i);
					if (potential_target instanceof Mob && Dungeon.level.adjacent( hero.pos, potential_target.pos )
						&& potential_target.alignment == Char.Alignment.ENEMY && !hero.isCharmedBy(potential_target))
						targets.add(potential_target);
				}

				Mob second_target = (Mob)Random.element( targets );

				//i was trying literally anything to make it alright i had no fucking clue it would be this easy
				if (second_target != null) {
					Buff.affect(hero, AngerIssuesTracker.class);
					hero.attack(second_target);
					Buff.detach(hero, AngerIssuesTracker.class);
				}
			}
		}

		if (hero.hasTalent(DREAMWEAVER_NIGHTMARE) && hero.belongings.weapon instanceof MeleeWeapon
				&& enemy instanceof Mob && ((Mob)enemy).state == ((Mob)enemy).SLEEPING ) {
			if (hero.pointsInTalent(DREAMWEAVER_NIGHTMARE) == 2) {
				Dungeon.hero.HP = (int) Math.ceil(Math.min(Dungeon.hero.HT, Dungeon.hero.HP + dmg));
			}
			Buff.affect(Dungeon.hero, Hunger.class).affectHunger(dmg);
			Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 3);
		}



		return dmg;
	}

	public static class SuckerPunchTracker extends Buff{};
	public static class FollowupStrikeTracker extends Buff{};
	public static class AngerIssuesTracker extends Buff{};
	public static class DreamweaverTracker extends Buff{
		public int stacks = 0;
		public boolean woken_up = false;
		@Override
		public boolean act() {
			if ((target instanceof Mob && ((Mob)target).state != ((Mob)target).SLEEPING) || stacks >= 6) { woken_up = true;	}
			spend( TICK );
			return true;
		}
		private static final String STACKS = "stacks";
		private static final String WOKEN_UP = "woken_up";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STACKS, stacks);
			bundle.put(WOKEN_UP, woken_up);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			stacks = bundle.getInt(STACKS);
			woken_up = bundle.getBoolean(WOKEN_UP);
		}
	};

	public static final int MAX_TALENT_TIERS = 3;

	public static void initClassTalents( Hero hero ){
		initClassTalents( hero.heroClass, hero.talents );
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HEARTY_MEAL, ARMSMASTERS_INTUITION, TEST_SUBJECT, IRON_WILL);
				break;
			case MAGE:
				Collections.addAll(tierTalents, ENERGIZING_MEAL, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, MYSTICAL_MEAL, MYSTICAL_UPGRADE, WIDE_SEARCH, SILENT_STEPS, ROGUES_FORESIGHT);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, INVIGORATING_MEAL, RESTORED_NATURE, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES);
				break;
			case ADVENTURER:
				Collections.addAll(tierTalents, ARMOR_MASTERY, WEAPON_MASTERY, ARCANE_MASTERY, AGILITY_MASTERY, CRAFT_MASTERY, ELEMENTAL_MASTERY);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(0).put(talent, 0);
		}
		tierTalents.clear();

		//tier 2
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, IRON_STOMACH, RESTORED_WILLPOWER, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES);
				break;
			case MAGE:
				Collections.addAll(tierTalents, ENERGIZING_MEAL, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, MYSTICAL_MEAL, MYSTICAL_UPGRADE, WIDE_SEARCH, SILENT_STEPS, ROGUES_FORESIGHT);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, INVIGORATING_MEAL, RESTORED_NATURE, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES);
				break;
			case ADVENTURER:
				//technically all of them are available, but only some of them are visible at the time
				Collections.addAll(tierTalents, STAFFCRAFTING, BERSERKING, DARKARTS, SPELLSHOT, ARMOR_AGILITY,
												ARMOR_CRAFT, ARMOR_ELEMENTS, WEAPON_AGILITY, WEAPON_CRAFT, WEAPON_ELEMENTS,
												ARCANE_CRAFT, ARCANE_ELEMENTS, AGILITY_CRAFT, DREAMWEAVER, CRAFT_ELEMENTS);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();

		//tier 3
		switch (cls){
			case WARRIOR: default:
				Collections.addAll(tierTalents, HOLD_FAST, STRONGMAN);
				break;
			case MAGE:
				Collections.addAll(tierTalents, EMPOWERING_SCROLLS, ALLY_WARP);
				break;
			case ROGUE:
				Collections.addAll(tierTalents, ENHANCED_RINGS, LIGHT_CLOAK);
				break;
			case HUNTRESS:
				Collections.addAll(tierTalents, POINT_BLANK, SEER_SHOT);
			case ADVENTURER:
				Collections.addAll(tierTalents, STAFFCRAFTING_CHANNELING, STAFFCRAFTING_EMPOWERED, BERSERKING_ANGER, BERSERKING_DETERMINATION,
						DREAMWEAVER_SANDMAN, DREAMWEAVER_NIGHTMARE, DARKARTS_POWER, DARKARTS_NECROMANCER, SPELLSHOT_ENCHANT, SPELLSHOT_HARPOON);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

		//tier4
		//TBD
		//not anymore lmaoooooooooooooooooooooo -b4cku
	}

	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 3
		switch (cls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, ENDLESS_RAGE, BERSERKING_STAMINA, ENRAGED_CATALYST);
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, CLEAVE, LETHAL_DEFENSE, ENHANCED_COMBO);
				break;
			case BATTLEMAGE:
				Collections.addAll(tierTalents, EMPOWERED_STRIKE, MYSTICAL_CHARGE, EXCESS_CHARGE);
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS);
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, ENHANCED_LETHALITY, ASSASSINS_REACH, BOUNTY_HUNTER);
				break;
			case FREERUNNER:
				Collections.addAll(tierTalents, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH);
				break;
			case SNIPER:
				Collections.addAll(tierTalents, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES);
				break;
			case WARDEN:
				Collections.addAll(tierTalents, DURABLE_TIPS, BARKSKIN, SHIELDING_DEW);
				break;
		}
		for (Talent talent : tierTalents){
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

		//tier4
		//TBD
	}

	private static final String TALENT_TIER = "talents_tier_";

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				if (tier.get(talent) > 0){
					tierBundle.put(talent.name(), tier.get(talent));
				}
				if (tierBundle.contains(talent.name())){
					tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
				}
			}
			bundle.put(TALENT_TIER+(i+1), tierBundle);
		}
	}

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero ){
		if (hero.heroClass != null) initClassTalents(hero);
		if (hero.subClass != null)  initSubclassTalents(hero);

		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = bundle.contains(TALENT_TIER+(i+1)) ? bundle.getBundle(TALENT_TIER+(i+1)) : null;
			//pre-0.9.1 saves
			if (tierBundle == null && i == 0 && bundle.contains("talents")){
				tierBundle = bundle.getBundle("talents");
			}

			if (tierBundle != null){
				for (Talent talent : tier.keySet()){
					if (tierBundle.contains(talent.name())){
						tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
					}
				}
			}
		}
	}

}
