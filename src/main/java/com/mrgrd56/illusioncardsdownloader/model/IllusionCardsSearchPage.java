package com.mrgrd56.illusioncardsdownloader.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IllusionCardsSearchPage {
    private List<String> results;
    private Integer pagesCount;
}
