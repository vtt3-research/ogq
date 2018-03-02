package metavideo.vo;

public class AccountVO {
    private String accountId;
    private String accountPw;
    private String organ;
    private String accountName;
    private String email;
    private String registeredDt;
    private String lastUpdateDt;
    private String certify;

    @Override
    public String toString() {
        return "AccountVO{" +
                "accountId='" + accountId + '\'' +
                ", accountPw='" + accountPw + '\'' +
                ", organ='" + organ + '\'' +
                ", accountName='" + accountName + '\'' +
                ", email='" + email + '\'' +
                ", registeredDt='" + registeredDt + '\'' +
                ", lastUpdateDt='" + lastUpdateDt + '\'' +
                ", certify='" + certify + '\'' +
                '}';
    }

    public String getCertify() {
        return certify;
    }

    public void setCertify(String certify) {
        this.certify = certify;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountPw() {
        return accountPw;
    }

    public void setAccountPw(String accountPw) {
        this.accountPw = accountPw;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountVO accountVO = (AccountVO) o;

        return accountId != null ? accountId.equals(accountVO.accountId) : accountVO.accountId == null;
    }

    @Override
    public int hashCode() {
        return accountId != null ? accountId.hashCode() : 0;
    }


}
