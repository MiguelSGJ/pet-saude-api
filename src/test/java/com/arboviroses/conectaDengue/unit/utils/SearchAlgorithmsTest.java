package com.arboviroses.conectaDengue.unit.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arboviroses.conectaDengue.Utils.Search.SearchAlgorithms;

public class SearchAlgorithmsTest {
    
    @Test
    public void test_if_levenstein_algorithm_is_returning_correct_values()
    {
        Assertions.assertEquals(0, SearchAlgorithms.levenstein("", ""));
        Assertions.assertEquals(1, SearchAlgorithms.levenstein("teste", "tesre"));
        Assertions.assertEquals(9, SearchAlgorithms.levenstein("testesssssssss", "teste"));
        Assertions.assertEquals(9, SearchAlgorithms.levenstein("teste", "testesssssssss"));
    }
}
