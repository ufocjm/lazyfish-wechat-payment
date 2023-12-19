package fun.nibaba.lazyfish.wechat.payment.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import fun.nibaba.lazyfish.wechat.payment.constant.WechatConstant;
import fun.nibaba.lazyfish.wechat.payment.properties.WechatPaymentProperties;
import fun.nibaba.lazyfish.wechat.payment.utils.WechatAesUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 微信支付-签名
 *
 * @author chenjiamin
 * @date 2021/5/14 6:14 下午
 */
@Slf4j
public class WechatPaymentSign {

    /**
     * 加密参数实体
     */
    private final WechatPaymentParams params;

    /**
     * 微信商户配置
     */
    private final WechatPaymentProperties properties;

    @Getter
    private String sign;

    @Getter
    private Map<String, Object> sortMap;

    /**
     * appid key
     */
    private static final String APP_ID_KEY = "appid";

    /**
     * mch_id key
     */
    private static final String MCH_ID_KEY = "mch_id";

    public WechatPaymentSign(WechatPaymentParams params, WechatPaymentProperties properties) {
        if (params == null) {
            throw new NullPointerException("加密参数实体不能为空");
        }
        if (properties == null) {
            throw new NullPointerException("微信商户配置不能为空");
        }
        this.params = params;
        this.properties = properties;
    }

    /**
     * 执行签名
     *
     * @return
     */
    public WechatPaymentSign sign() {
        return this.sign(true);
    }

    /**
     * 执行签名
     *
     * @param toUnderlineCase 是否需要将 params 转为下划线的Map
     * @return
     */
    public WechatPaymentSign sign(boolean toUnderlineCase) {
        this.sortMap = BeanUtil.beanToMap(params, new TreeMap<>(), toUnderlineCase, true);

        this.sortMap.put(APP_ID_KEY, properties.getAppid());
        this.sortMap.put(MCH_ID_KEY, properties.getMchId());

        this.sign = WechatAesUtil.sign(this.sortMap, properties.getMchSecret());
        this.sortMap.put(WechatConstant.SIGN_KEY, this.sign);
        return this;
    }


}
