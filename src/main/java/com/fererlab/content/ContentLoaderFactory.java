package com.fererlab.content;

public class ContentLoaderFactory {
    public static ContentLoader createLoader(String scheme) {
        switch (scheme) {
            case "jdbc":
                return new JDBCContentLoader();
            case "ftp":
                return new FtpContentLoader();
            case "http":
                return new HttpContentLoader();
            case "https":
                return new HttpsContentLoader();
            default:
                return new FileContentLoader();
        }
    }
}
