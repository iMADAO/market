package cn.haizhi.market.other.util;

import cn.haizhi.market.main.bean.madao.PageInfo;

public class PageInfoUtils {
    public static PageInfo getPageInfo(int pageNum, int pageSize, int listSize){
        PageInfo pageInfo = new PageInfo();

        if(pageNum<1)
            pageNum = 1;
        long total = listSize;

        int pages = ((int)total / pageSize);
        if(total%pageSize!=0)
            pages++;

        if (pageNum>pages)
            pageNum = pages;

        int startRow = (pageNum-1) * pageSize + 1;
        int endRow = pageNum * pageSize;
        endRow = endRow > total? (int) total : endRow;

        int size = endRow - startRow + 1;

        int prePage = pageNum==1 ? 1 :pageNum-1;
        int nextPage = pageNum==pages ? pages : pageNum+1;
        boolean hasNextPage = false;
        boolean hasPreviousPage = false;
        if(pageNum>1)
            hasPreviousPage=true;
        if(pageNum<pages){
            hasNextPage=true;
        }
        boolean isFirstPage = false;
        boolean isLastPage = false;
        if(pageNum==1)
            isFirstPage = true;
        if(pageNum==pages)
            isLastPage = true;
        int firstPage = 1;
        int lastPage = pages;

        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setSize(size);
        pageInfo.setStartRow(startRow);
        pageInfo.setEndRow(endRow);
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        pageInfo.setPrePage(prePage);
        pageInfo.setNextPage(nextPage);
        pageInfo.setFirstPage(isFirstPage);
        pageInfo.setLastPage(isLastPage);
        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setHasNextPage(hasNextPage);
        pageInfo.setFirstPage(firstPage);
        pageInfo.setLastPage(lastPage);
        return pageInfo;

    }
}
