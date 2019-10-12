package com.dewmobile.zgchat.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dewmobile.zgchat.bean.Message;
import org.bson.Document;

/**
 * 客户端消息解析
 *
 * @author zc
 * @date 2019-10-08
 */
public class MsgUtil {

    private static final String MSG_TYPE_FILE = "file";
    private static final String MSG_TYPE_TXT = "txt";

    public static Message resolve2Message(String messageJson) {
        JSONObject obj = JSON.parseObject(messageJson);
        JSONObject msgJson = obj.getJSONObject("msg");
        JSONObject extJson = obj.getJSONObject("ext");

        String id = msgJson.getString("msg_id");
        String type = msgJson.getString("type");
        String from = extJson.getString("from");
        String msgId = "";
        Long date = msgJson.getLong("tm");

        String msg = msgJson.getString("msg");
        String msgType = "";
        String subMsgType = "";
        String url = "";

        if (msg != null) {
            if (msg.contains("[")) {
                msgType = MSG_TYPE_FILE;
                subMsgType = extJson.getString("z_msg_type");
                url = extJson.getString("z_msg_url");
            } else {
                msgType = MSG_TYPE_TXT;
            }
        }

        return new Message(id, type, from, msgId, msgType, subMsgType, msg, url, messageJson, date);
    }

}
