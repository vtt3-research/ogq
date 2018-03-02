package metavideo.vo;

public class ContentVO {
    private int contentId;
    private String accountId;
    private String contentTitle;
    private String explanation;
    private String registeredDt;
    private String lastUpdateDt;
    private String previewImagePath;
    private String agreeYn;
    private String entireTags;
    private String videoRunningTime;
    private int videoFileSize;
    private String videoFileUrl;
    private String metaFileUrl;

    private String accountName;
    private String organ;

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getRegisteredDt() {
        return registeredDt;
    }

    public void setRegisteredDt(String registeredDt) {
        this.registeredDt = registeredDt;
    }

    public String getLastUpdateDt() {
        return lastUpdateDt;
    }

    public void setLastUpdateDt(String lastUpdateDt) {
        this.lastUpdateDt = lastUpdateDt;
    }

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public void setPreviewImagePath(String previewImagePath) {
        this.previewImagePath = previewImagePath;
    }

    public String getAgreeYn() {
        return agreeYn;
    }

    public void setAgreeYn(String agreeYn) {
        this.agreeYn = agreeYn;
    }

    public String getEntireTags() {
        return entireTags;
    }

    public void setEntireTags(String entireTags) {
        this.entireTags = entireTags;
    }

    public String getVideoRunningTime() {
        return videoRunningTime;
    }

    public void setVideoRunningTime(String videoRunningTime) {
        this.videoRunningTime = videoRunningTime;
    }

    public int getVideoFileSize() {
        return videoFileSize;
    }

    public void setVideoFileSize(int videoFileSize) {
        this.videoFileSize = videoFileSize;
    }

    public String getVideoFileUrl() {
        return videoFileUrl;
    }

    public void setVideoFileUrl(String videoFileUrl) {
        this.videoFileUrl = videoFileUrl;
    }

    public String getMetaFileUrl() {
        return metaFileUrl;
    }

    public void setMetaFileUrl(String metaFileUrl) {
        this.metaFileUrl = metaFileUrl;
    }

    @Override
    public String toString() {
        return "ContentVO{" +
                "contentId=" + contentId +
                ", accountId='" + accountId + '\'' +
                ", contentTitle='" + contentTitle + '\'' +
                ", explanation='" + explanation + '\'' +
                ", registeredDt='" + registeredDt + '\'' +
                ", lastUpdateDt='" + lastUpdateDt + '\'' +
                ", previewImagePath='" + previewImagePath + '\'' +
                ", agreeYn='" + agreeYn + '\'' +
                ", entireTags='" + entireTags + '\'' +
                ", videoRunningTime='" + videoRunningTime + '\'' +
                ", videoFileSize=" + videoFileSize +
                ", videoFileUrl='" + videoFileUrl + '\'' +
                ", metaFileUrl='" + metaFileUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentVO contentVO = (ContentVO) o;

        return contentId == contentVO.contentId;
    }

    @Override
    public int hashCode() {
        return contentId;
    }
}
