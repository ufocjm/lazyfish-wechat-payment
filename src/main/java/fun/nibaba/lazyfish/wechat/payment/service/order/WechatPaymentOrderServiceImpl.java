package fun.nibaba.lazyfish.wechat.payment.service.order;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSONObject;
import fun.nibaba.lazyfish.wechat.payment.constant.WechatConstant;
import fun.nibaba.lazyfish.wechat.payment.interceptors.order.WechatPaymentCreateOrderInterceptor;
import fun.nibaba.lazyfish.wechat.payment.interceptors.order.WechatPaymentPayCallbackInterceptor;
import fun.nibaba.lazyfish.wechat.payment.interceptors.order.WechatPaymentQueryOrderInterceptor;
import fun.nibaba.lazyfish.wechat.payment.model.WechatPaymentSign;
import fun.nibaba.lazyfish.wechat.payment.model.order.*;
import fun.nibaba.lazyfish.wechat.payment.properties.WechatPaymentProperties;
import fun.nibaba.lazyfish.wechat.payment.utils.WechatAesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 微信支付-订单服务-实现类
 *
 * @author chenjiamin
 * @date 2021/5/14 10:34 下午
 */
@Slf4j
@AllArgsConstructor
public class WechatPaymentOrderServiceImpl implements WechatPaymentOrderService {

    private final WechatPaymentCreateOrderInterceptor createOrderInterceptor;

    private final WechatPaymentQueryOrderInterceptor queryOrderInterceptor;

    private final WechatPaymentPayCallbackInterceptor payCallbackInterceptor;

    @Override
    public WechatPaymentCreateOrderResult createOrder(WechatPaymentCreateOrderParams params, WechatPaymentProperties properties) {
        createOrderInterceptor.processBefore(params);

        log.debug("开始-请求微信支付创建订单 参数:[{}],配置:[{}]", JSONObject.toJSONString(params), JSONObject.toJSONString(properties));
        WechatPaymentCreateOrderRequest orderRequest = new WechatPaymentCreateOrderRequest(new WechatPaymentSign(params, properties));
        WechatPaymentCreateOrderResponse response = orderRequest.request();
        log.debug("结束-请求微信支付创建订单");

        WechatPaymentCreateOrderResult result = response.toResult();
        createOrderInterceptor.processAfter(params, result);

        return result;
    }

    @Override
    public WechatPaymentQueryOrderResult queryOrder(@Valid WechatPaymentQueryOrderParams params, @Valid WechatPaymentProperties properties) {
        queryOrderInterceptor.processBefore(params);

        log.debug("开始-请求微信支付查询订单 参数:[{}],配置:[{}]", JSONObject.toJSONString(params), JSONObject.toJSONString(properties));
        WechatPaymentQueryOrderRequest queryOrderRequest = new WechatPaymentQueryOrderRequest(new WechatPaymentSign(params, properties));
        WechatPaymentQueryOrderResponse response = queryOrderRequest.request();
        log.debug("结束-请求微信支付查询订单");

        WechatPaymentQueryOrderResult result = response.toResult();

        queryOrderInterceptor.processAfter(params, result);
        return result;
    }

    @Override
    public WechatPaymentPayCallBackResult callBack(String xmlBody) {
        log.debug("开始-微信支付回调解析开始 xmlBody:[{}], 密钥:[{}]", xmlBody);
        payCallbackInterceptor.processBefore(xmlBody);
        WechatPaymentPayCallBackResult result = privateCallBack(xmlBody);
        payCallbackInterceptor.processAfter(xmlBody, result);
        log.debug("开始-微信支付回调解析结束");
        return result;
    }

    @Override
    public WechatPaymentPayCallBackResult callBack(@NotBlank(message = "回调消息体不能为空") String xmlBody, @NotBlank(message = "商户密钥不能为空") String mchSecret) {
        payCallbackInterceptor.processBefore(xmlBody);
        log.debug("开始-微信支付回调解析开始 xmlBody:[{}], 密钥:[{}]", xmlBody, mchSecret);
        Map<String, Object> xmlMap = XmlUtil.xmlToMap(xmlBody);
        String sign = WechatAesUtil.sign(xmlMap, mchSecret);
        String requestSign = (String) xmlMap.get(WechatConstant.SIGN_KEY);
        if (!requestSign.equals(sign)) {
            throw new RuntimeException("签名验证失败");
        }
        WechatPaymentPayCallBackResult result = privateCallBack(xmlBody);

        payCallbackInterceptor.processAfter(xmlBody, result);
        log.debug("开始-微信支付回调解析结束");
        return result;
    }

    private WechatPaymentPayCallBackResult privateCallBack(String xmlBody) {
        WechatPaymentPayCallBackBody wechatPaymentPayCallBackBody = new WechatPaymentPayCallBackBody(xmlBody);
        WechatPaymentPayCallBackResponse response = wechatPaymentPayCallBackBody.build();
        return response.toResult();
    }

}
