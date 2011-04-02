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
package net.l1j.server.serverpackets;

import net.l1j.server.Opcodes;
import net.l1j.server.model.instance.L1PcInstance;

public class S_PetGUI extends ServerBasePacket {

	private byte[] _byte = null;

        /*         
         * 0000	79 0c 03 00 00 00 00 00 00 00 00 00 47 32 32 29    y...........G22)
         *
         * 0000	79 0c 00 00 00 00 00 00 00 00 00 00 4c 6b 3f 30    y...........Lk?0
         */
	public S_PetGUI(int value) {
		writeC(Opcodes.S_OPCODE_PETGUI);
		writeC(0x0c);
		writeC(value); // 00:OFF or Die  03:Pet
                writeC(0x00);
                writeD(0x00000000);
                writeD(0x00000000);
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = _bao.toByteArray();
		}

		return _byte;
	}
}
