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
    /**
     * @param uid
     * @param pass in plaintext
     * @return encrypted password
     */
    public static byte[] encrypt(Long uid, String pass)
    {
        String resp = "";
        try
        {
            String baseurl = Configurator.get("librarica.server.web.baseurl");
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

            byte[] encrypted = CryptoUtil.RSAEncrypt(pass.getBytes("utf-8"), key);

            return(encrypted);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(resp);
            return(null);
        }

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
