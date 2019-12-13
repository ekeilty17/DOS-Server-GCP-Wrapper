package com.dnastack.gcp.idgen;

import com.dnastack.gcp.model.DrsObject;

public class SequenceIdGenerator implements IdGenerator {
    private long nextId;

    public SequenceIdGenerator(long startAt) {
        this.nextId = startAt;
    }

    @Override
    public String generateId(DrsObject object) {
        String id = String.valueOf(nextId);
        nextId++;
        return id;
    }
}
