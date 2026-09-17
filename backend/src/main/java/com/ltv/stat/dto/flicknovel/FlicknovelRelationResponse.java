package com.ltv.stat.dto.flicknovel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class FlicknovelRelationResponse extends FlicknovelBaseResponse<FlicknovelRelationResponse.RelationData> {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelationData {
        @JsonProperty("relations")
        private List<FlicknovelRelationDto> relations;

        public List<FlicknovelRelationDto> getRelations() {
            return relations != null ? relations : Collections.emptyList();
        }

        public void setRelations(List<FlicknovelRelationDto> relations) {
            this.relations = relations;
        }
    }
}
