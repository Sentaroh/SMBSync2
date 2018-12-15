package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;

import com.drew.lang.Charsets;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

public class KeyStoreUtil {
    final static String PROVIDER = "AndroidKeyStore";
    final static String ALGORITHM = "RSA";
    final static String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    final static public String makeSHA1Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        md.update(input);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    }

    final static public String makeSHA1Hash(String input) throws NoSuchAlgorithmException {
        return makeSHA1Hash(input.getBytes());
    }


    static final private String SAVED_KEY_ID="settings_key_store_util_save_key";
    public static String getGeneratedPassword(Context context, String alias) throws Exception {
//        Thread.dumpStack();
        KeyStore keyStore = null;
        byte[] bytes = null;
        String saved_key="";
        String generated_password="";
        keyStore = KeyStore.getInstance(PROVIDER);
        keyStore.load(null);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        saved_key=prefs.getString(SAVED_KEY_ID, "");
        if (!keyStore.containsAlias(alias) || saved_key.equals("")) {
            if (!keyStore.containsAlias(alias)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
                keyPairGenerator.initialize(createKeyPairGeneratorSpec(context, alias));
                keyPairGenerator.generateKeyPair();
            }

            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            generated_password=generateRandomPassword(32, true, true, true, true);
//            if (privateKey.toString().length()>128) generated_password=privateKey.toString().substring(1,128)+String.valueOf(System.currentTimeMillis());
//            else generated_password=privateKey.toString()+String.valueOf(System.currentTimeMillis());

//            String alg=privateKey.getAlgorithm();
//            String fmt=privateKey.getFormat();
//            byte[] enc=privateKey.getEncoded();
//
//            String pubKeyString = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
//            String privKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.NO_WRAP);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            bytes = cipher.doFinal(generated_password.getBytes(Charsets.UTF_8));

            saved_key=Base64.encodeToString(bytes, Base64.NO_WRAP);

            prefs.edit().putString(SAVED_KEY_ID, saved_key).commit();
        } else {
            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            bytes = Base64.decode(saved_key, Base64.NO_WRAP);

            byte[] b = cipher.doFinal(bytes);
            generated_password=new String(b);
        }

        return generated_password;
    }

    private static String generateRandomPassword(int max_length, boolean upperCase, boolean lowerCase, boolean numbers, boolean specialCharacters)
    {
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?/{}~|";
        String allowedChars = "";

        Random rn = new Random();
        StringBuilder sb = new StringBuilder(max_length);

        //this will fulfill the requirements of atleast one character of a type.
        if(upperCase) {
            allowedChars += upperCaseChars;
            sb.append(upperCaseChars.charAt(rn.nextInt(upperCaseChars.length()-1)));
        }

        if(lowerCase) {
            allowedChars += lowerCaseChars;
            sb.append(lowerCaseChars.charAt(rn.nextInt(lowerCaseChars.length()-1)));
        }

        if(numbers) {
            allowedChars += numberChars;
            sb.append(numberChars.charAt(rn.nextInt(numberChars.length()-1)));
        }

        if(specialCharacters) {
            allowedChars += specialChars;
            sb.append(specialChars.charAt(rn.nextInt(specialChars.length()-1)));
        }


        //fill the allowed length from different chars now.
        for(int i=sb.length();i < max_length;++i){
            sb.append(allowedChars.charAt(rn.nextInt(allowedChars.length())));
        }

        return  sb.toString();
    }
    public static KeyPairGeneratorSpec createKeyPairGeneratorSpec(Context context, String alias){
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 100);

        return new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal(String.format("CN=%s", alias)))
                .setSerialNumber(BigInteger.valueOf(1000000))
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
    }

}
