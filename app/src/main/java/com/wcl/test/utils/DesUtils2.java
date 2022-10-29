
package com.wcl.test.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DesUtils2 {
    private static final String TAG = "DesUtils";
    /**
     * 原文：1234567890 加密后：DAPuqXUOOYyueVvpS+NSfA==
     */

    private static final String ALGORITHM_DES = "DES/CBC/PKCS5Padding";
    public static final String KEY = "zshTtp^1";

    public static byte[] encryptDES(byte[] encryptData) {
        return encryptDES(encryptData, KEY.getBytes());
    }

    public static String encryptString(String input, String key) {
        if (TextUtils.isEmpty(input) || TextUtils.isEmpty(key)) {
            return "";
        }
        String ret = "";
        try {
            ret = new String(Base64.encode(encryptDES(input.getBytes(), key.getBytes()), Base64.DEFAULT));
        } catch (Throwable e) {
        }
        AppLogUtils.d(TAG, "[encryptString] ret:" + ret);
        return ret;
    }

    public static byte[] encryptDES(byte[] encryptData, byte[] key) {
        byte[] ret = null;
        IvParameterSpec zeroIv = null;
        SecretKeySpec sks = null;
        Cipher cipher = null;
        byte[] encryptedData = null;
        try {
            if (encryptData != null && encryptData.length > 0) {
                zeroIv = new IvParameterSpec(key);
                sks = new SecretKeySpec(key, "DES");
                cipher = Cipher.getInstance(ALGORITHM_DES);
                cipher.init(Cipher.ENCRYPT_MODE, sks, zeroIv);
                encryptedData = cipher.doFinal(encryptData);
                ret = encryptedData;
            }
        } catch(NoSuchMethodError e){
        } catch (Exception e) {

        }
        return ret;
    }

    public static String decryptString(String input, String key) {
        AppLogUtils.d(TAG, "[decryptString] input:" + input + ", key=" + key);
        if (TextUtils.isEmpty(input) || TextUtils.isEmpty(key)) {
            return "";
        }
        String ret = "";
        try {
            ret = new String(decryptDES(Base64.decode(input.getBytes(), Base64.DEFAULT), key.getBytes()));
        } catch (Throwable e) {
            AppLogUtils.e(TAG, ""+ e.toString());
        }
        AppLogUtils.d(TAG, "[decryptString] ret:" + ret);
        return ret;
    }

    public static byte[] decryptDES(byte[] encryptData) {
        return decryptDES(encryptData, KEY.getBytes());
    }

    public static byte[] decryptDES(byte[] encryptData, byte[] key) {
       
        try {
        	 byte[] ret = null;
             byte[] byteMi = null, decryptData = null;
             IvParameterSpec zeroIv = null;
             SecretKeySpec sks = null;
             Cipher cipher = null;
            if (encryptData != null && encryptData.length > 0) {
                byteMi = encryptData;
                zeroIv = new IvParameterSpec(key);
                sks = new SecretKeySpec(key, "DES");
                cipher = Cipher.getInstance(ALGORITHM_DES);
                cipher.init(Cipher.DECRYPT_MODE, sks, zeroIv);
                decryptData = cipher.doFinal(byteMi);
                ret = decryptData;
            }
            
            return ret;
        } catch (Exception e) {
            
        }
        return null;
    }

    public static String decryptDES(InputStream response, int contentLength) {
        return decryptDES(response, contentLength, KEY.getBytes());
    }

    public static String decryptDES(InputStream response, int contentLength, byte[] key) {
        String ret = null;
        IvParameterSpec zeroIv = null;
        SecretKeySpec sks = null;
        Cipher cipher = null;
        int size = contentLength >= 1024 ? contentLength : 1024;
        ByteArrayOutputStream result = new ByteArrayOutputStream(size);
        byte[] buffer = new byte[1024];
        int length = -1;
        try {
            zeroIv = new IvParameterSpec(key);
            sks = new SecretKeySpec(key, "DES");
            cipher = Cipher.getInstance(ALGORITHM_DES);
            CipherInputStream cis = new CipherInputStream(response, cipher);
            cipher.init(Cipher.DECRYPT_MODE, sks, zeroIv);
            while ((length = cis.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            result.flush();
            result.close();
            cis.close();
            ret = result.toString();
        } catch (Exception e) {
            
        }
        return ret;
    }

    private static final int BUFFER_LENGTH = 1024;

    /**
     * des 加密文件，文件的前8个字节为加密的长度，取1024个字节进行加des加密，如果文件小于1024个字节，则全部进行加密
     * 
     * @param desKey
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void encryptFile(String desKey, File sourceFile, File targetFile)
            throws IOException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new IOException();
        }

        FileInputStream in = new FileInputStream(sourceFile);
        DataInputStream dis = new DataInputStream(in);

        FileOutputStream out = new FileOutputStream(targetFile);
        DataOutputStream dos = new DataOutputStream(out);
        byte[] buffer = new byte[BUFFER_LENGTH];
        try {
            if (sourceFile.length() < BUFFER_LENGTH) {
                int n = dis.read(buffer, 0, buffer.length);
                byte[] temp = new byte[n];
                System.arraycopy(buffer, 0, temp, 0, n);
                byte[] desData = encryptDES(temp, desKey.getBytes());
                dos.writeLong(desData.length);// 8 byte
                dos.write(desData);
            } else {
                dis.read(buffer, 0, buffer.length);
                byte[] desData = encryptDES(buffer, desKey.getBytes());
                dos.writeLong(desData.length);// 8 byte
                dos.write(desData);
                int n = 0;
                while ((n = dis.read(buffer, 0, buffer.length)) != -1) {
                    dos.write(buffer, 0, n);
                }
            }
        } catch (IOException e) {
            throw new IOException();
        } finally {
            dis.close();
            in.close();
            dos.close();
            out.close();
        }
    }

    /**
     * des 解密文件，文件的前8个字节为加密的长度，取1024个字节进行加des加密，如果文件小于1024个字节，则全部进行加密
     * 
     * @param desKey
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void decryptFile(String desKey, File sourceFile, File targetFile)
            throws IOException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new IOException();
        }

        FileInputStream in = new FileInputStream(sourceFile);
        DataInputStream dis = new DataInputStream(in);

        FileOutputStream out = new FileOutputStream(targetFile);
        DataOutputStream dos = new DataOutputStream(out);
        byte[] buffer = new byte[BUFFER_LENGTH];
        try {
            long desLength = dis.readLong();
            if (sourceFile.length() < desLength + 8) {
                int n = dis.read(buffer, 0, buffer.length);
                byte[] temp = new byte[n];
                System.arraycopy(buffer, 0, temp, 0, n);
                byte[] desData = decryptDES(temp, desKey.getBytes());
                dos.write(desData);
            } else {
                byte[] desBuffer = new byte[(int) desLength];
                dis.read(desBuffer, 0, desBuffer.length);
                byte[] desData = decryptDES(desBuffer, desKey.getBytes());
                dos.write(desData);

                int n = 0;
                while ((n = dis.read(buffer, 0, buffer.length)) != -1) {
                    dos.write(buffer, 0, n);
                }

            }
        } catch (IOException e) {
            throw new IOException();
        } finally {
            dis.close();
            in.close();
            dos.close();
            out.close();
        }
    }
}
