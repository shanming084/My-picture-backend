package com.shanming.mypicturebackend.model.enums;
import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("审核通过", 1),
    REJECT("审核拒绝", 0);

    private final Integer value;

    private final String description;

    PictureReviewStatusEnum(String description, Integer value) {
        this.description = description;
        this.value = value;
    }

    public static PictureReviewStatusEnum getPictureReviewStatus(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.getValue() == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
