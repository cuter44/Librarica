package com.github.cuter44.util.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;

import java.security.MessageDigest;

import java.security.SecureRandom;

import java.security.NoSuchAlgorithmException;

import java.nio.ByteBuffer;


/** 提供加密算法的封装
 * <br />
 * 主要提供了AES, RSA的加密, 解密, 密钥生成等方法.
 * <br />
 * 只要密钥和密文都正确就能给出明文, 否则给出空值, 这个库截断了可能产生的 Exception
 * 以简化上层代码逻辑, 上层代码在进行解密时应始终检查返回值是否为空.
 */
public class CryptoUtil
{
    public static final char[] base64char =
        {'A','B','C','D','E','F','G','H',
         'I','J','K','L','M','N','O','P',
         'Q','R','S','T','U','V','W','X',
         'Y','Z','a','b','c','d','e','f',
         'g','h','i','j','k','l','m','n',
         'o','p','q','r','s','t','u','v',
         'w','x','y','z','0','1','2','3',
         '4','5','6','7','8','9','+','/'};

  // SINGLETON
    private KeyGenerator AESKeyGen = null;
    private KeyPairGenerator RSAKeyGen = null;
    private SecureRandom rng = null;

    private CryptoUtil()
    {
        try
        {
            this.AESKeyGen = KeyGenerator.getInstance("AES");
            this.RSAKeyGen = KeyPairGenerator.getInstance("RSA");
            this.RSAKeyGen.initialize(1024);
            this.rng = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException ex)
        {
            // never occur
            ex.printStackTrace();
        }
    }

    private static class Singleton
    {
        private static CryptoUtil instance = new CryptoUtil();
    }

  // KEY
    public static SecretKey generateAESKey()
    {
        SecretKey key = Singleton.instance.AESKeyGen.generateKey();

        return(key);
    }

    public static KeyPair generateRSAKey()
    {
        KeyPair kp = Singleton.instance.RSAKeyGen.generateKeyPair();

        return(kp);
    }

  // CRYPTO
    public static byte[] AESEncrypt(byte[] in, SecretKey key)
    {
        try
        {
            if (in == null)
                throw(new IllegalArgumentException("in must have walue."));
            if (key == null)
                throw(new IllegalArgumentException("key must have value."));

            // TODO: specified mode and padding.
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, key);

            byte[] out = c.doFinal(in);

            return(out);
        }
        catch (IllegalArgumentException ex)
        {
            throw(ex);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static byte[] AESDecrypt(byte[] in, SecretKey key)
    {
        try
        {
            if (in == null)
                throw(new IllegalArgumentException("in must have walue."));
            if (key == null)
                throw(new IllegalArgumentException("key must have value."));

            // TODO: specified mode and padding.
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);

            byte[] out = c.doFinal(in);

            return(out);
        }
        catch (IllegalArgumentException ex)
        {
            throw(ex);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static byte[] RSAEncrypt(byte[] in, PublicKey key)
    {
        try
        {
            if (in == null)
                throw(new IllegalArgumentException("in must have walue."));
            if (key == null)
                throw(new IllegalArgumentException("key must have value."));

            // TODO: specified mode and padding.
            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.ENCRYPT_MODE, key);

            byte[] out = c.doFinal(in);

            return(out);
        }
        catch (IllegalArgumentException ex)
        {
            throw(ex);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    public static byte[] RSADecrypt(byte[] in, PrivateKey key)
    {
        try
        {
            if (in == null)
                throw(new IllegalArgumentException("in must have walue."));
            if (key == null)
                throw(new IllegalArgumentException("key must have value."));

            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.DECRYPT_MODE, key);

            byte[] out = c.doFinal(in);

            return(out);
        }
        catch (IllegalArgumentException ex)
        {
            throw(ex);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

  // DIGEST
    public static byte[] MD5Digest(byte[] in)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] out = md.digest(in);

            return(out);
        }
        catch (NoSuchAlgorithmException ex)
        {
            // never occur
            ex.printStackTrace();
            return(null);
        }
    }

  // BASE64
    public static String base64Encode(byte[] bytes)
    {
        final int[] patterns =
            {0x0000, 0x0001, 0x0003, 0x0007, 0x000f, 0x001f, 0x003f, 0x007f,
            0x00ff, 0x01ff, 0x03ff,0x07ff, 0x0fff, 0x1fff, 0x3fff, 0x7fff};

        StringBuilder sb = new StringBuilder();
        int inbuf = 0;
        int inpos = 0;
        int inbufsize = 0;

        while (inpos < bytes.length)
        {
            inbuf = (inbuf << 8) | (bytes[inpos] & 0xff);
            inpos += 1;
            inbufsize += 8;

            if ((inpos%57) == 0 )
                sb.append("\n");

            while (inbufsize >= 6)
            {
                int outbuf = inbuf >> (inbufsize - 6);
                inbuf = inbuf & patterns[inbufsize - 6];
                inbufsize -= 6;

                sb.append(base64char[outbuf]);
            }
        }

        if (inpos%3 != 0)
        {
            inbuf = (inbuf << 8);
            inbufsize += 8;
            int outbuf = inbuf >> (inbufsize - 6);
            sb.append(base64char[outbuf]);

            while (inpos%3 != 0)
            {
                sb.append("=");
                inpos++;
            }
        }

        return(sb.toString());
    }

  // RANDOM
    public static byte[] randomBytes(int length)
    {
        byte[] bytes = new byte[length];

        Singleton.instance.rng.nextBytes(bytes);
        return(bytes);
    }

  // CONVERT
    /** (自己实现的)将HexString转换成byte[]
     * <br />
     * 要保证输入是偶数个否则会丢失最末端的半个字节.
     */
    public static byte[] hexToBytes(String s)
    {
        int l = s.length() / 2;

        ByteBuffer buf = ByteBuffer.allocate(l);
        for (int i=0; i<s.length(); i+=2)
        {
            buf.put(
                Integer.valueOf(
                    s.substring(i, i+2),
                    16
                ).byteValue()
            );
        }

        return(buf.array());
    }

    public static String byteToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length*2);

        for (int i=0; i<bytes.length; i++)
            sb.append(
                String.format("%02x", bytes[i] & 0xff)
            );

        return(sb.toString());
    }
}
