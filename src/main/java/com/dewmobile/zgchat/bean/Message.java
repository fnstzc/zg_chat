package com.dewmobile.zgchat.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 本地保存的聊天消息体
 *
 * @author zc
 * @date 2019-09-30
 */
@Data
@AllArgsConstructor
public class Message {
    private String id;
    private String type;
    private String from;
    private String msgId;
    private String msgType;
    private String subMsgType;
    private String msg;
    private String url;
    private String fullMsg;
    private Long date;
}
