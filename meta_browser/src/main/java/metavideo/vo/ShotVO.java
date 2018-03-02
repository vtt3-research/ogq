package metavideo.vo;

public class ShotVO {
    private int contentId;
    private String shotTime;
    private String seekPos;
    private String sceneTags;

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getShotTime() {
        return shotTime;
    }

    public void setShotTime(String shotTime) {
        this.shotTime = shotTime;
    }

    public String getSeekPos() {
        return seekPos;
    }

    public void setSeekPos(String seekPos) {
        this.seekPos = seekPos;
    }

    public String getSceneTags() {
        return sceneTags;
    }

    public void setSceneTags(String sceneTags) {
        this.sceneTags = sceneTags;
    }

    @Override
    public String toString() {
        return "ShotVO{" +
                "contentId=" + contentId +
                ", shotTime='" + shotTime + '\'' +
                ", seekPos='" + seekPos + '\'' +
                ", sceneTags='" + sceneTags + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShotVO shotVO = (ShotVO) o;

        if (contentId != shotVO.contentId) return false;
        return shotTime != null ? shotTime.equals(shotVO.shotTime) : shotVO.shotTime == null;
    }

    @Override
    public int hashCode() {
        int result = contentId;
        result = 31 * result + (shotTime != null ? shotTime.hashCode() : 0);
        return result;
    }
}
