package com.james.chat.controller;

import com.james.chat.result.Result;
import com.james.chat.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
public class ImageController {

    private static final String ROOT_PATH = "C:\\data";

    private static final String[] allowedFormats = new String[]{".jpg",".jpeg",".png",".gif"};

    private static final long _1MB = 1024*1024;
    @PostMapping("user/b/upload_image")
    public Result<String> uploadImages(@RequestParam("file")MultipartFile multipartFile,@RequestParam("filename") String filename) throws Exception{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        String username = (String) authentication.getPrincipal();
        if (username == null) {
            return new Result<>(ResultCode.FAILED, false, "认证失败", null);
        }
        File userDir = new File(ROOT_PATH+"\\"+username);
        if (!userDir.exists()) {
            userDir.mkdir();
        }
        System.out.println(multipartFile.getSize());
        if (!checkFileFormat(filename) || multipartFile.getSize() > _1MB) {
            return new Result<>(500,false,"上传失败","文件不符合要求");
        }
        File file = new File(ROOT_PATH+"\\"+username+"\\"+filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        AsynchronousFileChannel asynchronousFileChannel = AsynchronousFileChannel.open(Paths.get(ROOT_PATH+"\\"+username+"\\"+filename), StandardOpenOption.WRITE);
        ByteBuffer buffer = ByteBuffer.allocate((int)multipartFile.getSize());
        buffer.put(multipartFile.getBytes());
        buffer.flip();
        asynchronousFileChannel.write(buffer, 0);
        return new Result<>(200,true,"头像上传成功", "http://localhost:8000/app/resources/"+username+"/"+filename);
    }

    private boolean checkFileFormat(String filename) {
        for (String suffix : allowedFormats) {
            if (filename.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
