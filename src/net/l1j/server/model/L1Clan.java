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
package net.l1j.server.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastTable;

import net.l1j.server.datatables.CharacterTable;
import net.l1j.server.model.instance.L1PcInstance;

public class L1Clan {
	private static final Logger _log = Logger.getLogger(L1Clan.class.getName());

	public static final int CLAN_RANK_PROBATION = 1;
	public static final int CLAN_RANK_PUBLIC = 2;
	public static final int CLAN_RANK_GUARDIAN = 3;
	public static final int CLAN_RANK_PRINCE = 4;

	private int _clanId;

	private String _clanName;

	private int _leaderId;

	private String _leaderName;

	private int _castleId;

	private int _houseId;

	private int _warehouse = 0;

	private final L1DwarfForClanInventory _dwarfForClan = new L1DwarfForClanInventory(this);

	private final FastTable<String> membersNameList = new FastTable<String>();

	public int getClanId() {
		return _clanId;
	}

	public void setClanId(int clan_id) {
		_clanId = clan_id;
	}

	public String getClanName() {
		return _clanName;
	}

	public void setClanName(String clan_name) {
		_clanName = clan_name;
	}

	public int getLeaderId() {
		return _leaderId;
	}

	public void setLeaderId(int leader_id) {
		_leaderId = leader_id;
	}

	public String getLeaderName() {
		return _leaderName;
	}

	public void setLeaderName(String leader_name) {
		_leaderName = leader_name;
	}

	public int getCastleId() {
		return _castleId;
	}

	public void setCastleId(int hasCastle) {
		_castleId = hasCastle;
	}

	public int getHouseId() {
		return _houseId;
	}

	public void setHouseId(int hasHideout) {
		_houseId = hasHideout;
	}

	public void addMemberName(String member_name) {
		if (!membersNameList.contains(member_name)) {
			membersNameList.add(member_name);
		}
	}

	public void delMemberName(String member_name) {
		if (membersNameList.contains(member_name)) {
			membersNameList.remove(member_name);
		}
	}

	public L1PcInstance[] getOnlineClanMember() { // オンライン中のクラン員のみ
		FastTable<L1PcInstance> onlineMembers = new FastTable<L1PcInstance>();
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if (pc != null && !onlineMembers.contains(pc)) {
				onlineMembers.add(pc);
			}
		}
		return onlineMembers.toArray(new L1PcInstance[onlineMembers.size()]);
	}

	public String getOnlineMembersFP() { // FP means "For Pledge"
		String result = "";
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + name + " ";
			}
		}
		return result;
	}

	public String getAllMembersFP() {
		String result = "";
		for (String name : membersNameList) {
			result = result + name + " ";
		}
		return result;
	}

	public String getOnlineMembersFPWithRank() {
		String result = "";
		for (String name : membersNameList) {
			L1PcInstance pc = L1World.getInstance().getPlayer(name);
			if (pc != null) {
				result = result + name + getRankString(pc) + " ";
			}
		}
		return result;
	}

	public String getAllMembersFPWithRank() {
		String result = "";
		try {
			for (String name : membersNameList) {
				L1PcInstance pc = CharacterTable.getInstance().restoreCharacter(name);
				if (pc != null) {
					result = result + name + getRankString(pc) + " ";
				}
			}
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}

	private static final String[] RANKNAME = {"", "[見習]", "[一般]", "[守護騎士]", "[聯盟君主]"};
	private String getRankString(L1PcInstance pc) {
		String rank = "";
		if (pc != null) {
			rank = RANKNAME[pc.getClanRank()];
		}
		return rank;
	}

	public String[] getAllMembers() {
		return membersNameList.toArray(new String[membersNameList.size()]);
	}

	public L1DwarfForClanInventory getDwarfForClanInventory() {
		return _dwarfForClan;
	}

	public int getWarehouseUsingChar() {
		return _warehouse;
	}

	public void setWarehouseUsingChar(int objid) {
		_warehouse = objid;
	}
}
