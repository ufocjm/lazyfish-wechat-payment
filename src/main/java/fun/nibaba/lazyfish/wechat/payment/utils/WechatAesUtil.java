package fun.nibaba.lazyfish.wechat.payment.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import fun.nibaba.lazyfish.wechat.payment.constant.WechatConstant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * 微信AES工具
 *
 * @author chenjiamin
 */
@Slf4j
@UtilityClass
public class WechatAesUtil {

    /**
     * 密钥算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加解密算法/工作模式/填充方式
     */
    private static final String ALGORITHM_MODE_AES_CBC_PADDING = "AES/CBC/PKCS7Padding";

    private static final String ALGORITHM_MODE_AES_ECB_PADDING = "AES/ECB/PKCS7Padding";

    public static boolean initialized = false;

    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AES解密
     *
     * @param content 密文
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     */
    public byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws InvalidAlgorithmParameterException {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_AES_CBC_PADDING);
            Key sKeySpec = new SecretKeySpec(keyByte, ALGORITHM);
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (WechatAesUtil.class) {
            Security.addProvider(new BouncyCastleProvider());
            initialized = true;
        }
    }

    /**
     * 生成iv
     *
     * @param iv
     * @return
     * @throws Exception
     */
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }

    /**
     * AES解密
     *
     * @param encryptionData 密文
     * @param encryptionKey  密钥
     * @return
     */
    public String decrypt(String encryptionData, String encryptionKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_AES_ECB_PADDING, "BC");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SecureUtil.md5(encryptionKey).getBytes(), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.decode(encryptionData)));

        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException |
                 NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        log.error("解密失败:encryptionData[{}],encryptionKey[{}]", encryptionData, encryptionKey);
        throw new RuntimeException("解密失败[" + encryptionData + "]");
    }

    /**
     * 签名
     *
     * @param params    参数
     * @param mchSecret 密钥
     * @return sign
     */
    public String sign(Map<String, Object> params, String mchSecret) {
        if (params == null) {
            throw new RuntimeException("加密参数不能为空");
        }
        if (StrUtil.isBlank(mchSecret)) {
            throw new RuntimeException("商户密钥不能为空");
        }
        Map<String, Object> sortMap;
        if (params instanceof TreeMap) {
            sortMap = params;
        } else {
            sortMap = new TreeMap<>(params);
        }

        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, Object>> entries = sortMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String paramKey = entry.getKey();
            String paramValue = entry.getValue().toString();
            if (StrUtil.isNotBlank(paramValue) && !WechatConstant.SIGN_KEY.equals(paramKey) && !WechatConstant.MCH_SECRET_KEY.equals(paramKey)) {
                sb.append(paramKey).append("=").append(paramValue).append("&");
            }
        }

        sb.append(WechatConstant.MCH_SECRET_KEY).append("=").append(mchSecret);
        log.debug("签名参数:[{}]", sb.toString());

        String sign = SecureUtil.md5(sb.toString()).toUpperCase();
        log.debug("签名字符串:[{}]", sign);
        return sign;

    }

}
