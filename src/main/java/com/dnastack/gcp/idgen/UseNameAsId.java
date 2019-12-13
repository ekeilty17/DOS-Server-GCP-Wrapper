package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.DrsObject;

import java.util.Objects;

public class UseNameAsId implements IdGenerator {
    @Override
    public String generateId(DrsObject object) {
        return Objects.requireNonNull(object.getName()).replaceAll("/", "__");
    }
}
