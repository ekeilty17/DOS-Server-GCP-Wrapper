package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.DrsObject;

public interface IdGenerator {
    public String generateId(DrsObject object);
}
