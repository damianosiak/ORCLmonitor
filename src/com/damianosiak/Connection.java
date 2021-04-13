package com.damianosiak;

/**This class is responsible for connection object*/
public class Connection {
    private String connectionName;
    private String connectionAddress;
    private String connectionPort;
    private String connectionServiceNameOrSID;
    private String connectionUser;
    private String connectionPassword;
    private String connectionRefreshRate;

    public Connection(String connectionName, String connectionAddress, String connectionPort, String connectionServiceNameOrSID, String connectionUser, String connectionPassword, String connectionRefreshRate) {
        this.connectionName = connectionName;
        this.connectionAddress = connectionAddress;
        this.connectionPort = connectionPort;
        this.connectionServiceNameOrSID = connectionServiceNameOrSID;
        this.connectionUser = connectionUser;
        this.connectionPassword = connectionPassword;
        this.connectionRefreshRate = connectionRefreshRate;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionAddress() {
        return connectionAddress;
    }

    public void setConnectionAddress(String connectionAddress) {
        this.connectionAddress = connectionAddress;
    }

    public String getConnectionPort() {
        return connectionPort;
    }

    public void setConnectionPort(String connectionPort) {
        this.connectionPort = connectionPort;
    }

    public String getConnectionServiceNameOrSID() {
        return connectionServiceNameOrSID;
    }

    public void setConnectionServiceNameOrSID(String connectionServiceNameOrSID) {
        this.connectionServiceNameOrSID = connectionServiceNameOrSID;
    }

    public String getConnectionUser() {
        return connectionUser;
    }

    public void setConnectionUser(String connectionUser) {
        this.connectionUser = connectionUser;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getConnectionRefreshRate() {
        return connectionRefreshRate;
    }

    public void setConnectionRefreshRate(String connectionRefreshRate) {
        this.connectionRefreshRate = connectionRefreshRate;
    }
}
