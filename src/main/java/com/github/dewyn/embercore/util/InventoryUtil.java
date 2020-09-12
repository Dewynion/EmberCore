package com.github.dewyn.embercore.util;

public class InventoryUtil {
    public static final int INVENTORY_COLUMNS = 9;
    public static final int INVENTORY_ROWS = 6;
    public static final int LAST_COL_INDEX = (INVENTORY_COLUMNS - 1);
    public static final int LAST_ROW_INDEX = (INVENTORY_ROWS - 1) * INVENTORY_COLUMNS;
    // if you decide to fork, don't edit this, edit its components
    public static final int INVENTORY_SIZE = INVENTORY_ROWS * INVENTORY_COLUMNS;

    /**
     * @param row The zero-indexed row of the inventory. Clamped to [0, {@value #LAST_ROW_INDEX} - 1].
     * @return The index of the first slot in the given row, indexed from 0.
     */
    public static int indexOfRow(int row) {
        return MathUtil.clamp(row, 0, INVENTORY_ROWS - 1) * INVENTORY_COLUMNS;
    }

    /**
     * @param row The zero-indexed row of the inventory. Clamped to [0, {@value #LAST_ROW_INDEX}].
     * @param col The zero-indexed column of the inventory. Clamped to [0, {@value #LAST_COL_INDEX}].
     * @return The one-dimensional index of the requested slot.
     */
    public static int indexOfSlot(int row, int col) {
        return indexOfRow(row) + MathUtil.clamp(col, 0, INVENTORY_COLUMNS - 1);
    }
}
