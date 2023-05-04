package com.offcn.product.Controller;

import com.offcn.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("admin/product/")
public class FileUploadController {
    @Value("${fileServer.url}")//这里注意$
    private String fileUrl;

    @RequestMapping("fileUpload")
    public Result<String> fileUpload(MultipartFile file) throws Exception{
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        //因为我的储存地址有空格，把空格取消掉
        if (configFile.contains("%20")){
            configFile = configFile.replace("%20"," ");
        }
        String path = null;

        if (configFile!=null){
            // 初始化
            ClientGlobal.init(configFile);
            // 创建trackerClient
            TrackerClient trackerClient = new TrackerClient();
            // 获取trackerService
            TrackerServer trackerServer = trackerClient.getConnection();
            // 创建storageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
            //      upload_appender_file1 上传后修改
            path = storageClient1.upload_appender_file1(file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
            System.out.println(fileUrl + path);
        }
        return Result.ok(fileUrl+path);
    }



}
