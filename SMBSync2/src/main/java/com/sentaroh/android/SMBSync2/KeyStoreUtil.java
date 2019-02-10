package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.drew.lang.Charsets;
import com.sentaroh.android.Utilities.SafFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.security.auth.x500.X500Principal;

public class KeyStoreUtil {
    final static String PROVIDER = "AndroidKeyStore";
    final static String ALGORITHM = "RSA";
    final static String CIPHER_TRANSFORMATION_BELOW_API27 = "RSA/ECB/PKCS1Padding";
    final static String CIPHER_TRANSFORMATION_ABOVE_API28 = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
//    final static boolean LOG_MESSAGE_ENABLED=false;

    private static Logger slf4jLog = LoggerFactory.getLogger(KeyStoreUtil.class);

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
    static final private String KEY_PARE_CREATE_VERSION_KEY="settings_key_store_util_key_pare_version";
    static final private String KEY_PARE_CREATE_VERSION_API28="28";

    public static String getGeneratedPasswordOldVersion(Context context, String alias) throws Exception {
//        Thread.dumpStack();
        slf4jLog.info("getGeneratedPasswordOldVersion entered");
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
                keyPairGenerator.initialize(createKeyPairGeneratorSpecBelowApi27(context, alias));
                keyPairGenerator.generateKeyPair();
            }

            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
//            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            generated_password=generateRandomPassword(32, true, true, true, true);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_BELOW_API27);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            bytes = cipher.doFinal(generated_password.getBytes(Charsets.UTF_8));

            saved_key=Base64.encodeToString(bytes, Base64.NO_WRAP);

            prefs.edit().putString(SAVED_KEY_ID, saved_key).commit();
        } else {
//            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_BELOW_API27);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            bytes = Base64.decode(saved_key, Base64.NO_WRAP);

            byte[] b = cipher.doFinal(bytes);
            generated_password=new String(b);
        }
        slf4jLog.info("getGeneratedPasswordOldVersion ended");
        return generated_password;
    }

    public static String getGeneratedPasswordNewVersion(Context context, String alias) throws Exception {
        slf4jLog.info("getGeneratedPasswordNewVersion entered");
//        Thread.dumpStack();
        KeyStore keyStore = null;
        byte[] bytes = null;
        String saved_key="";
        String generated_password="";
        keyStore = KeyStore.getInstance(PROVIDER);
        keyStore.load(null);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        saved_key=prefs.getString(SAVED_KEY_ID, "");
        OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
        if (!keyStore.containsAlias(alias) || saved_key.equals("")) {
            if (!keyStore.containsAlias(alias)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
                if (Build.VERSION.SDK_INT>=28) {
                    prefs.edit().putString(KEY_PARE_CREATE_VERSION_KEY,KEY_PARE_CREATE_VERSION_API28).commit();
                    keyPairGenerator.initialize(createKeyGenParameterSpecAboveApi28(context, alias));
                } else {
                    keyPairGenerator.initialize(createKeyPairGeneratorSpecBelowApi27(context, alias));
                }
                keyPairGenerator.generateKeyPair();
            }

            String key_pare_version=prefs.getString(KEY_PARE_CREATE_VERSION_KEY,"0");
            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();

            generated_password=generateRandomPassword(32, true, true, true, true);

            Cipher cipher = null;
            if (Build.VERSION.SDK_INT>=28 && key_pare_version.equals(KEY_PARE_CREATE_VERSION_API28)) {
                cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_ABOVE_API28);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey, spec);
            } else {
                cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_BELOW_API27);
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            }

            bytes = cipher.doFinal(generated_password.getBytes(Charsets.UTF_8));

            saved_key=Base64.encodeToString(bytes, Base64.NO_WRAP);

            prefs.edit().putString(SAVED_KEY_ID, saved_key).commit();
        } else {
            String key_pare_version=prefs.getString(KEY_PARE_CREATE_VERSION_KEY,"0");
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

            Cipher cipher = null;
            if (Build.VERSION.SDK_INT>=28 && key_pare_version.equals(KEY_PARE_CREATE_VERSION_API28)) {
                cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_ABOVE_API28);
                cipher.init(Cipher.DECRYPT_MODE, privateKey, spec);
            } else {
                cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_BELOW_API27);
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
            }

            bytes = Base64.decode(saved_key, Base64.NO_WRAP);

            byte[] b = cipher.doFinal(bytes);
            generated_password=new String(b);
        }
        slf4jLog.info("getGeneratedPasswordNewVersion ended");
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
    private static KeyPairGeneratorSpec createKeyPairGeneratorSpecBelowApi27(Context context, String alias){
//        Thread.dumpStack();
        slf4jLog.info("createKeyPairGeneratorSpecBelowApi27 entered");
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 100);

        KeyPairGeneratorSpec kgs=new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(new X500Principal(String.format("CN=%s", alias)))
                .setSerialNumber(BigInteger.valueOf(1000000))
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        slf4jLog.info("createKeyPairGeneratorSpecBelowApi27 ended");
        return kgs;
    }

    private static KeyGenParameterSpec createKeyGenParameterSpecAboveApi28(Context context, String alias){
        slf4jLog.info("createKeyGenParameterSpecAboveApi28 entered");
//        Thread.dumpStack();
//        Calendar start = Calendar.getInstance();
//        Calendar end = Calendar.getInstance();
//        end.add(Calendar.YEAR, 100);

        KeyGenParameterSpec kgs=new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT|KeyProperties.PURPOSE_ENCRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build();
        slf4jLog.info("createKeyGenParameterSpecAboveApi28 ended");
        return kgs;
    }

}
