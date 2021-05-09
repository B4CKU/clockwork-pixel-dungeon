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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChooseWay;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class Wishgranter extends Item {

	private static final String AC_GRANT = "GRANT";
	
	{
		image = ItemSpriteSheet.ARTIFACT_BEACON;
		defaultAction = AC_GRANT;

		unique = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_GRANT );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_GRANT)) {

			curUser = hero;

			GameScene.show( new WndWishgranter(this) );
			
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}

	private static class WndWishgranter extends Window {

		private static final int WIDTH		= 120;
		private static final int BTN_HEIGHT	= 18;
		private static final float GAP		= 2;

		public WndWishgranter(final Wishgranter wish) {
			super();

			IconTitle titlebar = new IconTitle();
			titlebar.icon( new ItemSprite( wish.image(), null ) );
			titlebar.label( wish.name() );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			RenderedTextBlock hl = PixelScene.renderTextBlock( 6 );
			hl.text( Messages.get(this, "message"), WIDTH );
			hl.setPos( titlebar.left(), titlebar.bottom() + GAP );
			add( hl );

			RedButton btnExp = new RedButton( Messages.get(this, "experience") ) {
				@Override
				protected void onClick() {
					Potion potion = new PotionOfExperience();
					potion.doDrink(curUser);
				}
			};
			btnExp.setRect( 0, hl.bottom() + GAP, (WIDTH - GAP) / 2, BTN_HEIGHT );
			add( btnExp );

			RedButton btnStr = new RedButton( Messages.get(this, "strength") ) {
				@Override
				protected void onClick() {
					Potion potion = new PotionOfStrength();
					potion.doDrink(curUser);
				}
			};
			btnStr.setRect( btnExp.right() + GAP, btnExp.top(), btnExp.width(), BTN_HEIGHT );
			add( btnStr );

			RedButton btnUpg = new RedButton( Messages.get(this, "upgrades") ) {
				@Override
				protected void onClick() {
					Scroll scroll = new ScrollOfUpgrade();
					if(! scroll.identify().collect() )
						Dungeon.level.drop(scroll, curUser.pos).sprite.drop();
					else
						Sample.INSTANCE.play( Assets.Sounds.ITEM );
				}
			};
			btnUpg.setRect( btnExp.left(), btnExp.bottom() + GAP, btnStr.width(), BTN_HEIGHT );
			add( btnUpg );

			RedButton btnEnc = new RedButton( Messages.get(this, "enchantments") ) {
				@Override
				protected void onClick() {
					Scroll scroll = new ScrollOfEnchantment();
					if(! scroll.identify().collect() )
						Dungeon.level.drop(scroll, curUser.pos).sprite.drop();
					else
						Sample.INSTANCE.play( Assets.Sounds.ITEM );
				}
			};
			btnEnc.setRect( btnUpg.right() + GAP, btnUpg.top(), btnUpg.width(), BTN_HEIGHT );
			add( btnEnc );

			RedButton btnIte = new RedButton( Messages.get(this, "items") ) {
				@Override
				protected void onClick() {
					Item weapon = new Greataxe();
					if(! weapon.identify().collect() )
						Dungeon.level.drop(weapon, curUser.pos).sprite.drop();
					else
						Sample.INSTANCE.play( Assets.Sounds.ITEM );

					Item armor = new PlateArmor();
					if(! armor.identify().collect() )
						Dungeon.level.drop(armor, curUser.pos).sprite.drop();
					else
						Sample.INSTANCE.play( Assets.Sounds.ITEM );

					Item wand = new WandOfMagicMissile();
					if(! wand.identify().collect() )
						Dungeon.level.drop(wand, curUser.pos).sprite.drop();
					else
						Sample.INSTANCE.play( Assets.Sounds.ITEM );
				}
			};
			btnIte.setRect( btnUpg.left(), btnUpg.bottom() + GAP, btnEnc.width(), BTN_HEIGHT );
			add( btnIte );

			RedButton btnHps = new RedButton( Messages.get(this, "health") ) {
				@Override
				protected void onClick() {
					Potion potion = new PotionOfHealing();
					potion.doDrink(curUser);
				}
			};
			btnHps.setRect( btnIte.right() + GAP, btnIte.top(), btnIte.width(), BTN_HEIGHT );
			add( btnHps );

			RedButton btnTpf = new RedButton( Messages.get(this, "tpnext") ) {
				@Override
				protected void onClick() {
					if(Dungeon.depth < 26) {
						InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
						InterlevelScene.returnDepth = Dungeon.depth + 1;
						InterlevelScene.returnPos = -1;
						Game.switchScene( InterlevelScene.class );
					}
				}
			};
			btnTpf.setRect( btnIte.left(), btnIte.bottom() + GAP, btnHps.width(), BTN_HEIGHT );
			add( btnTpf );

			RedButton btnTpb = new RedButton( Messages.get(this, "tpprev") ) {
				@Override
				protected void onClick() {
					if(Dungeon.depth > 1) {
						InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
						InterlevelScene.returnDepth = Dungeon.depth - 1;
						InterlevelScene.returnPos = -1;
						Game.switchScene( InterlevelScene.class );
					}
				}
			};
			btnTpb.setRect( btnTpf.right() + GAP, btnTpf.top(), btnTpf.width(), BTN_HEIGHT );
			add( btnTpb );

			RedButton btnCancel = new RedButton( Messages.get(this, "cancel") ) {
				@Override
				protected void onClick() {
					hide();
				}
			};
			btnCancel.setRect( 0, btnTpf.bottom() + GAP, WIDTH, BTN_HEIGHT );
			add( btnCancel );

			resize( WIDTH, (int)btnCancel.bottom() );

		}

	}
}
