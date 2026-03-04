package com.arboviroses.conectaDengue.Api.DTO.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AgravoCountByAgeRange {
    private Map<String, Integer> count;
}
