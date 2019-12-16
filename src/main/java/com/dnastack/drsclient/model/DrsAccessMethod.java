package com.dnastack.drsclient.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DrsAccessMethod {
    public enum AccessType{
        s3,gs,ftp,gsiftp,globus,htsget,https,file;
    }

    private AccessType type;

    private DrsAccessUrl access_url; //at least one of access_url or access_id must be specified.

    private String access_id;

    private String region;
}
