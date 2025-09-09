package lib.net.util;

import java.util.HashMap;

import lib.net.command.ACommand;

public class UrlBuilder {

    // Hatanın düzeltildiği satır
    public static String build(String basePath, ACommand command) {
        StringBuilder urlBuilder = new StringBuilder(basePath);

        String relativeUrl = command.getRelativeUrl();
        if (relativeUrl != null && !relativeUrl.isEmpty()) {
            urlBuilder.append(relativeUrl);
        }

        HashMap<String, String> queryParams = command.getQueryParams();
        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            boolean firstParam = true;
            for (String key : queryParams.keySet()) {
                if (!firstParam) {
                    urlBuilder.append("&");
                }
                //urlBuilder.append(key).append("=").append(queryParams.get(key));

                urlBuilder.append(MyURLEncoder.encode(key))
                        .append("=")
                        .append(MyURLEncoder.encode(queryParams.get(key)));

                firstParam = false;
            }
        }

        return urlBuilder.toString();
    }

//    public static String build(String basePath, ACommand cmd){
//        StringBuilder sb = new StringBuilder(basePath);
//        String rel = cmd.getRelativeUrl(); if (rel!=null && !rel.isEmpty()) sb.append(rel);
//        HashMap<String,String> q = cmd.getQueryParams();
//        if (q!=null && !q.isEmpty()){
//            sb.append("?"); boolean first=true;
//            for (String k: q.keySet()){
//                if (!first) sb.append("&"); first=false;
//                sb.append(MyURLEncoder.encode(k)).append("=").append(MyURLEncoder.encode(q.get(k)));
//            }
//        }
//        return sb.toString();
//    }
}

