package fun.nibaba.lazyfish.wechat.payment.model.order;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import fun.nibaba.lazyfish.wechat.payment.model.WechatPaymentResult;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 微信支付-创建订单-返回结果
 *
 * @author chenjiamin
 * @date 2021/5/14 11:49 下午
 */
@Getter
public class WechatPaymentCreateOrderResult extends WechatPaymentResult {

    /**
     * 业务结果
     * SUCCESS/FAIL
     */
    private final String resultCode;

    /**
     * 错误代码
     * 当result_code为FAIL时返回错误代码，详细参见下文错误列表
     */
    private final String errCode;

    /**
     * 错误代码描述
     * 当result_code为FAIL时返回错误描述，详细参见下文错误列表
     */
    private final String errCodeDes;

    /**
     * 交易类型 {@link fun.nibaba.lazyfish.wechat.payment.enums.TradeType}
     * JSAPI -JSAPI支付
     * NATIVE -Native支付
     * APP -APP支付
     */
    private final String tradeType;

    /**
     * 预支付交易会话标识
     * 微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时
     */
    private final String prepayId;

    /**
     * 二维码链接
     * trade_type=NATIVE时有返回，此url用于生成支付二维码，然后提供给用户进行扫码支付。
     */
    private final String codeUrl;

    @Builder
    public WechatPaymentCreateOrderResult(String returnCode,
                                          String returnMsg,
                                          String appid,
                                          String mchId,
                                          String nonceStr,
                                          String sign,
                                          String resultCode,
                                          String errCode,
                                          String errCodeDes,
                                          String tradeType,
                                          String prepayId,
                                          String codeUrl) {
        super(returnCode, returnMsg, appid, mchId, nonceStr, sign);
        this.resultCode = resultCode;
        this.errCode = errCode;
        this.errCodeDes = errCodeDes;
        this.tradeType = tradeType;
        this.prepayId = prepayId;
        this.codeUrl = codeUrl;
    }

    /**
     * 签名
     *
     * @return 签名
     */
    public WechatPaymentCreateOrderResultSign sign(String key) {
        WechatPaymentCreateOrderResultSign wechatPaymentCreateOrderResultSign = new WechatPaymentCreateOrderResultSign(this.appid, this.prepayId, key);
        wechatPaymentCreateOrderResultSign.sign();
        return wechatPaymentCreateOrderResultSign;
    }

    /**
     * 返回结果 签名后 实体
     */
    @Getter
    @Slf4j
//    @Data
    public static class WechatPaymentCreateOrderResultSign {

        private final String appId;

        private final String nonceStr;

        @JSONField(name = "package")
        private final String packageStr;

        private final Long timeStamp;

        private final String signType = "MD5";

        @JSONField(serialize = false, deserialize = false)
        private final String key;

        @JSONField(serialize = false, deserialize = false)
        private String sign;

        public WechatPaymentCreateOrderResultSign(String appId, String prepayId, String key) {
            if (StrUtil.isBlank(prepayId)) {
                throw new RuntimeException("prepayId 不能为空");
            }
            this.appId = appId;
            this.nonceStr = IdUtil.simpleUUID();
            this.packageStr = "prepay_id=" + prepayId;
            this.timeStamp = System.currentTimeMillis();
            this.key = key;
        }

        private void sign() {
            Map<String, Object> sortMap = new TreeMap<>(JSONObject.parseObject(JSONObject.toJSONString(this)));

            StringBuilder sb = new StringBuilder();
            Set<Map.Entry<String, Object>> entries = sortMap.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String paramKey = entry.getKey();
                String paramValue = entry.getValue().toString();
                if (StrUtil.isNotBlank(paramValue)) {
                    sb.append(paramKey).append("=").append(paramValue).append("&");
                }
            }
            sb.append("key").append("=").append(this.key);
            this.sign = SecureUtil.md5(sb.toString()).toUpperCase();
            log.debug("签名字符串:[{}]", this.sign);
        }

    }

}
