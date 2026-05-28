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
 * RecyclerView 分割线装饰器
 *
 * 支持：
 * 1. LinearLayoutManager
 * 2. GridLayoutManager
 *
 * includeEdge:
 * true  -> 包含 RecyclerView 外围边框 + item 内部分割线
 * false -> 仅包含 item 内部分割线
 */
public class RecyclerDivider extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private final int dividerThickness; // 分割线尺寸（px）
    private final boolean includeEdge;  // 是否包含 RecyclerView 外围边框

    /**
     * @param color             分割线颜色
     * @param dividerThickness  分割线尺寸（单位：px）
     * @param includeEdge       是否包含 RecyclerView 外围边框
     */
    public RecyclerDivider(@ColorInt int color, int dividerThickness, boolean includeEdge) {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setColor(color);
        this.paint.setStyle(Paint.Style.FILL);
        this.dividerThickness = dividerThickness;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager gridLayoutManager) {
            handleGridOffsets(outRect, view, parent, state, gridLayoutManager);
        } else {
            handleLinearOffsets(outRect, view, parent, state);
        }
    }

    private void handleLinearOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        int itemCount = state.getItemCount();
        if (includeEdge) {
            outRect.left = dividerThickness;
            outRect.right = dividerThickness;
        }
        if (includeEdge && position == 0) {
            outRect.top = dividerThickness;
        }
        if (position != itemCount - 1) {
            outRect.bottom = dividerThickness;
        }
        if (includeEdge && position == itemCount - 1) {
            outRect.bottom = dividerThickness;
        }
    }

    private void handleGridOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull GridLayoutManager layoutManager) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        int spanCount = layoutManager.getSpanCount();
        int itemCount = state.getItemCount();
        int column = position % spanCount;
        boolean isLastColumn = column == spanCount - 1;
        boolean isLastRow = isLastRow(position, itemCount, spanCount);

        if (includeEdge) {
            outRect.left = dividerThickness - column * dividerThickness / spanCount;
            outRect.right = (column + 1) * dividerThickness / spanCount;
            if (position < spanCount) {
                outRect.top = dividerThickness;
            }
            outRect.bottom = dividerThickness;
        } else {
            outRect.left = column * dividerThickness / spanCount;
            outRect.right = dividerThickness - (column + 1) * dividerThickness / spanCount;
            if (position >= spanCount) {
                outRect.top = dividerThickness;
            }
            if (isLastColumn) {
                outRect.right = 0;
            }
            if (isLastRow) {
                outRect.bottom = 0;
            }
        }
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager gridLayoutManager) {
            drawGrid(canvas, parent, state, gridLayoutManager);
        } else {
            drawLinear(canvas, parent, state);
        }
    }

    private void drawLinear(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            int itemCount = state.getItemCount();
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int childLeft = child.getLeft() - params.leftMargin;
            int childRight = child.getRight() + params.rightMargin;
            int childTop = child.getTop() - params.topMargin;
            int childBottom = child.getBottom() + params.bottomMargin;
            if (includeEdge && position == 0) {
                canvas.drawRect(childLeft - dividerThickness, childTop - dividerThickness, childRight + dividerThickness, childTop, paint);
            }
            if (includeEdge) {
                canvas.drawRect(childLeft - dividerThickness, childTop, childLeft, childBottom, paint);
                canvas.drawRect(childRight, childTop, childRight + dividerThickness, childBottom, paint);
            }
            if (includeEdge || position != itemCount - 1) {
                canvas.drawRect(childLeft - (includeEdge ? dividerThickness : 0), childBottom, childRight + (includeEdge ? dividerThickness : 0), childBottom + dividerThickness, paint);
            }
        }
    }

    private void drawGrid(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state, @NonNull GridLayoutManager layoutManager) {
        int childCount = parent.getChildCount();
        int spanCount = layoutManager.getSpanCount();
        int itemCount = state.getItemCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }
            int column = position % spanCount;
            boolean isLastColumn = column == spanCount - 1;
            boolean isLastRow = isLastRow(position, itemCount, spanCount);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int childLeft = child.getLeft() - params.leftMargin;
            int childRight = child.getRight() + params.rightMargin;
            int childTop = child.getTop() - params.topMargin;
            int childBottom = child.getBottom() + params.bottomMargin;
            if (includeEdge && column == 0) {
                canvas.drawRect(childLeft - dividerThickness, childTop, childLeft, childBottom, paint);
            }
            if (includeEdge && position < spanCount) {
                canvas.drawRect(childLeft, childTop - dividerThickness, childRight, childTop, paint);
            }
            if (includeEdge || !isLastColumn) {
                canvas.drawRect(childRight, childTop, childRight + dividerThickness, childBottom, paint);
            }
            if (includeEdge || !isLastRow) {
                canvas.drawRect(childLeft, childBottom, childRight, childBottom + dividerThickness, paint);
            }
        }
    }

    private boolean isLastRow(int position, int itemCount, int spanCount) {
        int remainder = itemCount % spanCount;
        int lastRowStart = (remainder == 0) ? (itemCount - spanCount) : (itemCount - remainder);
        return position >= lastRowStart;
    }
}