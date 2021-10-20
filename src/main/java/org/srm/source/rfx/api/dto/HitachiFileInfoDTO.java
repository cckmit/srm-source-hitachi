package org.srm.source.rfx.api.dto;

/**
 * @author guotao.yu@hand-china.com 2021/3/24 下午7:41
 */
public class HitachiFileInfoDTO {
    private String fileName;
    private String downloadUrl;
    private String type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
