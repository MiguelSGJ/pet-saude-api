package com.arboviroses.conectaDengue.Api.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgravoCountBySemanaEpidemiologica 
{
    private int epidemiologicalWeek;
    private long casesCount;   
    private String agravoId; 
}
