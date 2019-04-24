package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.Ga4ghDataObject;

public interface IdGenerator {
    public String generateId(Ga4ghDataObject object);
}
