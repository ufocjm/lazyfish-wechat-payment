package fun.nibaba.lazyfish.wechat.payment.constant;

/**
 * 微信常量
 */
public interface WechatConstant {

    /**
     * 商户密钥的key
     */
    String MCH_SECRET_KEY = "key";

    /**
     * 签名字段的key
     */
    String SIGN_KEY = "sign";

    /**
     * 回调成功标识
     */
    String CALLBACK_SUCCESS = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";


}
