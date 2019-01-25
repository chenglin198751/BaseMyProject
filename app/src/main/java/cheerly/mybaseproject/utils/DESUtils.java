package cheerly.mybaseproject.utils;

import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DESUtils {

    /**
     * DES 加密
     *
     * @param encryptString 要被DES加密的数据
     * @param desKey        密钥：一定要是8个字节
     * @return 加密后的数据
     */
    public static String encrypt(String encryptString, String desKey) {
        if (TextUtils.isEmpty(encryptString)) {
            throw new NullPointerException("encryptString must not empty");
        }

        if (TextUtils.isEmpty(desKey)) {
            throw new NullPointerException("desKey must not empty");
        }

        if (desKey.length() != 8) {
            throw new IllegalArgumentException("desKey.length() must = 8");
        }

        byte[] encryptData = encryptString.getBytes();
        byte[] key = desKey.getBytes();

        try {
            IvParameterSpec zeroIv = new IvParameterSpec(key);
            SecretKeySpec sks = new SecretKeySpec(key, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sks, zeroIv);
            byte[] encryptedData = cipher.doFinal(encryptData);
            if (encryptedData == null || encryptedData.length <= 0) {
                return null;
            }
            return new String(Base64.encode(encryptedData, Base64.DEFAULT));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    /**
     * DES 解密
     *
     * @param decryptString 要被DES解密的数据
     * @param desKey        密钥：一定要是8个字节
     * @return 解密后的数据
     */
    public static String decrypt(String decryptString, String desKey) {
        if (TextUtils.isEmpty(decryptString)) {
            throw new NullPointerException("encryptString must not empty");
        }

        if (TextUtils.isEmpty(desKey)) {
            throw new NullPointerException("desKey must not empty");
        }

        if (desKey.length() != 8) {
            throw new IllegalArgumentException("desKey.length() must = 8");
        }

        try {
            byte[] base64byte = Base64.decode(decryptString, Base64.DEFAULT);
            if (base64byte == null || base64byte.length <= 0) {
                return null;
            }

            IvParameterSpec spec = new IvParameterSpec(desKey.getBytes());
            SecretKeySpec key = new SecretKeySpec(desKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] decryptedData = cipher.doFinal(base64byte);
            if (decryptedData == null || decryptedData.length <= 0) {
                return null;
            }

            return new String(decryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
