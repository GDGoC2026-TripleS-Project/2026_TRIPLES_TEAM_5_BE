package com.triples.team5be.domain.tag.dto;

import java.util.List;

public record SavePostSituationTagsRequest(
        List<Long> tagIds) {
}