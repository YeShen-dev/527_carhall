package com.autoparts.market.payment.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 微信支付V3 签名/AES解密工具
 *
 * 微信V3接口使用 RSA-SHA256 签名，回调数据使用 AES-256-GCM 解密
 */
@Slf4j
public class WechatPaySignature {

    /**
     * RSA-SHA256 签名
     *
     * @param message    待签名字符串（method\nurl\ntimestamp\nnonce\nbody\n）
     * @param privateKey PKCS8私钥内容(PEM去除头尾和换行)
     * @return Base64编码的签名
     */
    public static String sign(String message, String privateKey) throws Exception {
        byte[] keyBytes = Base64.getMimeDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(spec);

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(pk);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    /**
     * RSA-SHA256 验签
     *
     * @param message   待验签字符串
     * @param signature Base64签名值
     * @param publicKey 微信平台公钥(PEM去除头尾和换行)
     */
    public static boolean verify(String message, String signature, String publicKey) throws Exception {
        byte[] keyBytes = Base64.getMimeDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pk = kf.generatePublic(spec);

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(pk);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        return sign.verify(Base64.getDecoder().decode(signature));
    }

    /**
     * AES-256-GCM 解密回调报文
     *
     * @param associatedData 附加数据（通常为空或固定值）
     * @param nonce          随机串（Base64）
     * @param ciphertext     密文（Base64）
     * @param apiV3Key       APIv3密钥（32字节）
     * @return 解密后的明文JSON
     */
    public static String decrypt(byte[] associatedData, byte[] nonce, String ciphertext, String apiV3Key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        // 微信GCM模式需要传入 associated_data
        if (associatedData != null && associatedData.length > 0) {
            cipher.updateAAD(associatedData);
        }

        byte[] plaintext = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
