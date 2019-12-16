package com.dnastack.drsclient.idgen;

import com.dnastack.drsclient.model.DrsObject;

public interface IdGenerator {
    public String generateId(DrsObject object);
}
