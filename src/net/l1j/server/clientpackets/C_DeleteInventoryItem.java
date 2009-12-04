/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.l1j.server.clientpackets;

import java.util.logging.Logger;

import net.l1j.server.ClientThread;
import net.l1j.log.LogDeleteItem;
import net.l1j.server.model.instance.L1DollInstance;
import net.l1j.server.model.instance.L1ItemInstance;
import net.l1j.server.model.instance.L1PcInstance;
import net.l1j.server.model.instance.L1PetInstance;
import net.l1j.server.serverpackets.S_ServerMessage;

// Referenced classes of package net.l1j.server.clientpackets:
// ClientBasePacket

public class C_DeleteInventoryItem extends ClientBasePacket {

	private static Logger _log = Logger.getLogger(C_DeleteInventoryItem.class
			.getName());
	private static final String C_DELETE_INVENTORY_ITEM
			= "[C] C_DeleteInventoryItem";

	public C_DeleteInventoryItem(byte[] decrypt, ClientThread client) {
		super(decrypt);
		int itemObjectId = readD();
		L1PcInstance pc = client.getActiveChar();
		L1ItemInstance item = pc.getInventory().getItem(itemObjectId);

		// 在伺服器上打算刪除物品的時候
		if (item == null) {
			return;
		}

		if (item.getItem().isCantDelete()) {
			// \f1你不能夠放棄此樣物品。
			pc.sendPackets(new S_ServerMessage(125));
			return;
		}

		Object[] petlist = pc.getPetList().values().toArray();
		for (Object petObject : petlist) {
			if (petObject instanceof L1PetInstance) {
				L1PetInstance pet = (L1PetInstance) petObject;
				if (item.getId() == pet.getItemObjId()) {
					// \f1%0%d是不可轉移的…
					pc.sendPackets(new S_ServerMessage(210, item.getItem()
							.getName()));
					return;
				}
			}
		}

		Object[] dollList = pc.getDollList().values().toArray();
		for (Object dollObject : dollList) {
			if (dollObject instanceof L1DollInstance) {
				L1DollInstance doll = (L1DollInstance) dollObject;
				if (item.getId() == doll.getItemObjId()) {
					// \f1這個魔法娃娃目前正在使用中。
					pc.sendPackets(new S_ServerMessage(1181));
					return;
				}
			}
		}

		if (item.isEquipped()) {
			// \f1你不能夠放棄此樣物品。
			pc.sendPackets(new S_ServerMessage(125));
			return;
		}
		if (item.getBless() >= 128) { // 封印的裝備
			// \f1%0%d是不可轉移的…
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
			return;
		}
		LogDeleteItem ldi = new LogDeleteItem();
		ldi.storeLogDeleteItem(pc, item);
		pc.getInventory().removeItem(item, item.getCount());
		pc.turnOnOffLight();
	}

	@Override
	public String getType() {
		return C_DELETE_INVENTORY_ITEM;
	}
}