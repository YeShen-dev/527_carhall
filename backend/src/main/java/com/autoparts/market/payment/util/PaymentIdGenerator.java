package com.autoparts.market.payment.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 支付流水号生成器
 * 格式：PAY/REF + yyyyMMddHHmmss + 6位随机数字
 */
public class PaymentIdGenerator {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 生成支付流水号 */
    public static String generatePaymentNo() {
        return "PAY" + now() + random6();
    }

    /** 生成退款流水号 */
    public static String generateRefundNo() {
        return "REF" + now() + random6();
    }

    private static String now() {
        return LocalDateTime.now().format(DT_FMT);
    }

    private static String random6() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
    }
}
