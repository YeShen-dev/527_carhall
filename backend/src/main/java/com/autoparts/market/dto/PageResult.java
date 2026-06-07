package com.autoparts.market.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "分页结果")
@Data
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> content;
    @Schema(description = "总记录数")
    private long totalElements;
    @Schema(description = "总页数")
    private int totalPages;
    @Schema(description = "当前页码（从0开始）")
    private int number;
    @Schema(description = "每页大小")
    private int size;

    public static <T> PageResult<T> from(Page<?> mpPage, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.content = records;
        result.totalElements = mpPage.getTotal();
        result.totalPages = (int) mpPage.getPages();
        result.number = (int) mpPage.getCurrent() - 1;
        result.size = (int) mpPage.getSize();
        return result;
    }
}
