package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.Ga4ghDataObject;

import java.util.Objects;

public class UseNameAsId implements IdGenerator {
    @Override
    public String generateId(Ga4ghDataObject object) {
        return Objects.requireNonNull(object.getName()).replaceAll("/", "__");
    }
}
