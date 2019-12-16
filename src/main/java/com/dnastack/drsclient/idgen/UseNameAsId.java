package com.dnastack.drsclient.idgen;

import com.dnastack.drsclient.model.DrsObject;

import java.util.Objects;

public class UseNameAsId implements IdGenerator {
    @Override
    public String generateId(DrsObject object) {
        return Objects.requireNonNull(object.getName()).replaceAll("/", "__");
    }
}
