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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javolution.util.FastMap;

import net.l1j.server.model.basisfunction.FaceInto;
import net.l1j.server.model.instance.L1DollInstance;
import net.l1j.server.model.instance.L1FollowerInstance;
import net.l1j.server.model.instance.L1ItemInstance;
import net.l1j.server.model.instance.L1NpcInstance;
import net.l1j.server.model.instance.L1PcInstance;
import net.l1j.server.model.instance.L1PetInstance;
import net.l1j.server.model.instance.L1SummonInstance;
import net.l1j.server.model.map.L1Map;
import net.l1j.server.model.poison.L1Poison;
import net.l1j.server.model.skill.SkillTimer;
import net.l1j.server.model.skill.SkillTimerCreator;
import net.l1j.server.serverpackets.S_PetGUI;
import net.l1j.server.serverpackets.S_Light;
import net.l1j.server.serverpackets.S_Poison;
import net.l1j.server.serverpackets.S_RemoveObject;
import net.l1j.server.serverpackets.ServerBasePacket;
import net.l1j.server.types.Base;
import net.l1j.server.types.Point;
import net.l1j.util.IntRange;
import net.l1j.util.MoveUtil;
import static net.l1j.server.model.skill.SkillId.*;

public class L1Character extends L1Object {
	private static final long serialVersionUID = 1L;

	private L1Poison _poison = null;
	private boolean _paralyzed;
	private boolean _sleeped;
	private L1PcInstance _petMaster;

	private final Map<Integer, L1NpcInstance> _petlist = new FastMap<Integer, L1NpcInstance>();
	private final Map<Integer, L1DollInstance> _dolllist = new FastMap<Integer, L1DollInstance>();
	private final Map<Integer, SkillTimer> _skillEffect = new FastMap<Integer, SkillTimer>();
	private final Map<Integer, L1ItemDelay.ItemDelayTimer> _itemdelay = new FastMap<Integer, L1ItemDelay.ItemDelayTimer>();
	private final Map<Integer, L1FollowerInstance> _followerlist = new FastMap<Integer, L1FollowerInstance>();

	public L1Character() {
		_level = 1;
	}

	/**
	 * キャラクターを復活させる。
	 * 
	 * @param hp 復活後のHP
	 */
	public void resurrect(int hp) {
		if (!isDead()) {
			return;
		}
		if (hp <= 0) {
			hp = 1;
		}
		setCurrentHp(hp);
		setDead(false);
		setStatus(0);
		L1PolyMorph.undoPoly(this);
		for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(this)) {
			pc.sendPackets(new S_RemoveObject(this));
			pc.removeKnownObject(this);
			pc.updateObject();
		}
	}

	private int _currentHp;

	/**
	 * キャラクターの現在のHPを返す。
	 * 
	 * @return 現在のHP
	 */
	public int getCurrentHp() {
		return _currentHp;
	}

	/**
	 * キャラクターのHPを設定する。
	 * 
	 * @param i キャラクターの新しいHP
	 */
	// 特殊な處理がある場合はこっちをオーバライド（パケット送信等）
	public void setCurrentHp(int i) {
		_currentHp = i;
		if (_currentHp >= getMaxHp()) {
			_currentHp = getMaxHp();
		}
	}

	/**
	 * キャラクターのHPを設定する。
	 * 
	 * @param i キャラクターの新しいHP
	 */
	public void setCurrentHpDirect(int i) {
		_currentHp = i;
	}

	private int _currentMp;

	/**
	 * キャラクターの現在のMPを返す。
	 * 
	 * @return 現在のMP
	 */
	public int getCurrentMp() {
		return _currentMp;
	}

	/**
	 * キャラクターのMPを設定する。
	 * 
	 * @param i キャラクターの新しいMP
	 */
	// 特殊な處理がある場合はこっちをオーバライド（パケット送信等）
	public void setCurrentMp(int i) {
		_currentMp = i;
		if (_currentMp >= getMaxMp()) {
			_currentMp = getMaxMp();
		}
	}

	/**
	 * キャラクターのMPを設定する。
	 * 
	 * @param i キャラクターの新しいMP
	 */
	public void setCurrentMpDirect(int i) {
		_currentMp = i;
	}

	/**
	 * キャラクターの眠り狀態を返す。
	 * 
	 * @return 眠り狀態を表す值。眠り狀態であればtrue。
	 */
	public boolean isSleeped() {
		return _sleeped;
	}

	/**
	 * キャラクターの眠り狀態を設定する。
	 * 
	 * @param sleeped 眠り狀態を表す值。眠り狀態であればtrue。
	 */
	public void setSleeped(boolean sleeped) {
		_sleeped = sleeped;
	}

	/**
	 * キャラクターの麻痺狀態を返す。
	 * 
	 * @return 麻痺狀態を表す值。麻痺狀態であればtrue。
	 */
	public boolean isParalyzed() {
		return _paralyzed;
	}

	/**
	 * キャラクターの麻痺狀態を設定する。
	 * 
	 * @param i 麻痺狀態を表す值。麻痺狀態であればtrue。
	 */
	public void setParalyzed(boolean paralyzed) {
		_paralyzed = paralyzed;
	}

	L1Paralysis _paralysis;

	public L1Paralysis getParalysis() {
		return _paralysis;
	}

	public void setParalaysis(L1Paralysis p) {
		_paralysis = p;
	}

	public void cureParalaysis() {
		if (_paralysis != null) {
			_paralysis.cure();
		}
	}

	/**
	 * キャラクターの可視範圍に居るプレイヤーへ、パケットを送信する。
	 * 
	 * @param packet 送信するパケットを表すServerBasePacketオブジェクト。
	 */
	public void broadcastPacket(ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * キャラクターの可視範圍に居るプレイヤーへ、パケットを送信する。 ただしターゲットの畫面內には送信しない。
	 * 
	 * @param packet 送信するパケットを表すServerBasePacketオブジェクト。
	 */
	public void broadcastPacketExceptTargetSight(ServerBasePacket packet, L1Character target) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayerExceptTargetSight(this, target)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * キャラクターの可視範囲でインビジを見破れるor見破れないプレイヤーを区別して、パケットを送信する。
	 * 
	 * @param packet 送信するパケットを表すServerBasePacketオブジェクト。
	 * @param isFindInvis true : 見破れるプレイヤーにだけパケットを送信する。 false :
	 *            見破れないプレイヤーにだけパケットを送信する。
	 */
	public void broadcastPacketForFindInvis(ServerBasePacket packet, boolean isFindInvis) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this)) {
			if (isFindInvis) {
				if (pc.hasSkillEffect(GMSTATUS_FINDINVIS)) {
					pc.sendPackets(packet);
				}
			} else {
				if (!pc.hasSkillEffect(GMSTATUS_FINDINVIS)) {
					pc.sendPackets(packet);
				}
			}
		}
	}

	/**
	 * キャラクターの50マス以內に居るプレイヤーへ、パケットを送信する。
	 * 
	 * @param packet 送信するパケットを表すServerBasePacketオブジェクト。
	 */
	public void wideBroadcastPacket(ServerBasePacket packet) {
		for (L1PcInstance pc : L1World.getInstance().getVisiblePlayer(this, 50)) {
			pc.sendPackets(packet);
		}
	}

	/**
	 * キャラクターの正面の座標を返す。
	 * 
	 * @return 正面の座標
	 */
	public int[] getFrontLoc() {
		int[] loc = {getX(), getY()};
		MoveUtil.MoveLoc(loc, getHeading());
		return loc;
	}

	/**
	 * 指定された座標に對する方向を返す。
	 * 
	 * @param tx 座標のX值
	 * @param ty 座標のY值
	 * @return 指定された座標に對する方向
	 */
	public int targetDirection(int tx, int ty) {
		return FaceInto.getFace(getX(), getY(), getHeading(), tx ,ty);
	}

	/**
	 * 指定された座標までの直線上に、障害物が存在*しないか*を返す。
	 * 
	 * @param tx 座標のX值
	 * @param ty 座標のY值
	 * @return 障害物が無ければtrue、あればfalseを返す。
	 */
	public boolean glanceCheck(int tx, int ty) {
		L1Map map = getMap();
		int chx = getX();
		int chy = getY();
		for (int i = 0; i < 15; i++) {
			int tempx = chx - tx, tempy = chy - ty;

			if ((tempx * tempx + tempy * tempy) <= 2) // 使用畢氏定理 判斷鄰近1格的條件
				break;

			int _dir = targetDirection(tx, ty);
			if (!map.isArrowPassable(chx, chy, _dir))
				return false;

			chx += MoveUtil.MoveX(_dir);
			chy += MoveUtil.MoveY(_dir);
		}
		return true;
	}

	/**
	 * 指定された座標へ攻擊可能であるかを返す。
	 * 
	 * @param x 座標のX值。
	 * @param y 座標のY值。
	 * @param range 攻擊可能な範圍(タイル數)
	 * @return 攻擊可能であればtrue,不可能であればfalse
	 */
	public boolean isAttackPosition(int x, int y, int range) {
		if (range >= 7) { // 遠隔武器（７以上の場合斜めを考慮すると畫面外に出る)
			if (getLocation().getTileDistance(new Point(x, y)) > range) {
				return false;
			}
		} else { // 近接武器
			if (getLocation().getTileLineDistance(new Point(x, y)) > range) {
				return false;
			}
		}
		return glanceCheck(x, y);
	}

	/**
	 * キャラクターのインベントリを返す。
	 * 
	 * @return キャラクターのインベントリを表す、L1Inventoryオブジェクト。
	 */
	public L1Inventory getInventory() {
		return null;
	}

	private int _charStatus = 0;
	/**
	 * 特殊狀態速查
	 * 
	 * 將人身上特有的特殊狀態製成索引，降低 普通/魔法 攻擊時，需要一一比對身上技能的煩惱。
	 */
	public void addInvincibleEffect(int statusId) {
		if((_charStatus & statusId) != statusId)
			_charStatus ^= statusId;
	}

	public void removeInvincibleEffect(int statusId) {
		if((_charStatus & statusId) == statusId)
			_charStatus ^= statusId;
	}

	public boolean hasInvincibleEffect() {
		if(_charStatus == 0)
			return false;
		else
			return true;
	}

	public boolean hasInvincibleEffect(int statusId) {
		if((_charStatus & statusId) != statusId)
			return false;
		else
			return true;
	}

	public int getInvincibleEffect() {
		return _charStatus;
	}

	/**
	 * キャラクターへ、新たにスキル效果を追加する。
	 * 
	 * @param skillId 追加する效果のスキルID。
	 * @param timeMillis 追加する效果の持續時間。無限の場合は0。
	 */
	private void addSkillEffect(int skillId, int timeMillis) {
		SkillTimer timer = null;
		if (0 < timeMillis) {
			timer = SkillTimerCreator.create(this, skillId, timeMillis);
			timer.begin();
		}
		_skillEffect.put(skillId, timer);
	}

	/** 解除絕對屏障效果 */
	public void cancelAbsoluteBarrier() {
		if (hasInvincibleEffect(TRANSFORM_SKILL_ABSOLUTE_BARRIER)) {
			removeSkillEffect(SKILL_ABSOLUTE_BARRIER);
		}
	}

	/**
	 * キャラクターへ、スキル效果を設定する。<br>
	 * 重複するスキルがない場合は、新たにスキル效果を追加する。<br>
	 * 重複するスキルがある場合は、殘り效果時間とパラメータの效果時間の長い方を優先して設定する。
	 * 
	 * @param skillId 設定する效果のスキルID。
	 * @param timeMillis 設定する效果の持續時間。無限の場合は0。
	 */
	public void setSkillEffect(int skillId, int timeMillis) {
		if (hasSkillEffect(skillId)) {
			int remainingTimeMills = getSkillEffectTimeSec(skillId) * 1000;

			// 殘り時間が有限で、パラメータの效果時間の方が長いか無限の場合は上書きする。
			if (remainingTimeMills >= 0 && (remainingTimeMills < timeMillis || timeMillis == 0)) {
				killSkillEffectTimer(skillId);
				addSkillEffect(skillId, timeMillis);
			}
		} else {
			addSkillEffect(skillId, timeMillis);
		}
	}

	/**
	 * キャラクターから、スキル效果を削除する。
	 * 
	 * @param skillId 削除する效果のスキルID
	 */
	public void removeSkillEffect(int skillId) {
		SkillTimer timer = _skillEffect.remove(skillId);
		if (timer != null) {
			timer.end();
		}
	}

	/**
	 * キャラクターから、スキル效果のタイマーを削除する。 スキル效果は削除されない。
	 * 
	 * @param skillId 削除するタイマーのスキルＩＤ
	 */
	public void killSkillEffectTimer(int skillId) {
		SkillTimer timer = _skillEffect.remove(skillId);
		if (timer != null) {
			timer.kill();
		}
	}

	/**
	 * キャラクターから、全てのスキル效果タイマーを削除する。スキル效果は削除されない。
	 */
	public void clearSkillEffectTimer() {
		for (SkillTimer timer : _skillEffect.values()) {
			if (timer != null) {
				timer.kill();
			}
		}
		_skillEffect.clear();
	}

	/**
	 * キャラクターに、スキル效果が掛かっているかを返す。
	 * 
	 * @param skillId 調べる效果のスキルID。
	 * @return 魔法效果があればtrue、なければfalse。
	 */
	public boolean hasSkillEffect(int skillId) {
		return _skillEffect.containsKey(skillId);
	}

	/**
	 * キャラクターのスキル效果の持續時間を返す。
	 * 
	 * @param skillId 調べる效果のスキルID
	 * @return スキル效果の殘り時間(秒)。スキルがかかっていないか效果時間が無限の場合、-1。
	 */
	public int getSkillEffectTimeSec(int skillId) {
		SkillTimer timer = _skillEffect.get(skillId);
		if (timer == null) {
			return -1;
		}
		return timer.getRemainingTime();
	}

	private boolean _isSkillDelay = false;

	/**
	 * キャラクターへ、スキルディレイを追加する。
	 * 
	 * @param flag
	 */
	public void setSkillDelay(boolean flag) {
		_isSkillDelay = flag;
	}

	/**
	 * キャラクターの毒狀態を返す。
	 * 
	 * @return スキルディレイ中か。
	 */
	public boolean isSkillDelay() {
		return _isSkillDelay;
	}

	/**
	 * キャラクターへ、アイテムディレイを追加する。
	 * 
	 * @param delayId アイテムディレイID。 通常のアイテムであれば0、インビジビリティ クローク、バルログ ブラッディ
	 *            クロークであれば1。
	 * @param timer ディレイ時間を表す、L1ItemDelay.ItemDelayTimerオブジェクト。
	 */
	public void addItemDelay(int delayId, L1ItemDelay.ItemDelayTimer timer) {
		_itemdelay.put(delayId, timer);
	}

	/**
	 * キャラクターから、アイテムディレイを削除する。
	 * 
	 * @param delayId アイテムディレイID。 通常のアイテムであれば0、インビジビリティ クローク、バルログ ブラッディ
	 *            クロークであれば1。
	 */
	public void removeItemDelay(int delayId) {
		_itemdelay.remove(delayId);
	}

	/**
	 * キャラクターに、アイテムディレイがあるかを返す。
	 * 
	 * @param delayId 調べるアイテムディレイID。 通常のアイテムであれば0、インビジビリティ クローク、バルログ ブラッディ
	 *            クロークであれば1。
	 * @return アイテムディレイがあればtrue、なければfalse。
	 */
	public boolean hasItemDelay(int delayId) {
		return _itemdelay.containsKey(delayId);
	}

	/**
	 * キャラクターのアイテムディレイ時間を表す、L1ItemDelay.ItemDelayTimerを返す。
	 * 
	 * @param delayId 調べるアイテムディレイID。 通常のアイテムであれば0、インビジビリティ クローク、バルログ ブラッディ
	 *            クロークであれば1。
	 * @return アイテムディレイ時間を表す、L1ItemDelay.ItemDelayTimer。
	 */
	public L1ItemDelay.ItemDelayTimer getItemDelayTimer(int delayId) {
		return _itemdelay.get(delayId);
	}

	/**
	 * キャラクターへ、新たにペット、サモンモンスター、テイミングモンスター、あるいはクリエイトゾンビを追加する。
	 * 
	 * @param npc 追加するNpcを表す、L1NpcInstanceオブジェクト。
	 */
	public void addPet(L1NpcInstance npc) {
		_petlist.put(npc.getId(), npc);
		if (npc instanceof L1SummonInstance) {
			L1SummonInstance summon = (L1SummonInstance) npc;
			L1Character cha = summon.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_PetGUI(3));
				cha.setPetUI(true);
			}
		}
	}

	/**
	 * キャラクターから、ペット、サモンモンスター、テイミングモンスター、あるいはクリエイトゾンビを削除する。
	 * 
	 * @param npc 削除するNpcを表す、L1NpcInstanceオブジェクト。
	 */
	public void removePet(L1NpcInstance npc) {
		_petlist.remove(npc.getId());
		if (npc instanceof L1PetInstance) {
			L1PetInstance pet = (L1PetInstance) npc;
			L1Character cha = pet.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_PetGUI(0));
				cha.setPetUI(false);
			}
		} else if (npc instanceof L1SummonInstance) {
			L1SummonInstance summon = (L1SummonInstance) npc;
			L1Character cha = summon.getMaster();
			if (cha instanceof L1PcInstance) {
				L1PcInstance pc = (L1PcInstance) cha;
				pc.sendPackets(new S_PetGUI(0));
				cha.setPetUI(false);
			}
		}
	}

	/**
	 * キャラクターのペットリストを返す。
	 * @return
	 *         キャラクターのペットリストを表す、HashMapオブジェクト。このオブジェクトのKeyはオブジェクトID、ValueはL1NpcInstance。
	 */
	public Map<Integer, L1NpcInstance> getPetList() {
		return _petlist;
	}

	/**
	 * キャラクターへマジックドールを追加する。
	 * 
	 * @param doll 追加するdollを表す、L1DollInstanceオブジェクト。
	 */
	public void addDoll(L1DollInstance doll) {
		_dolllist.put(doll.getId(), doll);
	}

	/**
	 * キャラクターからマジックドールを削除する。
	 * 
	 * @param doll 削除するdollを表す、L1DollInstanceオブジェクト。
	 */
	public void removeDoll(L1DollInstance doll) {
		_dolllist.remove(doll.getId());
	}

	/**
	 * キャラクターのマジックドールリストを返す。
	 * 
	 * @return キャラクターの魔法人形リストを表す、HashMapオブジェクト。このオブジェクトのKeyはオブジェクトID、
	 *         ValueはL1DollInstance。
	 */
	public Map<Integer, L1DollInstance> getDollList() {
		return _dolllist;
	}

	/**
	 * キャラクターへ從者を追加する。
	 * 
	 * @param follower 追加するfollowerを表す、L1FollowerInstanceオブジェクト。
	 */
	public void addFollower(L1FollowerInstance follower) {
		_followerlist.put(follower.getId(), follower);
	}

	/**
	 * キャラクターから從者を削除する。
	 * 
	 * @param follower 削除するfollowerを表す、L1FollowerInstanceオブジェクト。
	 */
	public void removeFollower(L1FollowerInstance follower) {
		_followerlist.remove(follower.getId());
	}

	/**
	 * キャラクターの從者リストを返す。
	 * 
	 * @return キャラクターの從者リストを表す、HashMapオブジェクト。このオブジェクトのKeyはオブジェクトID、
	 *         ValueはL1FollowerInstance。
	 */
	public Map<Integer, L1FollowerInstance> getFollowerList() {
		return _followerlist;
	}

	/**
	 * キャラクターへ、毒を追加する。
	 * 
	 * @param poison 毒を表す、L1Poisonオブジェクト。
	 */
	public void setPoison(L1Poison poison) {
		_poison = poison;
	}

	/**
	 * キャラクターの毒を治療する。
	 */
	public void curePoison() {
		if (_poison == null) {
			return;
		}
		_poison.cure();
	}

	/**
	 * キャラクターの毒狀態を返す。
	 * 
	 * @return キャラクターの毒を表す、L1Poisonオブジェクト。
	 */
	public L1Poison getPoison() {
		return _poison;
	}

	/**
	 * キャラクターへ毒のエフェクトを付加する
	 * 
	 * @param effectId
	 * @see S_Poison#S_Poison(int, int)
	 */
	public void setPoisonEffect(int effectId) {
		broadcastPacket(new S_Poison(getId(), effectId));
	}

	/**
	 * キャラクターが存在する座標が、どのゾーンに屬しているかを返す。
	 * 
	 * @return 座標のゾーンを表す值。セーフティーゾーンであれば1、コンバットゾーンであれば-1、ノーマルゾーンであれば0。
	 */
	public int getZoneType() {
		if (getMap().isSafetyZone(getLocation())) {
			return 1;
		} else if (getMap().isCombatZone(getLocation())) {
			return -1;
		} else { // ノーマルゾーン
			return 0;
		}
	}

	private int _exp; // ● 經驗值

	/**
	 * キャラクターが保持している經驗值を返す。
	 * 
	 * @return 經驗值。
	 */
	public int getExp() {
		return _exp;
	}

	/**
	 * キャラクターが保持する經驗值を設定する。
	 * 
	 * @param exp 經驗值。
	 */
	public void setExp(int exp) {
		_exp = exp;
	}

	// ■■■■■■■■■■ L1PcInstanceへ移動するプロパティ ■■■■■■■■■■
	private final List<L1Object> _knownObjects = new CopyOnWriteArrayList<L1Object>();
	private final List<L1PcInstance> _knownPlayer = new CopyOnWriteArrayList<L1PcInstance>();

	/**
	 * 指定されたオブジェクトを、キャラクターが認識しているかを返す。
	 * 
	 * @param obj 調べるオブジェクト。
	 * @return オブジェクトをキャラクターが認識していればtrue、していなければfalse。 自分自身に對してはfalseを返す。
	 */
	public boolean knownsObject(L1Object obj) {
		return _knownObjects.contains(obj);
	}

	/**
	 * キャラクターが認識している全てのオブジェクトを返す。
	 * 
	 * @return キャラクターが認識しているオブジェクトを表すL1Objectが格納されたArrayList。
	 */
	public List<L1Object> getKnownObjects() {
		return _knownObjects;
	}

	/**
	 * キャラクターが認識している全てのプレイヤーを返す。
	 * 
	 * @return キャラクターが認識しているオブジェクトを表すL1PcInstanceが格納されたArrayList。
	 */
	public List<L1PcInstance> getKnownPlayers() {
		return _knownPlayer;
	}

	/**
	 * キャラクターに、新たに認識するオブジェクトを追加する。
	 * 
	 * @param obj 新たに認識するオブジェクト。
	 */
	public void addKnownObject(L1Object obj) {
		if (!_knownObjects.contains(obj)) {
			_knownObjects.add(obj);
			if (obj instanceof L1PcInstance) {
				_knownPlayer.add((L1PcInstance) obj);
			}
		}
	}

	/**
	 * キャラクターから、認識しているオブジェクトを削除する。
	 * 
	 * @param obj 削除するオブジェクト。
	 */
	public void removeKnownObject(L1Object obj) {
		_knownObjects.remove(obj);
		if (obj instanceof L1PcInstance) {
			_knownPlayer.remove(obj);
		}
	}

	/**
	 * キャラクターから、全ての認識しているオブジェクトを削除する。
	 */
	public void removeAllKnownObjects() {
		_knownObjects.clear();
		_knownPlayer.clear();
	}

	// ■■■■■■■■■■ プロパティ ■■■■■■■■■■

	private String _name; // ● 名前

	public String getName() {
		return _name;
	}

	public void setName(String s) {
		_name = s;
	}

	private int _level; // ● レベル

	public synchronized int getLevel() {
		return _level;
	}

	public synchronized void setLevel(int level) {
		_level = level;
	}

	private int _maxHp = 0; // ● ＭＡＸＨＰ（1～32767）
	private int _trueMaxHp = 0; // ● 本當のＭＡＸＨＰ

	public int getMaxHp() {
		return _maxHp;
	}

	public void setMaxHp(int hp) {
		_trueMaxHp = hp;
		_maxHp = IntRange.ensure(_trueMaxHp, 1, 32767);
		_currentHp = Math.min(_currentHp, _maxHp);
	}

	public void addMaxHp(int i) {
		setMaxHp(_trueMaxHp + i);
	}

	private int _maxMp = 0; // ● ＭＡＸＭＰ（0～32767）
	private int _trueMaxMp = 0; // ● 本當のＭＡＸＭＰ

	public int getMaxMp() {
		return _maxMp;
	}

	public void setMaxMp(int mp) {
		_trueMaxMp = mp;
		_maxMp = IntRange.ensure(_trueMaxMp, 0, 32767);
		_currentMp = Math.min(_currentMp, _maxMp);
	}

	public void addMaxMp(int i) {
		setMaxMp(_trueMaxMp + i);
	}

	protected int _ac = 0; // ● ＡＣ（-128～127） // waja 註:原寫法  private int _ac = 0;
	private int _trueAc = 0; // ● 本當のＡＣ

	public int getAc() {
		return _ac;
	}

	public void setAc(int i) {
		_trueAc = i;
		_ac = IntRange.ensure(i, -128, 127);
	}

	public void addAc(int i) {
		setAc(_trueAc + i);
	}

	private int _str = 0; // ● ＳＴＲ（1～127）
	private int _trueStr = 0; // ● 本當のＳＴＲ

	public int getStr() {
		return _str;
	}

	public void setStr(int i) {
		_trueStr = i;
		_str = IntRange.ensure(i, 1, 127);
	}

	public void addStr(int i) {
		setStr(_trueStr + i);
	}

	private int _con = 0; // ● ＣＯＮ（1～127）
	private int _trueCon = 0; // ● 本當のＣＯＮ

	public int getCon() {
		return _con;
	}

	public void setCon(int i) {
		_trueCon = i;
		_con = IntRange.ensure(i, 1, 127);
	}

	public void addCon(int i) {
		setCon(_trueCon + i);
	}

	private int _dex = 0; // ● ＤＥＸ（1～127）
	private int _trueDex = 0; // ● 本當のＤＥＸ

	public int getDex() {
		return _dex;
	}

	public void setDex(int i) {
		_trueDex = i;
		_dex = IntRange.ensure(i, 1, 127);
	}

	public void addDex(int i) {
		setDex(_trueDex + i);
	}

	private int _cha = 0; // ● ＣＨＡ（1～127）
	private int _trueCha = 0; // ● 本當のＣＨＡ

	public int getCha() {
		return _cha;
	}

	public void setCha(int i) {
		_trueCha = i;
		_cha = IntRange.ensure(i, 1, 127);
	}

	public void addCha(int i) {
		setCha(_trueCha + i);
	}

	private int _int = 0; // ● ＩＮＴ（1～127）
	private int _trueInt = 0; // ● 本當のＩＮＴ

	public int getInt() {
		return _int;
	}

	public void setInt(int i) {
		_trueInt = i;
		_int = IntRange.ensure(i, 1, 127);
	}

	public void addInt(int i) {
		setInt(_trueInt + i);
	}

	private int _wis = 0; // ● ＷＩＳ（1～127）
	private int _trueWis = 0; // ● 本當のＷＩＳ

	public int getWis() {
		return _wis;
	}

	public void setWis(int i) {
		_trueWis = i;
		_wis = IntRange.ensure(i, 1, 127);
	}

	public void addWis(int i) {
		setWis(_trueWis + i);
	}

	private int _wind = 0; // ● 風防御（-128～127）
	private int _trueWind = 0; // ● 本當の風防御

	public int getWind() {
		return _wind;
	} // 使用するとき

	public void addWind(int i) {
		_trueWind += i;
		if (_trueWind >= 127) {
			_wind = 127;
		} else if (_trueWind <= -128) {
			_wind = -128;
		} else {
			_wind = _trueWind;
		}
	}

	private int _water = 0; // ● 水防御（-128～127）
	private int _trueWater = 0; // ● 本當の水防御

	public int getWater() {
		return _water;
	} // 使用するとき

	public void addWater(int i) {
		_trueWater += i;
		if (_trueWater >= 127) {
			_water = 127;
		} else if (_trueWater <= -128) {
			_water = -128;
		} else {
			_water = _trueWater;
		}
	}

	private int _fire = 0; // ● 火防御（-128～127）
	private int _trueFire = 0; // ● 本當の火防御

	public int getFire() {
		return _fire;
	} // 使用するとき

	public void addFire(int i) {
		_trueFire += i;
		if (_trueFire >= 127) {
			_fire = 127;
		} else if (_trueFire <= -128) {
			_fire = -128;
		} else {
			_fire = _trueFire;
		}
	}

	private int _earth = 0; // ● 地防御（-128～127）
	private int _trueEarth = 0; // ● 本當の地防御

	public int getEarth() {
		return _earth;
	} // 使用するとき

	public void addEarth(int i) {
		_trueEarth += i;
		if (_trueEarth >= 127) {
			_earth = 127;
		} else if (_trueEarth <= -128) {
			_earth = -128;
		} else {
			_earth = _trueEarth;
		}
	}

	private int _addAttrKind; // エレメンタルフォールダウンで減少した屬性の種類

	public int getAddAttrKind() {
		return _addAttrKind;
	}

	public void setAddAttrKind(int i) {
		_addAttrKind = i;
	}

	// スタン耐性
	private int _registStun = 0;
	private int _trueRegistStun = 0;

	public int getRegistStun() {
		return _registStun;
	} // 使用するとき

	public void addRegistStun(int i) {
		_trueRegistStun += i;
		if (_trueRegistStun > 127) {
			_registStun = 127;
		} else if (_trueRegistStun < -128) {
			_registStun = -128;
		} else {
			_registStun = _trueRegistStun;
		}
	}

	// 石化耐性
	private int _registStone = 0;
	private int _trueRegistStone = 0;

	public int getRegistStone() {
		return _registStone;
	} // 使用するとき

	public void addRegistStone(int i) {
		_trueRegistStone += i;
		if (_trueRegistStone > 127) {
			_registStone = 127;
		} else if (_trueRegistStone < -128) {
			_registStone = -128;
		} else {
			_registStone = _trueRegistStone;
		}
	}

	// 睡眠耐性
	private int _registSleep = 0;
	private int _trueRegistSleep = 0;

	public int getRegistSleep() {
		return _registSleep;
	} // 使用するとき

	public void addRegistSleep(int i) {
		_trueRegistSleep += i;
		if (_trueRegistSleep > 127) {
			_registSleep = 127;
		} else if (_trueRegistSleep < -128) {
			_registSleep = -128;
		} else {
			_registSleep = _trueRegistSleep;
		}
	}

	// 凍結耐性
	private int _registFreeze = 0;
	private int _trueRegistFreeze = 0;

	public int getRegistFreeze() {
		return _registFreeze;
	} // 使用するとき

	public void add_regist_freeze(int i) {
		_trueRegistFreeze += i;
		if (_trueRegistFreeze > 127) {
			_registFreeze = 127;
		} else if (_trueRegistFreeze < -128) {
			_registFreeze = -128;
		} else {
			_registFreeze = _trueRegistFreeze;
		}
	}

	// ホールド耐性
	private int _registSustain = 0;
	private int _trueRegistSustain = 0;

	public int getRegistSustain() {
		return _registSustain;
	} // 使用するとき

	public void addRegistSustain(int i) {
		_trueRegistSustain += i;
		if (_trueRegistSustain > 127) {
			_registSustain = 127;
		} else if (_trueRegistSustain < -128) {
			_registSustain = -128;
		} else {
			_registSustain = _trueRegistSustain;
		}
	}

	// 暗闇耐性
	private int _registBlind = 0;
	private int _trueRegistBlind = 0;

	public int getRegistBlind() {
		return _registBlind;
	} // 使用するとき

	public void addRegistBlind(int i) {
		_trueRegistBlind += i;
		if (_trueRegistBlind > 127) {
			_registBlind = 127;
		} else if (_trueRegistBlind < -128) {
			_registBlind = -128;
		} else {
			_registBlind = _trueRegistBlind;
		}
	}

	private int _dmgup = 0; // ● ダメージ補正（-128～127）
	private int _trueDmgup = 0; // ● 本當のダメージ補正

	public int getDmgup() {
		return _dmgup;
	} // 使用するとき

	public void addDmgup(int i) {
		_trueDmgup += i;
		if (_trueDmgup >= 127) {
			_dmgup = 127;
		} else if (_trueDmgup <= -128) {
			_dmgup = -128;
		} else {
			_dmgup = _trueDmgup;
		}
	}

	private int _bowDmgup = 0; // ● 弓ダメージ補正（-128～127）
	private int _trueBowDmgup = 0; // ● 本當の弓ダメージ補正

	public int getBowDmgup() {
		return _bowDmgup;
	} // 使用するとき

	public void addBowDmgup(int i) {
		_trueBowDmgup += i;
		if (_trueBowDmgup >= 127) {
			_bowDmgup = 127;
		} else if (_trueBowDmgup <= -128) {
			_bowDmgup = -128;
		} else {
			_bowDmgup = _trueBowDmgup;
		}
	}

	private int _hitup = 0; // ● 命中補正（-128～127）
	private int _trueHitup = 0; // ● 本當の命中補正

	public int getHitup() {
		return _hitup;
	} // 使用するとき

	public void addHitup(int i) {
		_trueHitup += i;
		if (_trueHitup >= 127) {
			_hitup = 127;
		} else if (_trueHitup <= -128) {
			_hitup = -128;
		} else {
			_hitup = _trueHitup;
		}
	}

	private int _bowHitup = 0; // ● 弓命中補正（-128～127）
	private int _trueBowHitup = 0; // ● 本當の弓命中補正

	public int getBowHitup() {
		return _bowHitup;
	} // 使用するとき

	public void addBowHitup(int i) {
		_trueBowHitup += i;
		if (_trueBowHitup >= 127) {
			_bowHitup = 127;
		} else if (_trueBowHitup <= -128) {
			_bowHitup = -128;
		} else {
			_bowHitup = _trueBowHitup;
		}
	}

	private int _mr = 0; // ● 魔法防御（0～）
	private int _trueMr = 0; // ● 本當の魔法防御

	public int getMr() {
		if (hasSkillEffect(153)) {
			return _mr / 4;
		} else {
			return _mr;
		}
	} // 使用するとき

	public int getTrueMr() {
		return _trueMr;
	} // セットするとき

	public void addMr(int i) {
		_trueMr += i;
		if (_trueMr <= 0) {
			_mr = 0;
		} else {
			_mr = _trueMr;
		}
	}

	private int _sp = 0; // ● 增加したＳＰ

	public int getSp() {
		return getTrueSp() + _sp;
	}

	public int getTrueSp() {
		return getMagicLevel() + getMagicBonus();
	}

	public void addSp(int i) {
		_sp += i;
	}

	private boolean _isDead; // ● 死亡狀態

	public boolean isDead() {
		return _isDead;
	}

	public void setDead(boolean flag) {
		_isDead = flag;
	}

	private boolean _petUI;

	public boolean PetUI() {
		return _petUI;
	}

	public void setPetUI(boolean flag) {
		_petUI = flag;
	}

	private int _status; // ● 狀態？

	public int getStatus() {
		return _status;
	}

	public void setStatus(int i) {
		_status = i;
	}

	private String _title; // ● タイトル

	public String getTitle() {
		return _title;
	}

	public void setTitle(String s) {
		_title = s;
	}

	private int _lawful; // ● アライメント

	public int getLawful() {
		return _lawful;
	}

	public void setLawful(int i) {
		_lawful = i;
	}

	public synchronized void addLawful(int i) {
		_lawful += i;
		if (_lawful > 32767) {
			_lawful = 32767;
		} else if (_lawful < -32768) {
			_lawful = -32768;
		}
	}

	private int _heading; // ● 向き 0.左上 1.上 2.右上 3.右 4.右下 5.下 6.左下 7.左

	public int getHeading() {
		return _heading;
	}

	public void setHeading(int i) {
		_heading = i;
	}

	private int _moveSpeed; // ● スピード 0.通常 1.ヘイスト 2.スロー

	public int getMoveSpeed() {
		return _moveSpeed;
	}

	public void setMoveSpeed(int i) {
		_moveSpeed = i;
	}

	private int _braveSpeed; // ● ブレイブ狀態 0.通常 1.ブレイブ

	public int getBraveSpeed() {
		return _braveSpeed;
	}

	public void setBraveSpeed(int i) {
		_braveSpeed = i;
	}

	private int _tempCharGfx; // ● ベースグラフィックＩＤ

	public int getTempCharGfx() {
		return _tempCharGfx;
	}

	public void setTempCharGfx(int i) {
		_tempCharGfx = i;
	}

	private int _gfxid; // ● グラフィックＩＤ

	public int getGfxId() {
		return _gfxid;
	}

	public void setGfxId(int i) {
		_gfxid = i;
	}

	public int getMagicLevel() {
		return getLevel() / 4;
	}

	public int getMagicBonus() {
		int i = getInt();
		if (i <= 5) {
			return -2;
		} else if (i <= 8) {
			return -1;
		} else if (i <= 11) {
			return 0;
		} else if (i <= 14) {
			return 1;
		} else if (i <= 17) {
			return 2;
		} else if (i <= 24) {
			return i - 15;
		} else if (i <= 35) {
			return 10;
		} else if (i <= 42) {
			return 11;
		} else if (i <= 49) {
			return 12;
		} else if (i <= 50) {
			return 13;
		} else {
			return i - 25;
		}
	}

	public boolean isInvisble() {
		return (hasSkillEffect(SKILL_INVISIBILITY) || hasSkillEffect(SKILL_BLIND_HIDING));
	}

	public void healHp(int pt) {
		setCurrentHp(getCurrentHp() + pt);
	}

	private int _karma;

	/**
	 * キャラクターが保持しているカルマを返す。
	 * 
	 * @return カルマ。
	 */
	public int getKarma() {
		return _karma;
	}

	/**
	 * キャラクターが保持するカルマを設定する。
	 * 
	 * @param karma カルマ。
	 */
	public void setKarma(int karma) {
		_karma = karma;
	}

	public void setMr(int i) {
		_trueMr = i;
		if (_trueMr <= 0) {
			_mr = 0;
		} else {
			_mr = _trueMr;
		}
	}

	public void turnOnOffLight() {
		int lightSize = 0;
		if (this instanceof L1NpcInstance) {
			L1NpcInstance npc = (L1NpcInstance) this;
			lightSize = npc.getLightSize(); // npc.sqlのライトサイズ
		}
		if (hasSkillEffect(SKILL_LIGHT)) {
			lightSize = 14;
		}

		for (L1ItemInstance item : getInventory().getItems()) {
			if (item.getItem().getType2() == 0 && item.getItem().getType() == 2) { // light系アイテム
				int itemlightSize = item.getItem().getLightRange();
				if (itemlightSize != 0 && item.isNowLighting()) {
					if (itemlightSize > lightSize) {
						lightSize = itemlightSize;
					}
				}
			}
		}

		if (this instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) this;
			pc.sendPackets(new S_Light(pc.getId(), lightSize));
		}
		if (!isInvisble()) {
			broadcastPacket(new S_Light(getId(), lightSize));
		}

		setOwnLightSize(lightSize); // S_OwnCharPackのライト範圍
		setChaLightSize(lightSize); // S_OtherCharPack, S_NPCPackなどのライト範圍
	}

	private int _chaLightSize; // ● ライトの範圍

	public int getChaLightSize() {
		if (isInvisble()) {
			return 0;
		}
		return _chaLightSize;
	}

	public void setChaLightSize(int i) {
		_chaLightSize = i;
	}

	private int _ownLightSize; // ● ライトの範圍(S_OwnCharPack用)

	public int getOwnLightSize() {
		return _ownLightSize;
	}

	public void setOwnLightSize(int i) {
		_ownLightSize = i;
	}

	/* 物件特別狀態 [2009/07/28] */
	private int State;

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		State = state;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return State;
	}
}