package cn.edu.scau.librarica.util.test;

import java.util.Scanner;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.conf.*;
import com.alibaba.fastjson.*;
import org.apache.http.client.fluent.*;

/** rerieve key and encode
 */
public class EncryptPasswordCLI
{
    public static PublicKey getRsaKey(Long uid)
    {
        String resp = "";

        try
        {
            String baseurl = Configurator.get("librarica.server.api.baseurl");
            resp = Request.Post(baseurl+"/security/get-rsa-key")
                .bodyForm(
                    Form.form()
                    .add("uid", uid.toString())
                    .build()
                )
                .execute()
                .returnContent()
                .asString();

            JSONObject json = (JSONObject)JSONObject.parse(resp);
            BigInteger m = new BigInteger(json.getString("m"), 16);
            BigInteger e = new BigInteger(json.getString("e"), 16);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey key = kf.generatePublic(new RSAPublicKeySpec(m,e));

            return(key);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(resp);
            return(null);
        }
    }

    /**
     * encrypt with a specified key, not one retrieve from endpoint.
     */
    public static byte[] encrypt(String pass, PublicKey key)
    {
        try
        {
            byte[] encrypted = CryptoUtil.RSAEncrypt(pass.getBytes("utf-8"), key);

            return(encrypted);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return(null);
        }
    }

    /**
     * retrive key from http endpoint and encrypt.
     * @param uid
     * @param pass in plaintext
     * @return encrypted password
     */
    public static byte[] encrypt(Long uid, String pass)
    {
        return(
            encrypt(
                pass,
                getRsaKey(uid)
            )
        );
    }

    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);

        System.out.println("uid?");
        Long uid = scn.nextLong();
        scn.nextLine();

        System.out.println("pass?");
        String pass = scn.nextLine();

        System.out.println(
            CryptoUtil.byteToHex(
                encrypt(uid, pass)
            )
        );
    }
}
