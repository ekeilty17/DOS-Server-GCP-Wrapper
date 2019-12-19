package com.dnastack.drsclient;

import com.dnastack.drsclient.model.DrsObject;
import com.google.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DrsObjectList {
    private List<DrsObject> objects;
}
