package com.mrgrd56.illusioncardsdownloader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NamedFile {
    private String name;
    private byte[] content;
}
