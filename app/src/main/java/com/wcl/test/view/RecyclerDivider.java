package com.wcl.test.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 分割线装饰器，支持 LinearLayoutManager 和 GridLayoutManager，
 * 可配置颜色、间距、分割线粗细及是否包含边缘间距。
 */
public class RecyclerDivider extends RecyclerView.ItemDecoration {
    private final Paint paint;
    private final int spacing; // 间距大小
    private final int dividerThickness;  // 分割线粗细
    private final boolean includeEdge; // 是否包含边缘

    /**
     * ItemDecoration 用于 RecyclerView 的间距与分割线绘制
     *
     * @param color            分割线颜色
     * @param spacing          item 间间距（单位：px）
     * @param dividerThickness 分割线尺寸（单位：px，水平为高度，垂直为宽度）
     * @param includeEdge      是否包含 RecyclerView 四周外边距
     */
    public RecyclerDivider(
            @ColorInt int color,
            int spacing,
            int dividerThickness,
            boolean includeEdge
    ) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.FILL);

        this.spacing = spacing;
        this.dividerThickness = dividerThickness;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(
            @NonNull Rect outRect,
            @NonNull View view,
            @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state
    ) {
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (lm instanceof GridLayoutManager gridLayoutManager) {
            handleGridOffsets(outRect, view, parent, state, gridLayoutManager);
        } else {
            handleLinearOffsets(outRect, view, parent, state);
        }
    }

    /**
     * 处理 LinearLayoutManager 的 Item 间距
     */
    private void handleLinearOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state
    ) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        int itemCount = state.getItemCount();
        if (includeEdge && position == 0) {
            outRect.top = spacing;
        }
        if (position != itemCount - 1) {
            outRect.bottom = spacing;
        } else if (includeEdge) {
            outRect.bottom = spacing;
        }
    }

    /**
     * 处理 GridLayoutManager 的 Item 间距
     */
    private void handleGridOffsets(
            Rect outRect,
            View view,
            RecyclerView parent,
            RecyclerView.State state,
            GridLayoutManager layoutManager
    ) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        int spanCount = layoutManager.getSpanCount();
        int column = position % spanCount;
        int itemCount = state.getItemCount();
        boolean isLastRow = isLastRow(position, itemCount, spanCount);
        boolean isLastColumn = column == spanCount - 1;

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }

        // 避免最后一列额外间距
        if (!includeEdge && isLastColumn) {
            outRect.right = 0;
        }
        // 避免最后一行额外间距
        if (!includeEdge && isLastRow) {
            outRect.bottom = 0;
        }
    }

    @Override
    public void onDraw(
            @NonNull Canvas canvas,
            @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state
    ) {
        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        if (lm instanceof GridLayoutManager gridLayoutManager) {
            drawGrid(canvas, parent, state, gridLayoutManager);
        } else {
            drawLinear(canvas, parent, state);
        }
    }

    /**
     * 绘制 LinearLayoutManager 的分割线
     */
    private void drawLinear(
            Canvas canvas,
            RecyclerView parent,
            RecyclerView.State state
    ) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            int itemCount = state.getItemCount();
            if (!includeEdge && position == itemCount - 1) {
                continue;
            }

            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + dividerThickness;
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    /**
     * 绘制 GridLayoutManager 的分割线（右边和底部）
     */
    private void drawGrid(
            Canvas canvas,
            RecyclerView parent,
            RecyclerView.State state,
            GridLayoutManager layoutManager
    ) {
        int childCount = parent.getChildCount();
        int spanCount = layoutManager.getSpanCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            int itemCount = state.getItemCount();
            int column = position % spanCount;
            boolean isLastColumn = column == spanCount - 1;
            boolean isLastRow = isLastRow(position, itemCount, spanCount);

            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();

            // 画右边 divider
            if (!isLastColumn || includeEdge) {
                int left = child.getRight() + params.rightMargin;
                int right = left + dividerThickness;
                int top = child.getTop() - params.topMargin;
                int bottom = child.getBottom() + params.bottomMargin;
                canvas.drawRect(left, top, right, bottom, paint);
            }

            // 画底部 divider
            if (!isLastRow || includeEdge) {
                int left = child.getLeft() - params.leftMargin;
                int right = child.getRight() + params.rightMargin;
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + dividerThickness;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }

    /**
     * 判断是否为 Grid 最后一行
     */
    private boolean isLastRow(int position, int itemCount, int spanCount) {
        return position >= ((itemCount - 1) / spanCount) * spanCount;
    }
}
