package com.chanapps.four.component;

import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.widget.GridView;
import com.chanapps.four.component.ChanViewHelper.ServiceType;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: arley
 * Date: 11/21/12
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChanGridSizer {

    private static final String TAG = ChanGridSizer.class.getSimpleName();

    private static final int[] MAX_COLUMN_WIDTHS = {
            ServiceType.SELECTOR.ordinal(), 310,
            ServiceType.BOARD.ordinal(), 260,
            ServiceType.THREAD.ordinal(), 135,
            ServiceType.WATCHLIST.ordinal(), 260
    };

    private GridView g;
    private Display d;
    private int numColumns = 0;
    private int columnWidth = 0;
    private int maxColumnWidth = 260;

    public ChanGridSizer(GridView g, Display d, ChanViewHelper.ServiceType serviceType) {
        this.g = g;
        this.d = d;
        for (int i = 0; i < MAX_COLUMN_WIDTHS.length; i += 2) {
            if (serviceType.ordinal() == MAX_COLUMN_WIDTHS[i]) {
                maxColumnWidth = MAX_COLUMN_WIDTHS[i + 1];
            }
        }
    }

    public void sizeGridToDisplay() {
        Point size = new Point();
        d.getSize(size);
        int width = size.x;
        int height = size.y;
        numColumns = width / maxColumnWidth == 1 ? 2 : width / maxColumnWidth;
        columnWidth = (width - 15) / numColumns;
        Log.i(TAG, "sizeGridToDisplay width: " + width + ", height: " + height + ", numCols: " + numColumns);
        g.setNumColumns(numColumns);
        g.setColumnWidth(columnWidth);
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getNumColumns() {
        return numColumns;
    }
}