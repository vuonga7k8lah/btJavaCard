package AppletLuuDiem;

import javacard.framework.*;

public class AppletLuuDiem extends Applet
{

	
	final static byte SV_ID_LENGTH 	=(byte)0x04;
	private byte[] diemThi, sinhVien, position;		
	private static byte soMonThi;
	short len_diemThi;
	final static byte SV_CLA 		= (byte)0xA0;
	final static byte INS_NHAP_DIEM = (byte)0x01;
	final static byte INS_IN_DIEM 	= (byte)0x02;
	final static byte INS_SUA_DIEM 	= (byte)0x03;
	final static byte INS_XOA_DIEM 	= (byte)0x04;
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new AppletLuuDiem(bArray, bOffset, bLength);
	}

	private AppletLuuDiem(byte[] bArray, short bOffset, byte bLength) {
		byte iLen = bArray[bOffset];	
		if (iLen == 0) {
			register();
		} else {
			
			register(bArray, (short) (bOffset + 1), iLen);
		}
		bOffset = (short) (bOffset + iLen + 1);	
		byte cLen = bArray[bOffset];	
		bOffset = (short) (bOffset + cLen + 1);	
		byte aLen = bArray[bOffset];	
		bOffset = (short) (bOffset + 1);	
		if (aLen != 0) {
			sinhVien = new byte[SV_ID_LENGTH];
			Util.arrayCopy(bArray, bOffset, sinhVien, (byte)0, SV_ID_LENGTH);	
			bOffset += SV_ID_LENGTH;
			soMonThi = bArray[bOffset];
		} else {
			// gan ID cua sinh vien va so luong mon thi
			sinhVien = new byte[] {'S', 'V', '0', '1'};
			soMonThi = (byte) 0x09;
		}
		// set byte cho mang diemThi
		    diemThi = new byte[20];
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		
		
		if (buf[ISO7816.OFFSET_CLA] != SV_CLA)
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		
		short input_data_length = (short)(buf[ISO7816.OFFSET_LC]&0xff);
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00:
			//in ra ID cua sinh vien va so luong mon thi
			apdu.setOutgoing();
			apdu.setOutgoingLength((short)5);
			apdu.sendBytesLong(sinhVien, (short)0, SV_ID_LENGTH);
			buf[0] = soMonThi;
			apdu.sendBytes((short)0, (short)1);
			break;
			
		case INS_NHAP_DIEM:
			
			if(input_data_length <= (short) 0x09) {
				
				if( (input_data_length % 2) == 0) {
					Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, diemThi, (short) 0, (short) input_data_length);
					len_diemThi = input_data_length;
					
					apdu.setOutgoing();
					apdu.setOutgoingLength((short)input_data_length);
					apdu.sendBytesLong(diemThi, (short)0, input_data_length);
				}
				
			} else {
				// so mon thi nhap vao > 9
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			}
			break;
			
			
		case INS_IN_DIEM:
			apdu.setOutgoing();
			apdu.setOutgoingLength((short)(len_diemThi));
			apdu.sendBytesLong(diemThi, (short)0, (short)len_diemThi);
			break;
			
		case INS_SUA_DIEM:
			byte x = buf[ISO7816.OFFSET_P1];
			
			for(byte i = 0; i <= len_diemThi; i+=2) {
				if( diemThi[i] == (byte) x ) {
					
					diemThi[i+1] =  buf[ISO7816.OFFSET_P2];
					
					apdu.setOutgoing();
					apdu.setOutgoingLength((short)len_diemThi);
					apdu.sendBytesLong(diemThi, (short)0, (short)len_diemThi);
					break;
				}
			}

			break;
		case INS_XOA_DIEM:
			byte y = buf[ISO7816.OFFSET_P1];
			short position = (short) 9*2;
			
			for(byte i = 0; i <= len_diemThi; i+=2) {
				if( diemThi[i] == (byte) y ) {
					position = i;
					break;
				}
			}
			
			byte[] buffer = new byte[20];
			Util.arrayCopy(diemThi, (short)0, buffer, (short)0, (short)position);
			Util.arrayCopy(diemThi, (short)(position+2), buffer, (short)(position), (short)(len_diemThi - position + 1));
			Util.arrayCopy(buffer, (short)0, diemThi, (short)0, (short)buffer.length);
			len_diemThi = (short) (len_diemThi - 2);		
			apdu.setOutgoing();
			apdu.setOutgoingLength((short)len_diemThi);
			apdu.sendBytesLong(diemThi, (short)0, (short)len_diemThi);
			
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}