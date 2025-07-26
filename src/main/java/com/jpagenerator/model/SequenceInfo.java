package com.jpagenerator.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SequenceInfo {
    private String columnName;
    private String sequenceName;
    private String sequenceSchema;

    public SequenceInfo() {
    }

}