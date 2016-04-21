package cn.com.cig.adsense.utils.secret;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**   
 * @File: EDncryUtil.java 
 * @Package cn.com.cig.adsense.utils.secret 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月3日 上午11:23:21 
 * @version V1.0   
 */
public class EDncryUtil {
	private static Logger logger = LoggerFactory.getLogger(EDncryUtil.class);
	public static String MD5(String inStr){
		MessageDigest md5 = null;
		try{
			md5 = MessageDigest.getInstance("MD5");
		}catch (Exception e){
			logger.error("error:"+e.getMessage(),e);
			return "";
		}
		
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		
		for (int i = 0; i < md5Bytes.length; i++){
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		
		return hexValue.toString();
	}
	
	public static String decryptMode(String msg,String key) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(key.getBytes(), "DESede");
            // 解密
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            return new String(cipher.doFinal(Base64.decodeBase64(msg)));
        } catch (java.security.NoSuchAlgorithmException e1) {
        	logger.error("error:"+e1.getMessage(),e1);
        } catch (javax.crypto.NoSuchPaddingException e2) {
        	logger.error("error:"+e2.getMessage(),e2);
        } catch (java.lang.Exception e3) {
        	logger.error("error:"+e3.getMessage(),e3);
        }
        return null;
    }
	
	// 解密  
    /*public static byte[] decodeBase64(String s) {  
        byte[] b = null;  
        if (s != null) {  
            BASE64Decoder decoder = new BASE64Decoder();  
            try {  
                b = decoder.decodeBuffer(s);
                //result = new String(b, "UTF-8");  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return b;  
    }  */

}
