package net.sf.freecol.common.model;

/**
 * Copyright (C) 2002-2015   The FreeCol Team This file is part of FreeCol. FreeCol is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version. FreeCol is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

public class PlayerProduct {
	private boolean[][] canSeeTiles = null;
	private boolean canSeeValid = false;
	private final Object canSeeLock = new Object();

	public boolean[][] getCanSeeTiles() {
		return canSeeTiles;
	}

	/**
	* Can this player see a given tile. The tile can be seen if it is in a unit or settlement's line of sight.
	* @param tile  The <code>Tile</code> to check.
	* @return  True if this player can see the given <code>Tile</code>.
	*/
	public boolean canSee(Tile tile, Player player) {
		if (tile == null)
			return false;
		do {
			synchronized (canSeeLock) {
				if (canSeeValid) {
					return canSeeTiles[tile.getX()][tile.getY()];
				}
			}
		} while (resetCanSeeTiles(player));
		return false;
	}

	/**
	* Resets this player's "can see"-tiles.  This is done by setting all the tiles within each  {@link Unit}  and  {@link Settlement} s line of sight visible.  The other tiles are made invisible. Use  {@link #invalidateCanSeeTiles}  whenever possible.
	* @return  True if successful.
	*/
	public boolean resetCanSeeTiles(Player player) {
		Map map = player.getGame().getMap();
		if (map == null)
			return false;
		boolean[][] cST = player.makeCanSeeTiles(map);
		synchronized (canSeeLock) {
			canSeeTiles = cST;
			canSeeValid = true;
		}
		return true;
	}

	/**
	* Forces an update of the <code>canSeeTiles</code>. This method should be used to invalidate the current <code>canSeeTiles</code> when something significant changes. The method  {@link #resetCanSeeTiles}  will be called whenever it is needed. So what is "significant"?  Looking at the makeCanSeeTiles routine suggests the following: - Unit added to map - Unit removed from map - Unit moved on map - Unit type changes (which may change its line-of-sight) - Unit ownership changes - Settlement added to map - Settlement removed from map - Settlement ownership changes - Coronado added (can now see other colonies) - Coronado removed (only in debug mode) - Mission established (if enhanced missionaries enabled) - Mission removed (if enhanced missionaries enabled) - Mission ownership changes (Spanish succession with enhanced missionaries enabled) - Map is unexplored (debug mode) Ideally then when any of these events occurs we should call invalidateCanSeeTiles().  However while iCST is quick and cheap, as soon as we then call canSee() the big expensive makeCanSeeTiles will be run.  Often the situation in the server is that several routines with visibility implications will be called in succession.  Usually there, the best solution is to make all the changes and issue the iCST at the end.  So, to make this a bit more visible, routines that change visibility are annotated with a "-vis" comment at both definition and call sites.  Similarly routines that fix up the mess have a "+vis" comment.  Thus it is an error for a -vis to appear without a following +vis (unless the enclosing routine is marked -vis). By convention, we try to avoid cs* routines being -vis.
	*/
	public void invalidateCanSeeTiles() {
		synchronized (canSeeLock) {
			canSeeValid = false;
		}
	}

	/**
	* Initialize the highSeas. Needs to be public until the backward compatibility code in FreeColServer is gone.
	*/
	public void initializeHighSeas(Player player, Europe europe) {
		Game game = player.getGame();
		player.setHighSeas(new HighSeas(game));
		if (europe != null)
			player.getHighSeas().addDestination(europe);
		if (game.getMap() != null)
			player.getHighSeas().addDestination(game.getMap());
	}
}
