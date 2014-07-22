package cn.edu.scau.librarica.util.test;

import java.util.Scanner;
import java.security.PublicKey;

import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.conf.*;
import com.alibaba.fastjson.*;
import org.apache.http.client.fluent.*;

public class PasswdCLI
{
    public static void doPasswd(Long uid, String pass, String newpass)
        throws Exception
    {
        PublicKey key = EncryptPasswordCLI.getRsaKey(uid);

        String enpass = CryptoUtil.byteToHex(
            EncryptPasswordCLI.encrypt(pass, key)
        );
        String ennewpass = CryptoUtil.byteToHex(
            EncryptPasswordCLI.encrypt(newpass, key)
        );

        String baseurl = Configurator.get("librarica.server.api.baseurl");
        String resp = Request.Post(baseurl+"/user/passwd")
            .bodyForm(
                Form.form()
                .add("uid", uid.toString())
                .add("pass", enpass)
                .add("newpass", ennewpass)
                .build()
            )
            .execute()
            .returnContent()
            .asString();

        return;
    }

    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);

        System.out.println("uid?");
        Long uid = scn.nextLong();
        scn.nextLine();

        System.out.println("pass?");
        String pass = scn.nextLine();

        System.out.println("newpass?");
        String newpass = scn.nextLine();

        try
        {
            doPasswd(uid, pass, newpass);
            System.out.println("OK(200)");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
