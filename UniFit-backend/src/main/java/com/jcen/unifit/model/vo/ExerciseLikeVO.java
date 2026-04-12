package com.jcen.unifit.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLikeVO {

    private boolean liked;

    private Integer likeCount;
}
