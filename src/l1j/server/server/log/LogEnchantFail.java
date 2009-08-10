/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1ItemInstance;

public class LogEnchantFail {
	private static Logger _log = Logger.getLogger(LogEnchantFail.class.getName());
/*
	public void storeLogEnchantFail(L1PcInstance pc, L1ItemInstance item) {
		File file = new File("log/EnchantFail.txt");
		boolean fileex = file.exists();
		if (!fileex) {
			File file2 = new File("log/");
			file2.mkdirs();
			DataOutputStream out = null;
			String ditem = null;

			Date time1 = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				out = new DataOutputStream(new FileOutputStream("log/EnchantFail.txt"));
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				out.write("#                                         EnchantFail.                                   #\r\n".getBytes());
				out.write("#                                                                                        #\r\n".getBytes());
				out.write("#----------------------------------------------------------------------------------------#\r\n".getBytes());
				ditem = fm + "	";
				out.write(ditem.getBytes());
				ditem = pc.getNetConnection().getIp() + "	";
				out.write(ditem.getBytes());
				ditem = pc.getAccountName() + "	";
				out.write(ditem.getBytes());
				ditem = pc.getId() + "	";
				out.write(ditem.getBytes());
				ditem = pc.getName() + "	";
				out.write(ditem.getBytes());
				ditem = item.getId() + "	";
				out.write(ditem.getBytes());
				ditem = item.getItem().getName() + "	";
				out.write(ditem.getBytes());
				ditem = item.getEnchantLevel() + "	";
				out.write(ditem.getBytes());
				ditem = item.getCount() + "\r\n";
				out.write(ditem.getBytes());
			} catch (Exception e) {
				_log.warn("enchantfail log outofstream error:" + e);
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (Exception e1) {
				}
			}
		} else {
			RandomAccessFile rfile = null;
			String ditem = null;

			Date time1 = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String fm = formatter.format(time1.getTime());
			try {
				rfile = new RandomAccessFile("log/EnchantFail.txt", "rw");
				rfile.seek(rfile.length());

				ditem = fm + "	";
				rfile.writeBytes(ditem);
				ditem = pc.getNetConnection().getIp() + "	";
				rfile.writeBytes(ditem);
				ditem = pc.getAccountName() + "	";
				rfile.writeBytes(ditem);
				ditem = pc.getId() + "	";
				rfile.writeBytes(ditem);
				ditem = pc.getName() + "	";
				rfile.writeBytes(encode(ditem));
				ditem = item.getId() + "	";
				rfile.writeBytes(ditem);
				ditem = item.getItem().getName() + "	";
				rfile.writeBytes(encode(ditem));
				ditem = item.getEnchantLevel() + "	";
				rfile.writeBytes(ditem);
				ditem = item.getCount() + "\r\n";
				rfile.writeBytes(ditem);
			} catch (Exception e) {
				_log.warn("enchantfail log randomacess error:" + e);
				e.printStackTrace();
			} finally {
				try {
					rfile.close();
				} catch (Exception e1) {
				}
			}
		}
	}

	public static String encode(String str) {
		String result = "";
		try {
			if (str == null)
				return result;
			result = new String(str.getBytes("UTF-8"), "8859_1");
		} catch (java.io.UnsupportedEncodingException e) {
		}
		return result;
	}
*/
}