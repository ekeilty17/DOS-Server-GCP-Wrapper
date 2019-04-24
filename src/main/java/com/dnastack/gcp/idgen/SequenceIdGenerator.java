package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.Ga4ghDataObject;

public class SequenceIdGenerator implements IdGenerator {
    private long nextId;

    public SequenceIdGenerator(long startAt) {
        this.nextId = startAt;
    }

    @Override
    public String generateId(Ga4ghDataObject object) {
        String id = String.valueOf(nextId);
        nextId++;
        return id;
    }
}
