package logia.assistant.gateway.web.rest.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import logia.assistant.share.common.utils.UuidUtils;

public class UuidTest {

    public static void main(String[] args) throws IOException {
        Set<String> uuids = new HashSet<>();
        for (int i = 0; i < 200000; i++) {
            uuids.add(UuidUtils.newSecureUUIDString());
        }
        FileUtils.writeLines(new File("/home/hanhle/jhipster/assistant-gateway/target/uuids.txt"), uuids);
    }

}
