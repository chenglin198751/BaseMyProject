
package com.wcl.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public class MD5Util {

    private static final String TAG = "MD5Util";

    public static final String ALGORITHM = "MD5";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static String md5LowerCase(String string) {
        if (!TextUtils.isEmpty(string)) {
            try {
                byte[] buffer = string.getBytes(DEFAULT_CHARSET);
                if (buffer != null && buffer.length > 0) {
                    MessageDigest digester = MessageDigest.getInstance(ALGORITHM);
                    digester.update(buffer);
                    buffer = digester.digest();
                    string = toLowerCaseHex(buffer);
                }
            } catch (NoSuchAlgorithmException e) {
            } catch (UnsupportedEncodingException e) {
            } catch (Exception e) {
            }
        }

        return string;
    }
    
    public static String md5(String string) {
        if (!TextUtils.isEmpty(string)) {
            try {
                byte[] buffer = string.getBytes(DEFAULT_CHARSET);
                if (buffer != null && buffer.length > 0) {
                    MessageDigest digester = MessageDigest.getInstance(ALGORITHM);
                    digester.update(buffer);
                    buffer = digester.digest();
                    string = new String(buffer);
                }
            } catch (NoSuchAlgorithmException e) {
            } catch (UnsupportedEncodingException e) {
            } catch (Exception e) {
            }
        }

        return string;
    }
    
    

    public static String md5LowerCase(byte[] buffer) {
        String ret = "";
        try {
            if (buffer != null && buffer.length > 0) {
                MessageDigest digester = MessageDigest.getInstance(ALGORITHM);
                digester.update(buffer);
                buffer = digester.digest();
                ret = toLowerCaseHex(buffer);
            }
        } catch (Throwable tr) {
        }
        return ret;
    }

    private static String toLowerCaseHex(byte[] b) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int v = b[i];
            builder.append(HEX_LOWER_CASE[(0xF0 & v) >> 4]);
            builder.append(HEX_LOWER_CASE[0x0F & v]);
        }
        return builder.toString();
    }

    private static final char[] HEX_LOWER_CASE = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    
    /**
     * 获取单个文件的MD5值！

     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
     if (!file.isFile()) {
      return null;
     }
     MessageDigest digest = null;
     FileInputStream in = null;
     String ret = "";
     byte buffer[] = new byte[1024];
     int len;
     try {
      digest = MessageDigest.getInstance("MD5");
      in = new FileInputStream(file);
      while ((len = in.read(buffer, 0, 1024)) != -1) {
       digest.update(buffer, 0, len);
      }
      in.close();
     } catch (Exception e) {
      e.printStackTrace();
      return null;
     }
//     BigInteger bigInt = new BigInteger(1, digest.digest());
//     return bigInt.toString(16);
     ret = toLowerCaseHex(digest.digest());
     return ret;
    }

}
