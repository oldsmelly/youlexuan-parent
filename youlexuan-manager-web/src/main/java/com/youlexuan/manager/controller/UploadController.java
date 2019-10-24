package com.youlexuan.manager.controller;

import com.youlexuan.entity.Result;
import com.youlexuan.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    /**
     * Result
     * 前台需要得到上传后的图片的url地址作为回显
     * result.message = url
     * @return
     */
    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            String fileId = fastDFSClient.uploadFile(file.getBytes(), extName);
            String picUrl = FILE_SERVER_URL+fileId;
            return new Result(true,picUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,e.toString());
        }


    }
}
