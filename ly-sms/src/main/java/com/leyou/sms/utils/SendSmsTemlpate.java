package com.leyou.sms.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 发送短信业务
 */
public class SendSmsTemlpate {

    // 用户账号
    private static String userid = null;
    // 用户密码
    private static String pwd = null;
    // 主IP信息 必填
    private static String masterIpAddress = "api01.monyun.cn:7901";
    // 备IP1 选填
    private static String ipAddress1 = null;
    // 备IP2 选填
    private static String ipAddress2 = null;
    // 备IP3 选填
    private static String ipAddress3 = null;

    private static boolean isEncryptPwd = false;

    // 日期格式定义，自定义的时间不要改，会报错
    private static SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");

    static {
        // 设置IP
        ConfigManager.setIpInfo(masterIpAddress, ipAddress1, ipAddress2, ipAddress3);

        // 密码是否加密 true：密码加密;false：密码不加密
        ConfigManager.IS_ENCRYPT_PWD = true;
        isEncryptPwd = ConfigManager.IS_ENCRYPT_PWD;
    }

    /**
     * 单条发送短信
     *
     * @param phone
     * @param content
     */
    public static int singleSend(String phone, String content, String userid, String pwd) {
        try {
            // 参数类
            Message message = new Message();
            // 实例化短信处理对象
            CHttpPost cHttpPost = new CHttpPost();
            // 设置账号 将 userid转成大写,以防大小写不一致
            message.setUserid(userid.toUpperCase());
            // 密码是否加密 true：密码加密;false：密码不加密
            ConfigManager.IS_ENCRYPT_PWD = true;
            // 默认进行加密
            // isEncryptPwd = ConfigManager.IS_ENCRYPT_PWD;
            // 密码加密，则对密码进行加密
            if (ConfigManager.IS_ENCRYPT_PWD) {
                // 设置时间戳
                String timestamp = sdf.format(Calendar.getInstance().getTime());
                message.setTimestamp(timestamp);
                // 对密码进行加密
                String encryptPwd = cHttpPost.encryptPwd(message.getUserid(), pwd, message.getTimestamp());
                // 设置加密后的密码
                message.setPwd(encryptPwd);
            } else {
                // 设置密码
                message.setPwd(pwd);
            }

            // 设置手机号码 此处只能设置一个手机号码
            message.setMobile(phone);
            // 设置内容
            message.setContent(content);
            // 业务类型
            message.setSvrtype("SMS001");

            // 返回的平台流水编号等信息
            StringBuffer msgId = new StringBuffer();
            // 返回值
            int result = -310099;
            // 发送短信
            result = cHttpPost.singleSend(message, msgId);
            // result为0:成功;非0:失败
            if (result == 0) {
                System.out.println("单条发送提交成功！");
                System.out.println(msgId.toString());
            } else {
                System.out.println("单条发送提交失败,错误码：" + result);
            }
            return result;
            //start(isEncryptPwd);
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 相同内容群发
     *
     * @param phone
     * @param content
     */
    public static int batchSend(String phone, String content, String userid, String pwd) {
        try {
            // 参数类
            Message message = new Message();

            // 实例化短信处理对象
            CHttpPost cHttpPost = new CHttpPost();

            // 设置账号 将 userid转成大写,以防大小写不一致
            message.setUserid(userid.toUpperCase());

            // 判断密码是否加密。
            // 密码加密，则对密码进行加密
            if (isEncryptPwd) {
                // 设置时间戳
                String timestamp = sdf.format(Calendar.getInstance().getTime());
                message.setTimestamp(timestamp);

                // 对密码进行加密
                String encryptPwd = cHttpPost.encryptPwd(message.getUserid(), pwd, message.getTimestamp());
                // 设置加密后的密码
                message.setPwd(encryptPwd);
            } else {
                // 设置密码
                message.setPwd(pwd);
            }

            // 设置手机号码
            message.setMobile(phone);
            // 设置内容
            message.setContent(content);
            // 业务类型
            // message.setSvrtype("SMS001");

            // 返回的平台流水编号等信息
            StringBuffer msgId = new StringBuffer();
            // 返回值
            int result = -310099;
            // 发送短信
            result = cHttpPost.batchSend(message, msgId);
            // result为0:成功;非0:失败
            if (result == 0) {
                System.out.println("相同内容发送提交成功！");

                System.out.println(msgId.toString());
            } else {
                System.out.println("相同内容发送提交失败,错误码：" + result);
            }
            return result;
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 个性化群发
     *
     * @param multixMts
     */
    public static int multiSend(List<MultiMt> multixMts, String userid, String pwd) {
        try {
            // 返回的流水号
            StringBuffer msgId = new StringBuffer();
            // 返回值
            int result = -310099;
            // 实例化短信处理对象
            CHttpPost cHttpPost = new CHttpPost();

            // 将 userid转成大写,以防大小写不一致
            SendSmsTemlpate.userid = userid.toUpperCase();
            // 判断密码是否加密。
            // 密码加密，则对密码进行加密
            String timestamp = null;
            if (isEncryptPwd) {
                // 设置时间戳
                timestamp = sdf.format(Calendar.getInstance().getTime());

                // 对密码进行加密
                SendSmsTemlpate.pwd = cHttpPost.encryptPwd(userid, pwd, timestamp);
            } else {
                // 不加密，不需要设置时间戳
                timestamp = null;
            }

            // 发送短信
            result = cHttpPost.multiSend(userid, pwd, timestamp, multixMts, msgId);
            // result为0:成功;非0:失败
            if (result == 0) {
                System.out.println("个性化群发提交成功！");
                System.out.println(msgId.toString());
            } else {
                System.out.println("个性化群发提交失败,错误码：" + result);
            }
            return result;
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 查询余额
     */
    public static int getBalance(String userid, String pwd) {
        try {
            // 返回值
            int result = -310099;

            // 实例化短信处理对象
            CHttpPost cHttpPost = new CHttpPost();

            // 将userid转成大写,以防大小写不一致
            SendSmsTemlpate.userid = userid.toUpperCase();
            // 密码加密，则对密码进行加密
            String timestamp = null;
            if (isEncryptPwd) {
                // 设置时间戳
                timestamp = sdf.format(Calendar.getInstance().getTime());

                // 对密码进行加密
                SendSmsTemlpate.pwd = cHttpPost.encryptPwd(userid, pwd, timestamp);
            } else {
                // 不加密，不需要设置时间戳
                timestamp = null;
            }

            // 调用查询余额的方法查询余额
            result = cHttpPost.getBalance(userid, pwd, timestamp);
            // 返回值大于等于0:查询成功;小于0:查询失败
            if (result >= 0) {
                System.out.println("查询余额成功，余额为：" + result);
            } else {
                System.out.println("查询余额失败，错误码为：" + result);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 查询剩余金额或条数接
     */
    public static void getRemains(String userid, String pwd) {
        // 返回值
        Remains remains = new Remains(-310099);
        try {
            // 实例化短信处理对象
            CHttpPost cHttpPost = new CHttpPost();

            // 将 userid转成大写,以防大小写不一致
            SendSmsTemlpate.userid = userid.toUpperCase();

            String timestamp = null;

            if (isEncryptPwd) {
                // 设置时间戳
                timestamp = sdf.format(Calendar.getInstance().getTime());

                // 对密码进行加密
                SendSmsTemlpate.pwd = cHttpPost.encryptPwd(userid, pwd, timestamp);
            } else {
                // 不加密，不需要设置时间戳
                timestamp = null;
            }

            // 调用查询余额的方法查询余额
            remains = cHttpPost.getRemains(userid, pwd, timestamp);

            // remains不为空
            if (remains != null) {
                // 查询成功
                if (remains.getResult() == 0) {
                    // 计费类型为0，条数计费
                    if (remains.getChargetype() == 0) {
                        System.out.println("查询成功,剩余条数为：" + remains.getBalance());
                    } else if (remains.getChargetype() == 1) {
                        // 计费类型为1，金额计费
                        System.out.println("查询成功,剩余金额为：" + remains.getMoney());
                    } else {
                        System.out.println("未知的计费类型,计费类型:" + remains.getChargetype());
                    }
                } else {
                    // 查询失败
                    System.out.println("查询失败,错误码为：" + remains.getResult());
                }
            } else {
                System.out.println("查询失败。");
            }
        } catch (Exception e) {
            // 异常处理
            e.printStackTrace();
        }
    }
}
