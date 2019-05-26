package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties({UploadProperties.class})
public class UploadService {
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private UploadProperties pror;


    //private static final List<String> ALLOW_TYPES = Arrays.asList("image/png","image/jpeg","image/bmp");
    public String uploadImage(MultipartFile file) {


        try {
            //校验文件
            String contentType = file.getContentType();
            if (!pror.getAllowTypes().contains(contentType))
            {
                throw new LyException(ExceptionEnumm.INVALID_FILE_TYPE);
            }

            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image ==null)
            {
                throw new LyException(ExceptionEnumm.INVALID_FILE_TYPE);
            }
            //上传到FastDFS
            String extention = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extention, null);

            //返回路径
            return pror.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            //上传失败
            log.error("上传文件失败！",e);
            throw new LyException(ExceptionEnumm.UPLOAD_FILE_ERROR);
        }
        //保存文件到本地


    }
}
