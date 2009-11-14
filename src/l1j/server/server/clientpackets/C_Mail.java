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

package l1j.server.server.clientpackets;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastTable;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.MailTable;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Mail;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.templates.L1Mail;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_Mail extends ClientBasePacket {

	private static final String C_MAIL = "[C] C_Mail";

	private static Logger _log = Logger.getLogger(C_Mail.class.getName());

	private static int TYPE_NORMAL_MAIL = 0; // 一般
	private static int TYPE_CLAN_MAIL = 1; // 血盟
	private static int TYPE_MAIL_BOX = 2; // 保管箱

	public C_Mail(byte abyte0[], ClientThread client) {
		super(abyte0);
		int type = readC();
		L1PcInstance pc = client.getActiveChar();

		if (type == 0x00 || type == 0x01 || type == 0x02) { // 開啟信件
			pc.sendPackets(new S_Mail(pc.getName(), type));
		} else if (type == 0x10 || type == 0x11 || type == 0x12) { // 讀む
			int mailId = readD();
			L1Mail mail = MailTable.getInstance().getMail(mailId);
			if (mail.getReadStatus() == 0) {
				MailTable.getInstance().setReadStatus(mailId);
			}
			pc.sendPackets(new S_Mail(mailId, type));
		} else if (type == 0x20) { // 一般信件
			int unknow = readH();
			String receiverName = readS();
			byte[] text = readByte();
			L1PcInstance receiver = L1World.getInstance().
					getPlayer(receiverName);
			if (receiver != null) {
				if (getMailSizeByReceiver(receiverName,
						TYPE_NORMAL_MAIL) >= 20) {
					pc.sendPackets(new S_Mail(type));
					pc.sendPackets(new S_ServerMessage(1240)); // 無法傳送信件。
					receiver.sendPackets(new S_ServerMessage(1261)); // 信箱已滿，無法再收信件。
					return;
				}
				MailTable.getInstance().writeMail(TYPE_NORMAL_MAIL,
						receiverName, pc, text);
				if (receiver.getOnlineStatus() == 1) {
					receiver.sendPackets(new S_Mail(receiverName,
							TYPE_NORMAL_MAIL));
					pc.sendPackets(new S_ServerMessage(1239)); // 已將信件送出了。
					receiver.sendPackets(new S_ServerMessage(428)); // 您收到鴿子信差給你的信件。
				}
			} else {
				try {
					L1PcInstance restorePc = CharacterTable.getInstance()
							.restoreCharacter(receiverName);
					if (restorePc != null) {
						if (getMailSizeByReceiver(receiverName,
								TYPE_NORMAL_MAIL) >= 20) {
							pc.sendPackets(new S_Mail(type));
							pc.sendPackets(new S_ServerMessage(1242)); // 信箱已滿，無法再收信件。
							return;
						}
						MailTable.getInstance().writeMail(TYPE_NORMAL_MAIL,
								receiverName, pc, text);
					} else {
						pc.sendPackets(new S_ServerMessage(109, receiverName)); // 沒有叫%0的人。
					}
				} catch (Exception e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} else if (type == 0x21) { // 血盟信件
			int unknow = readH();
			String clanName = readS();
			byte[] text = readByte();
			L1Clan clan = L1World.getInstance().getClan(clanName);
			if (clan != null) {
				for (String name : clan.getAllMembers()) {
					int size = getMailSizeByReceiver(name, TYPE_CLAN_MAIL);
					if (size >= 50) {
						pc.sendPackets(new S_ServerMessage(1240)); // 無法傳送信件。
						continue;
					}
					MailTable.getInstance().writeMail(TYPE_CLAN_MAIL, name,
							pc, text);
					L1PcInstance clanPc = L1World.getInstance().
							getPlayer(name);
					if (clanPc != null) {
						clanPc.sendPackets(new S_Mail(name,
								TYPE_CLAN_MAIL));
						pc.sendPackets(new S_ServerMessage(1239)); // 已將信件送出了。
						clanPc.sendPackets(new S_ServerMessage(428)); // 您收到鴿子信差給你的信件。
					}
				}
			}
		} else if (type == 0x30 || type == 0x31 || type == 0x32) { // 刪除信件
			int mailId = readD();
			MailTable.getInstance().deleteMail(mailId);
			pc.sendPackets(new S_Mail(mailId, type));
		} else if(type == 0x40) { // 保管信件
			int mailId = readD();
			MailTable.getInstance().setMailType(mailId, TYPE_MAIL_BOX);
			pc.sendPackets(new S_Mail(mailId, type));
		}
	}

	private int getMailSizeByReceiver(String receiverName, int type) {
		FastTable<L1Mail> mails = new FastTable<L1Mail>();
		for (L1Mail mail : MailTable.getInstance().getAllMail()) {
			if (mail.getReceiverName().equalsIgnoreCase(receiverName)) {
				if (mail.getType() == type) {
					mails.add(mail);
				}
			}
		}
		return mails.size();
	}

	@Override
	public String getType() {
		return C_MAIL;
	}
}