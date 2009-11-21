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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.l1j.Config;
import net.l1j.server.ClientThread;
import net.l1j.server.model.L1Clan;
import net.l1j.server.model.L1World;
import net.l1j.server.model.instance.L1PcInstance;
import net.l1j.server.serverpackets.S_CharTitle;
import net.l1j.server.serverpackets.S_ServerMessage;

// Referenced classes of package net.l1j.server.clientpackets:
// ClientBasePacket

public class C_Title extends ClientBasePacket {

	private static final String C_TITLE = "[C] C_Title";
	private static Logger _log = Logger.getLogger(C_Title.class.getName());

	public C_Title(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		L1PcInstance pc = clientthread.getActiveChar();
		String charName = readS();
		String title = readS();

		if (charName.isEmpty() || title.isEmpty()) {
			// \f1次のように入力してください：「/title \f0キャラクター名 呼稱\f1」
			pc.sendPackets(new S_ServerMessage(196));
			return;
		}
		L1PcInstance target = L1World.getInstance().getPlayer(charName);
		if (target == null) {
			return;
		}

		if (pc.isGm()) {
			changeTitle(target, title);
			return;
		}

		if (isClanLeader(pc)) { // 血盟主
			if (pc.getId() == target.getId()) { // 自分
				if (pc.getLevel() < 10) {
					// \f1血盟員の場合、呼稱を持つにはレベル10以上でなければなりません。
					pc.sendPackets(new S_ServerMessage(197));
					return;
				}
				changeTitle(pc, title);
			} else { // 他人
				if (pc.getClanid() != target.getClanid()) {
					// \f1血盟員でなければ他人に呼稱を与えることはできません。
					pc.sendPackets(new S_ServerMessage(199));
					return;
				}
				if (target.getLevel() < 10) {
					// \f1%0のレベルが10未滿なので呼稱を与えることはできません。
					pc.sendPackets(new S_ServerMessage(202, charName));
					return;
				}
				changeTitle(target, title);
				L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					for (L1PcInstance clanPc : clan.getOnlineClanMember()) {
						// \f1%0が%1に「%2」という呼稱を与えました。
						clanPc.sendPackets(new S_ServerMessage(203, pc
								.getName(), charName, title));
					}
				}
			}
		} else {
			if (pc.getId() == target.getId()) { // 自分
				if (pc.getClanid() != 0 && !Config.CHANGE_TITLE_BY_ONESELF) {
					// \f1血盟員に呼稱を与えられるのはプリンスとプリンセスだけです。
					pc.sendPackets(new S_ServerMessage(198));
					return;
				}
				if (target.getLevel() < 40) {
					// \f1血盟員ではないのに呼稱を持つには、レベル40以上でなければなりません。
					pc.sendPackets(new S_ServerMessage(200));
					return;
				}
				changeTitle(pc, title);
			} else { // 他人
				if (pc.isCrown()) { // 連合に所屬した君主
					if (pc.getClanid() == target.getClanid()) {
						// \f1%0はあなたの血盟ではありません。
						pc.sendPackets(new S_ServerMessage(201, pc
								.getClanname()));
						return;
					}
				}
			}
		}
	}

	private void changeTitle(L1PcInstance pc, String title) {
		int objectId = pc.getId();
		pc.setTitle(title);
		pc.sendPackets(new S_CharTitle(objectId, title));
		pc.broadcastPacket(new S_CharTitle(objectId, title));
		try {
			pc.save(); // DBにキャラクター情報を書き迂む
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private boolean isClanLeader(L1PcInstance pc) {
		boolean isClanLeader = false;
		if (pc.getClanid() != 0) { // クラン所屬
			L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (pc.isCrown() && pc.getId() == clan.getLeaderId()) { // 君主、かつ、血盟主
					isClanLeader = true;
				}
			}
		}
		return isClanLeader;
	}

	@Override
	public String getType() {
		return C_TITLE;
	}

}
