package com.nexfi.yuanpeigen.nexfi_android_ble.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.GroupImageActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.ModifyInformationActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.UserInformationActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.TextMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.VoiceMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.GroupVoicePlayClickListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.BitMapUtil;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;

import java.util.List;

/**
 * Created by Mark on 2016/4/17.
 */
public class GroupChatAdapater extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<GroupChatMessage> coll;
    private Context mContext;
    private String userSelfId;
    private int pageSize;
    private int startIndex;


    public final static int SEND_LEFT = 30;
    public final static int SEND_RIGHT = 31;
    public final static int IMAGE_LEFT = 32;
    public final static int IMAGE_RIGHT = 33;
    public final static int VOICE_LEFT = 34;
    public final static int VOICE_RIGHT = 35;

    private int mMinItemWith;// 设置对话框的最大宽度和最小宽度
    private int mMaxItemWith;


    public GroupChatAdapater(Context context, List<GroupChatMessage> coll, String userSelfId,int pageSize,int startIndex) {
        this.coll = coll;
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.userSelfId = userSelfId;
        this.pageSize=pageSize;
        this.startIndex=startIndex;

        // 获取系统宽度
        WindowManager wManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wManager.getDefaultDisplay().getMetrics(outMetrics);
        mMaxItemWith = (int) (outMetrics.widthPixels * 0.7f);
        mMinItemWith = (int) (outMetrics.widthPixels * 0.15f);
    }


    private static final String TAG = ChatMessageAdapater.class.getSimpleName();

    @Override
    public int getViewTypeCount() {
        return 40;
    }

    @Override
    public int getItemViewType(int position) {
        GroupChatMessage entity = coll.get(position);
        switch (entity.messageBodyType) {
            case MessageBodyType.eMessageBodyType_Text:
                if (entity.userMessage.userId.equals(userSelfId)) {
                    return SEND_RIGHT;
                } else {
                    return SEND_LEFT;
                }
            case MessageBodyType.eMessageBodyType_Image:
                if (entity.userMessage.userId.equals(userSelfId)) {
                    return IMAGE_RIGHT;
                } else {
                    return IMAGE_LEFT;
                }
            case MessageBodyType.eMessageBodyType_Voice:
                if (entity.userMessage.userId.equals(userSelfId)) {
                    return VOICE_RIGHT;
                } else {
                    return VOICE_LEFT;
                }
        }
        return -1;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int position) {
        return coll.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = null;
        final GroupChatMessage entity = coll.get(position);
        int msgBodyType = entity.messageBodyType;

        TextMessage textMessage = null;
        VoiceMessage voiceMsg = null;

        switch (msgBodyType) {
            case MessageBodyType.eMessageBodyType_Text:
                textMessage = entity.textMessage;
                break;
            case MessageBodyType.eMessageBodyType_Image:
                FileMessage fileMessage = entity.fileMessage;
                break;
            case MessageBodyType.eMessageBodyType_Voice://语音消息
                voiceMsg = entity.voiceMessage;
                break;
        }

        ViewHolder_chatSend viewHolder_chatSend = null;
        ViewHolder_sendImage viewHolder_sendImage = null;
        ViewHolder_voice viewHolder_voice = null;

        if (convertView == null) {

            viewHolder_chatSend = new ViewHolder_chatSend();
            viewHolder_sendImage = new ViewHolder_sendImage();
            viewHolder_voice = new ViewHolder_voice();

            switch (msgBodyType) {
                case MessageBodyType.eMessageBodyType_Text:
                    if (entity.userMessage.userId.equals(userSelfId)) {//自己是发送(右)，别人是接收(左)
                        convertView = mInflater.inflate(R.layout.item_chatting_msg_send_group, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_chatting_msg_receive_group, null);
                    }

                    viewHolder_chatSend.tv_chatText_send = (TextView) convertView.findViewById(R.id.tv_chatText_send);
                    viewHolder_chatSend.tv_sendTime_send = (TextView) convertView.findViewById(R.id.tv_sendtime_send);
                    viewHolder_chatSend.iv_userhead_send_chat = (ImageView) convertView.findViewById(R.id.iv_userhead_send);
                    viewHolder_chatSend.tv_userNick_send = (TextView) convertView.findViewById(R.id.tv_userNick_send);
                    convertView.setTag(viewHolder_chatSend);
                    break;

                case MessageBodyType.eMessageBodyType_Image:
                    if (entity.userMessage.userId.equals(userSelfId)) {
                        convertView = mInflater.inflate(R.layout.item_send_image_group, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_recevied_imge_group, null);
                    }
                    viewHolder_sendImage.chatcontent_send = (RelativeLayout) convertView.findViewById(R.id.chatcontent_send);
                    viewHolder_sendImage.iv_icon_send = (ImageView) convertView.findViewById(R.id.iv_icon_send);
                    viewHolder_sendImage.iv_userhead_send_image = (ImageView) convertView.findViewById(R.id.iv_userhead_send_image);
                    viewHolder_sendImage.tv_sendTime_send_image = (TextView) convertView.findViewById(R.id.tv_sendTime_send_image);
                    viewHolder_sendImage.pb_send = (ProgressBar) convertView.findViewById(R.id.pb_send);
                    viewHolder_sendImage.tv_userNick_send_image = (TextView) convertView.findViewById(R.id.tv_userNick_send_image);
                    convertView.setTag(viewHolder_sendImage);
                    break;

                case MessageBodyType.eMessageBodyType_Voice:
                    if (entity.userMessage.userId.equals(userSelfId)) {
                        convertView = mInflater.inflate(R.layout.item_send_voice_group, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_receice_voice_group, null);
                    }
                    viewHolder_voice.length = convertView.findViewById(R.id.recorder_length);
                    viewHolder_voice.seconds = (TextView) convertView.findViewById(R.id.recorder_time);
                    viewHolder_voice.userHeadIcon = (ImageView) convertView.findViewById(R.id.item_icon);
                    viewHolder_voice.tv_userNick = (TextView) convertView.findViewById(R.id.tv_userNick);
                    viewHolder_voice.id_recorder_anim = (ImageView) convertView.findViewById(R.id.id_recorder_anim);//
                    convertView.setTag(viewHolder_voice);
                    break;
            }
        } else {
            switch (msgBodyType) {
                case MessageBodyType.eMessageBodyType_Text:
                    viewHolder_chatSend = (ViewHolder_chatSend) convertView.getTag();
                    break;
                case MessageBodyType.eMessageBodyType_Image:
                    viewHolder_sendImage = (ViewHolder_sendImage) convertView.getTag();
                    break;
                case MessageBodyType.eMessageBodyType_Voice:
                    viewHolder_voice = (ViewHolder_voice) convertView.getTag();
                    break;
            }

        }


        switch (msgBodyType) {
            case MessageBodyType.eMessageBodyType_Text:
                viewHolder_chatSend.iv_userhead_send_chat.setImageResource(BleApplication.iconMap.get(entity.userMessage.userAvatar));
                if (entity.userMessage.userId.equals(userSelfId)) {
                    viewHolder_chatSend.iv_userhead_send_chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ModifyInformationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    viewHolder_chatSend.iv_userhead_send_chat.setOnClickListener(new AvatarClick(position));
                }
                viewHolder_chatSend.tv_sendTime_send.setText(entity.timeStamp);
                viewHolder_chatSend.tv_chatText_send.setText(textMessage.fileData);
                viewHolder_chatSend.tv_userNick_send.setText(entity.userMessage.userNick);
                break;

            case MessageBodyType.eMessageBodyType_Image:
                try {
                    String nexModel = android.os.Build.MODEL;
                    if (nexModel.equals("Nexus 5X")) {
                        final byte[] bys_send = Base64.decode(entity.fileMessage.fileData, Base64.DEFAULT);
                        Bitmap bitmap = FileTransferUtils.getPicFromBytes(bys_send);
                        viewHolder_sendImage.iv_icon_send.setImageBitmap(bitmap);
                        viewHolder_sendImage.iv_icon_send.setScaleType(ImageView.ScaleType.FIT_XY);
                    }else{
                        final Bitmap bitmap = BitMapUtil.getBitmap(entity.fileMessage.filePath, 100, 100);
                        viewHolder_sendImage.iv_icon_send.setImageBitmap(bitmap);
                        viewHolder_sendImage.iv_icon_send.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }catch (OutOfMemoryError error){
                    final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.icon_loading);
                    viewHolder_sendImage.iv_icon_send.setImageBitmap(bitmap);
                    viewHolder_sendImage.iv_icon_send.setScaleType(ImageView.ScaleType.FIT_XY);
                }
                viewHolder_sendImage.iv_userhead_send_image.setImageResource(BleApplication.iconMap.get(entity.userMessage.userAvatar));
                if (entity.userMessage.userId.equals(userSelfId)) {
                    viewHolder_sendImage.iv_userhead_send_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ModifyInformationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    viewHolder_sendImage.iv_userhead_send_image.setOnClickListener(new AvatarClick(position));
                }
                viewHolder_sendImage.tv_sendTime_send_image.setText(entity.timeStamp);
                viewHolder_sendImage.tv_userNick_send_image.setText(entity.userMessage.userNick);
                if (entity.fileMessage.isPb == 0) {
                    viewHolder_sendImage.pb_send.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder_sendImage.pb_send.setVisibility(View.VISIBLE);
                }
                viewHolder_sendImage.chatcontent_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, GroupImageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("page", position);
                        intent.putExtra("pageSize",pageSize);
                        intent.putExtra("startIndex", startIndex);
                        intent.putExtra("filePath",entity.fileMessage.filePath);
                        mContext.startActivity(intent);
                        ((Activity) mContext).overridePendingTransition(R.anim.img_scale_in, R.anim.img_scale_out);
                    }
                });
                break;

            case MessageBodyType.eMessageBodyType_Voice:
                viewHolder_voice.seconds.setText(Math.round(Double.parseDouble(voiceMsg.durational)) + "\"");
                ViewGroup.LayoutParams lParams = viewHolder_voice.length.getLayoutParams();
                lParams.width = (int) (mMinItemWith + mMaxItemWith / 60f * Double.parseDouble(voiceMsg.durational));
                viewHolder_voice.length.setLayoutParams(lParams);
                viewHolder_voice.userHeadIcon.setImageResource(BleApplication.iconMap.get(entity.userMessage.userAvatar));
                if (entity.userMessage.userId.equals(userSelfId)) {
                    viewHolder_voice.userHeadIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ModifyInformationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    viewHolder_voice.userHeadIcon.setOnClickListener(new AvatarClick(position));
                }
                viewHolder_voice.id_recorder_anim.setOnClickListener(new GroupVoicePlayClickListener(entity, viewHolder_voice.id_recorder_anim, userSelfId, this, position));
                viewHolder_voice.tv_userNick.setText(entity.userMessage.userNick);
                break;
        }
        return convertView;
    }


    static class ViewHolder_chatSend {
        public TextView tv_chatText_send, tv_sendTime_send, tv_userNick_send;
        public ImageView iv_userhead_send_chat;
    }

    static class ViewHolder_sendImage {
        public TextView tv_sendTime_send_image, tv_userNick_send_image;
        public ImageView iv_userhead_send_image, iv_icon_send;
        public RelativeLayout chatcontent_send;
        public ProgressBar pb_send;
    }


    static class ViewHolder_voice {
        public TextView seconds, tv_userNick;// 时间
        public View length;// 对话框长度
        public ImageView userHeadIcon;
        public ImageView id_recorder_anim;
    }

    //单击事件实现
    class AvatarClick implements View.OnClickListener {
        public int position;

        public AvatarClick(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(mContext, UserInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data_obj", coll.get(position).userMessage);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

}