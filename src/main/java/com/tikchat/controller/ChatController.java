package com.tikchat.controller;

import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.ChatMessage;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.service.ChatMessageService;
import com.tikchat.service.ChatSessionUserService;
import com.tikchat.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * @ClassName ChatController
 * @Description
 * @Author Paprika
 * @date 2024-07-20
 **/
@RestController("chatController")
@RequestMapping("/chat")
public class ChatController extends ABaseControllertest{
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private AppConfig appConfig;

//    @Resource
//    private RedisComponent redisComponent;

    //某人主动发消息给别人
    @RequestMapping("sendMesaage")
    @GlobalInterceptor
    public ResponseVO sendMessage(HttpServletRequest req, @NotEmpty String contactId,
                                  @NotEmpty @Max(999) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType){

        //发送消息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageType(messageType);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileType(fileType);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);//用于返回给前端

        return getSuccessResponseVO(messageSendDto);
    }

    //别人发文件消息来 我们要把文件上传到我们这个服务器中！
    @RequestMapping("uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(HttpServletRequest req, @NotEmpty String messageId,
                                 @NotNull MultipartFile file,
                                 @NotNull MultipartFile cover){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        chatMessageService.saveMessageFile(tokenUserInfoDto.getUserId(),messageId,file,cover);
        return getSuccessResponseVO(null);
    }


    //接收文件 a->服务器  b点击接收或者自动接收：服务器->b
    /*这个路由是：通过OutputStream流返回的 （自定义response来返回信息）*/
    @RequestMapping("downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest req, HttpServletResponse res, @NotEmpty String fileIdInFilename,
                                   @NotNull Boolean showCover){
//        String fileIdInFilename = fileId;
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        File file = null;
        FileInputStream in = null;
        OutputStream out = null;
        try {
            //头像的下载
            if(!StringTools.isNumber(fileIdInFilename)){//真他妈的   写这行代码时还没写头像的设置吧好像？
                // 这个还没写 靠  头像图片文件是以用户或群聊的id来命令的  U13241或者G13241  所以不是数字！！！！他妈的
                String avatarFolderPath = appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE+Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = avatarFolderPath + fileIdInFilename + Constants.IMAGE_SUFFIX;
                if(showCover){
                    avatarPath = avatarPath + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if(!file.exists()){
                    throw new BusinessException(ResponseCodeEnum.CODE_602);
                }
            }
            //视频图片的下载（也很有可能包含Cover）
            else{//是纯数字说明是messageId 因为messageId是纯数字  而头像的文件是以U123或者G325命名
                /* 下载文件 */
                file = chatMessageService.downloadFile(tokenUserInfoDto.getUserId(), Long.parseLong(fileIdInFilename), showCover);
            }

            res.setContentType("application/x-msdownload;charset=UTF-8");
            res.setHeader("Content-Disposition", "attachment");
            res.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = res.getOutputStream();
            int len;
            while((len=in.read(byteData))!=-1){
                out.write(byteData,0,len);
            }
            out.flush();

        }catch (Exception e){
            logger.error("下载文件失败",e);
        }finally {

            //自己主动让IO流关闭  而不是让java自动关闭 其实自动关闭也是可以  这一块可以学习一下
            if(out!=null){
                try {
                    out.close();
                }catch (Exception e){
                    logger.error("IO异常",e);
                }
            }
            if(in!=null){
                try {
                    in.close();
                }catch (Exception e){
                    logger.error("IO异常",e);
                }
            }

        }
    }


}
